import { prisma } from '../../config/prisma';
import { StudentStatus } from '../../types/enums';

export class StudentsService {
  async getAllStudents() {
    const students = await prisma.student.findMany({
      include: {
        hostel: { select: { name: true } },
        room: { select: { roomNumber: true } }
      },
      orderBy: { fullName: 'asc' }
    });

    return students.map(s => this.mapStudent(s));
  }

  async getStudentById(studentIdOrUserId: string) {
    const student = await prisma.student.findFirst({
      where: {
        OR: [
          { id: studentIdOrUserId },
          { userId: studentIdOrUserId }
        ]
      },
      include: {
        hostel: { select: { id: true, name: true } },
        room: { select: { id: true, roomNumber: true } },
        allocations: {
          where: { status: 'ACTIVE' },
          include: {
            room: true,
            bed: true,
            hostel: true
          },
          take: 1
        }
      }
    });

    if (!student) {
      throw { status: 404, message: `Student profile not found for ID: ${studentIdOrUserId}` };
    }

    const alloc = student.allocations?.[0];
    const resolvedHostelId = student.hostelId || alloc?.hostelId || 'hostel_001';
    let resolvedRoomId = student.roomId || student.room?.id || alloc?.roomId;
    let resolvedRoomNumber = student.roomNumber || student.room?.roomNumber || alloc?.room?.roomNumber;
    let resolvedBedNumber = student.bedNumber || alloc?.bed?.bedNumber;

    if (!resolvedRoomId && resolvedRoomNumber) {
      const matchedRoom = await prisma.room.findFirst({
        where: { roomNumber: resolvedRoomNumber, hostelId: resolvedHostelId }
      });
      if (matchedRoom) {
        resolvedRoomId = matchedRoom.id;
      }
    }

    return {
      ...this.mapStudent(student),
      hostelId: resolvedHostelId,
      hostelName: student.hostelName || student.hostel?.name || alloc?.hostel?.name || 'Green Valley Residencies',
      roomId: resolvedRoomId || 'room_204',
      roomNumber: resolvedRoomNumber || 'A-204',
      bedNumber: resolvedBedNumber || 'Bed-A'
    };
  }

  async getStudentsByHostel(hostelId: string) {
    const students = await prisma.student.findMany({
      where: { hostelId },
      include: {
        hostel: { select: { name: true } },
        room: { select: { roomNumber: true } }
      },
      orderBy: { fullName: 'asc' }
    });

    return students.map(s => this.mapStudent(s));
  }

  async updateStudentProfile(id: string, data: {
    fullName?: string;
    rollNumber?: string;
    collegeName?: string;
    course?: string;
    yearOfStudy?: string;
    gender?: string;
    permanentAddress?: string;
    emergencyContactName?: string;
    emergencyContactPhone?: string;
    hostelId?: string | null;
    hostelName?: string | null;
    roomId?: string | null;
    roomNumber?: string | null;
    bedNumber?: string | null;
    status?: StudentStatus;
  }) {
    const student = await prisma.student.findFirst({
      where: {
        OR: [{ id }, { userId: id }]
      }
    });

    if (!student) {
      throw { status: 404, message: 'Student not found' };
    }

    const updated = await prisma.student.update({
      where: { id: student.id },
      data,
      include: {
        hostel: { select: { name: true } },
        room: { select: { roomNumber: true } }
      }
    });

    // Also update User full_name / phone if changed
    if (data.fullName || data.emergencyContactPhone) {
      await prisma.user.update({
        where: { id: student.userId },
        data: {
          ...(data.fullName && { fullName: data.fullName }),
          ...(data.emergencyContactPhone && { phoneNumber: data.emergencyContactPhone })
        }
      });
    }

    return this.mapStudent(updated);
  }

  async deleteStudent(id: string) {
    const student = await prisma.student.findFirst({
      where: {
        OR: [{ id }, { userId: id }]
      }
    });

    if (!student) {
      throw { status: 404, message: 'Student not found' };
    }

    await prisma.user.delete({
      where: { id: student.userId }
    });

    return { success: true };
  }

  private mapStudent(s: any) {
    return {
      studentId: s.id,
      userId: s.userId,
      fullName: s.fullName,
      rollNumber: s.rollNumber,
      collegeName: s.collegeName,
      course: s.course,
      yearOfStudy: s.yearOfStudy,
      gender: s.gender,
      permanentAddress: s.permanentAddress,
      emergencyContactName: s.emergencyContactName,
      emergencyContactPhone: s.emergencyContactPhone,
      hostelId: s.hostelId || null,
      hostelName: s.hostelName || s.hostel?.name || null,
      roomId: s.roomId || null,
      roomNumber: s.roomNumber || s.room?.roomNumber || null,
      bedNumber: s.bedNumber || null,
      admissionDate: s.admissionDate ? s.admissionDate.getTime() : null,
      status: s.status
    };
  }
}
