import { prisma } from '../../config/prisma';
import { AllocationStatus, RoomStatus, StudentStatus } from '../../types/enums';

export class AllocationsService {
  /**
   * Atomic room allocation transaction:
   * 1. Check bed exists
   * 2. Check room exists
   * 3. Check/find or auto-create student record
   * 4. Clear any previous active allocation (transfer)
   * 5. Create new RoomAllocation record
   * 6. Update Bed isOccupied = true
   * 7. Increment Room occupiedCount
   * 8. Update Student hostelId, roomId, roomNumber, bedNumber
   * 9. Increment Hostel occupiedBeds
   */
  async allocateBed(data: {
    bedId: string;
    roomId: string;
    studentId: string;
    studentName?: string;
    allocatedBy?: string;
    remarks?: string;
  }) {
    return prisma.$transaction(async (tx) => {
      // 1. Verify Bed
      let bed = await tx.bed.findUnique({
        where: { id: data.bedId },
        include: { room: true }
      });

      if (!bed) {
        bed = await tx.bed.findFirst({
          where: {
            OR: [
              { id: data.bedId },
              { bedNumber: data.bedId }
            ],
            ...(data.roomId ? { roomId: data.roomId } : {})
          },
          include: { room: true }
        });
      }

      if (!bed) {
        throw { status: 404, message: `Bed not found for ID: ${data.bedId}` };
      }

      // 2. Verify Room
      let room = await tx.room.findUnique({
        where: { id: data.roomId || bed.roomId }
      });

      if (!room) {
        room = await tx.room.findFirst({
          where: {
            OR: [
              { id: data.roomId },
              { roomNumber: data.roomId }
            ]
          }
        });
      }

      if (!room) {
        throw { status: 404, message: `Room not found for ID: ${data.roomId}` };
      }

      // 3. Verify / Find Student
      const cleanStudentId = data.studentId.trim();
      let student = await tx.student.findFirst({
        where: {
          OR: [
            { id: cleanStudentId },
            { userId: cleanStudentId },
            { rollNumber: cleanStudentId },
            { rollNumber: { equals: cleanStudentId, mode: 'insensitive' } },
            { user: { id: cleanStudentId } },
            { user: { email: cleanStudentId } },
            { user: { phoneNumber: cleanStudentId } },
            { emergencyContactPhone: cleanStudentId }
          ]
        },
        include: { user: true }
      });

      if (!student) {
        // Auto-create student record if owner entered a new student
        const email = cleanStudentId.includes('@')
          ? cleanStudentId
          : `student_${Date.now().toString().slice(-6)}@campus.edu`;

        const existingUser = await tx.user.findUnique({ where: { email } });
        const finalEmail = existingUser ? `student_${Date.now()}@campus.edu` : email;
        
        const user = await tx.user.create({
          data: {
            email: finalEmail,
            passwordHash: '$2b$10$epRswTFs9lPoEx5644EumeGgkNVP5smwa.88JmR4lT3gH8m',
            fullName: data.studentName || 'Resident Student',
            role: 'STUDENT',
            isActive: true
          }
        });

        // Ensure rollNumber is unique
        let rollNumber = cleanStudentId;
        const existingRoll = await tx.student.findUnique({ where: { rollNumber } });
        if (existingRoll) {
          rollNumber = `STU-${new Date().getFullYear()}-${Math.floor(1000 + Math.random() * 9000)}`;
        }

        student = await tx.student.create({
          data: {
            userId: user.id,
            rollNumber,
            fullName: data.studentName || 'Resident Student',
            collegeName: 'Campus Institute',
            course: 'General',
            yearOfStudy: '1st Year',
            gender: 'Male',
            permanentAddress: 'Hostel Resident',
            emergencyContactName: (data.studentName || 'Resident Student') + ' Guardian',
            emergencyContactPhone: '9876543210',
            status: StudentStatus.ACTIVE,
            admissionDate: new Date()
          },
          include: { user: true }
        });
      }

      if (!student) {
        throw { status: 404, message: `Student not found for identifier: ${cleanStudentId}` };
      }

      // 4. Handle previous allocations (transfer cleanly)
      const previousAllocations = await tx.roomAllocation.findMany({
        where: {
          studentId: student.id,
          status: AllocationStatus.ACTIVE
        }
      });

      for (const prev of previousAllocations) {
        await tx.roomAllocation.update({
          where: { id: prev.id },
          data: {
            status: AllocationStatus.VACATED,
            checkOutDate: new Date(),
            remarks: 'Transferred to new room'
          }
        });

        if (prev.bedId && prev.bedId !== bed.id) {
          await tx.bed.updateMany({
            where: { id: prev.bedId },
            data: { isOccupied: false }
          });
        }

        if (prev.roomId && prev.roomId !== room.id) {
          const oldRoom = await tx.room.findUnique({ where: { id: prev.roomId } });
          if (oldRoom) {
            await tx.room.update({
              where: { id: oldRoom.id },
              data: {
                occupiedCount: Math.max(0, oldRoom.occupiedCount - 1),
                status: RoomStatus.AVAILABLE
              }
            });
          }
        }
      }

      const hostelId = room.hostelId;
      const hostel = await tx.hostel.findUnique({ where: { id: hostelId } });

      // 5. Create RoomAllocation record
      let verifiedAllocatedBy: string | null = null;
      if (data.allocatedBy) {
        const u = await tx.user.findUnique({ where: { id: data.allocatedBy } });
        if (u) verifiedAllocatedBy = u.id;
      }

      const allocation = await tx.roomAllocation.create({
        data: {
          bedId: bed.id,
          roomId: room.id,
          hostelId: room.hostelId,
          studentId: student.id,
          status: AllocationStatus.ACTIVE,
          checkInDate: new Date(),
          allocatedBy: verifiedAllocatedBy,
          remarks: data.remarks || 'Allocated via HostelHub'
        }
      });

      // 6. Update Bed
      await tx.bed.update({
        where: { id: bed.id },
        data: { isOccupied: true }
      });

      // 7. Update Room
      const newOccupiedCount = room.occupiedCount + 1;
      await tx.room.update({
        where: { id: room.id },
        data: {
          occupiedCount: newOccupiedCount,
          status: newOccupiedCount >= room.totalCapacity ? RoomStatus.FULL : RoomStatus.AVAILABLE
        }
      });

      // 8. Update Student
      await tx.student.update({
        where: { id: student.id },
        data: {
          hostelId,
          hostelName: hostel?.name,
          roomId: room.id,
          roomNumber: room.roomNumber,
          bedNumber: bed.bedNumber,
          status: StudentStatus.ACTIVE
        }
      });

      // 9. Update Hostel
      if (hostelId) {
        await tx.hostel.update({
          where: { id: hostelId },
          data: {
            occupiedBeds: { increment: 1 }
          }
        });
      }

      return {
        allocationId: allocation.id,
        bedId: bed.id,
        roomId: room.id,
        roomNumber: room.roomNumber,
        bedNumber: bed.bedNumber,
        studentId: student.id,
        studentName: student.fullName,
        hostelId,
        allocationDate: allocation.allocationDate.getTime(),
        status: allocation.status
      };
    });
  }

  /**
   * Atomic checkout / vacate transaction:
   * Vacates bed, frees room count, and clears student assignment reliably.
   */
  async vacateBed(data: {
    bedId?: string;
    roomId?: string;
    allocationId?: string;
    vacatedBy?: string;
    remarks?: string;
  }) {
    return prisma.$transaction(async (tx) => {
      // 1. Locate bed
      let bed = null;
      if (data.bedId) {
        bed = await tx.bed.findFirst({
          where: {
            OR: [
              { id: data.bedId },
              { bedNumber: data.bedId }
            ],
            ...(data.roomId ? { roomId: data.roomId } : {})
          }
        });
      }

      const actualBedId = bed?.id || data.bedId;

      // 2. Find active allocation if any
      let allocation = null;
      if (data.allocationId) {
        allocation = await tx.roomAllocation.findUnique({
          where: { id: data.allocationId }
        });
      } else if (actualBedId) {
        allocation = await tx.roomAllocation.findFirst({
          where: { bedId: actualBedId, status: AllocationStatus.ACTIVE }
        });
      }

      if (allocation) {
        await tx.roomAllocation.update({
          where: { id: allocation.id },
          data: {
            status: AllocationStatus.VACATED,
            checkOutDate: new Date(),
            remarks: data.remarks || 'Vacated bed'
          }
        });

        // Clear Student assignment
        await tx.student.updateMany({
          where: { id: allocation.studentId },
          data: {
            roomId: null,
            roomNumber: null,
            bedNumber: null
          }
        });
      }

      // 3. Mark Bed unoccupied
      if (actualBedId) {
        await tx.bed.updateMany({
          where: { id: actualBedId },
          data: { isOccupied: false }
        });
      }

      if (bed) {
        // Also clear any student assigned with matching bedNumber and roomId
        await tx.student.updateMany({
          where: {
            roomId: bed.roomId,
            bedNumber: bed.bedNumber
          },
          data: {
            roomId: null,
            roomNumber: null,
            bedNumber: null
          }
        });
      }

      // 4. Update Room occupied count
      const roomId = data.roomId || bed?.roomId || allocation?.roomId;
      if (roomId) {
        const currentRoom = await tx.room.findUnique({ where: { id: roomId } });
        if (currentRoom) {
          const newCount = Math.max(0, currentRoom.occupiedCount - 1);
          await tx.room.update({
            where: { id: currentRoom.id },
            data: {
              occupiedCount: newCount,
              status: RoomStatus.AVAILABLE
            }
          });

          // Update hostel beds
          if (currentRoom.hostelId) {
            const hostel = await tx.hostel.findUnique({ where: { id: currentRoom.hostelId } });
            if (hostel && hostel.occupiedBeds > 0) {
              await tx.hostel.update({
                where: { id: currentRoom.hostelId },
                data: { occupiedBeds: { decrement: 1 } }
              });
            }
          }
        }
      }

      return { success: true, message: 'Bed vacated successfully' };
    });
  }

  async getAllocationsByStudent(studentId: string) {
    const student = await prisma.student.findFirst({
      where: {
        OR: [{ id: studentId }, { userId: studentId }, { rollNumber: studentId }]
      }
    });

    if (!student) {
      return [];
    }

    const allocations = await prisma.roomAllocation.findMany({
      where: { studentId: student.id },
      include: {
        room: true,
        bed: true,
        hostel: true
      },
      orderBy: { allocationDate: 'desc' }
    });

    return allocations.map((a) => ({
      allocationId: a.id,
      hostelId: a.hostelId,
      hostelName: a.hostel?.name,
      roomId: a.roomId,
      roomNumber: a.room?.roomNumber,
      bedId: a.bedId,
      bedNumber: a.bed?.bedNumber,
      checkInDate: a.checkInDate?.getTime(),
      checkOutDate: a.checkOutDate?.getTime(),
      status: a.status,
      remarks: a.remarks
    }));
  }
}
