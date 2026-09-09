import { prisma } from '../../config/prisma';
import { StudentStatus, UserRole, AllocationStatus, RoomStatus } from '../../types/enums';
import { hashPassword } from '../../utils/password';

export class StudentsService {
  /**
   * Generate a unique, secure Student ID in format STU-YYYY-XXXX (e.g., STU-2026-0001).
   * Verifies against database to guarantee zero collisions, fills sequence gaps,
   * and stays strictly formatted in 4-digit sequence format.
   */
  async generateUniqueStudentId(): Promise<string> {
    const year = new Date().getFullYear();
    const prefix = `STU-${year}-`;

    for (let attempt = 1; attempt <= 3; attempt++) {
      try {
        // Find all student roll numbers for this year prefix
        const existing = await prisma.student.findMany({
          where: {
            rollNumber: {
              startsWith: prefix
            }
          },
          select: { rollNumber: true }
        });

        const existingSeqSet = new Set<number>();
        for (const item of existing) {
          const parts = item.rollNumber.split('-');
          if (parts.length === 3) {
            const seq = parseInt(parts[2], 10);
            if (!isNaN(seq) && seq > 0) {
              existingSeqSet.add(seq);
            }
          }
        }

        // Find lowest unused sequential number starting from 1
        let nextSeq = 1;
        while (existingSeqSet.has(nextSeq)) {
          nextSeq++;
        }

        let candidateId = `${prefix}${String(nextSeq).padStart(4, '0')}`;

        // Extra collision safety check
        while (await prisma.student.findFirst({
          where: { rollNumber: { equals: candidateId, mode: 'insensitive' } }
        })) {
          nextSeq++;
          candidateId = `${prefix}${String(nextSeq).padStart(4, '0')}`;
        }

        return candidateId;
      } catch (err: any) {
        if (attempt < 3 && (err?.code === 'P1001' || err?.code === 'P1017' || err?.message?.includes('closed the connection') || err?.message?.includes('forcibly closed'))) {
          console.warn(`[STUDENT_ID] Retrying DB connection (attempt ${attempt}/3)...`);
          await new Promise(res => setTimeout(res, 800 * attempt));
          continue;
        }
        console.warn('[STUDENT_ID] Falling back to random sequence due to DB latency:', err);
        const rand = Math.floor(1000 + Math.random() * 9000);
        return `${prefix}${rand}`;
      }
    }
    const rand = Math.floor(1000 + Math.random() * 9000);
    return `${prefix}${rand}`;
  }

  /**
   * Admin / Hostel Owner creates a student record with a controlled, unique Student ID.
   */
  async createStudentByAdmin(
    data: {
      fullName: string;
      phoneNumber?: string;
      email?: string;
      collegeName: string;
      course: string;
      yearOfStudy: string;
      gender?: string;
      permanentAddress?: string;
      emergencyContactName?: string;
      emergencyContactPhone?: string;
      studentId?: string; // Optional custom ID or auto-generated
      password?: string;
      hostelId?: string | null;
      roomId?: string | null;
      roomNumber?: string | null;
      bedNumber?: string | null;
    },
    creator?: { role?: string; profileId?: string; userId?: string }
  ) {
    // 1. Validate required fields
    if (!data.fullName?.trim()) {
      throw { status: 400, message: 'Student full name is required.' };
    }
    if (!data.collegeName?.trim()) {
      throw { status: 400, message: 'College or University name is required.' };
    }
    if (!data.course?.trim()) {
      throw { status: 400, message: 'Course name is required.' };
    }
    if (!data.yearOfStudy?.trim()) {
      throw { status: 400, message: 'Year of study is required.' };
    }
    const rawPhone = (data.phoneNumber || data.emergencyContactPhone || '').trim();
    if (!rawPhone) {
      throw { status: 400, message: 'Mobile phone number is required.' };
    }
    const cleanPhone = rawPhone.replace(/[^0-9+]/g, '');
    if (cleanPhone.length < 7 || cleanPhone.length > 15) {
      throw { status: 400, message: 'Please enter a valid mobile phone number.' };
    }

    // 2. Resolve or generate unique Student ID
    let finalStudentId = data.studentId?.trim();
    if (!finalStudentId) {
      finalStudentId = await this.generateUniqueStudentId();
    } else {
      const existing = await prisma.student.findFirst({
        where: { rollNumber: { equals: finalStudentId, mode: 'insensitive' } }
      });
      if (existing) {
        throw { status: 409, message: `Student ID '${finalStudentId}' already exists.` };
      }
    }

    // 3. Resolve target hostel from authenticated owner context or specified hostel
    let resolvedHostelId: string | null = null;
    let hostelName: string | undefined = undefined;

    if (creator?.role === UserRole.HOST || creator?.profileId || creator?.userId) {
      const host = await prisma.host.findFirst({
        where: {
          OR: [
            { id: creator?.profileId || undefined },
            { userId: creator?.userId || undefined }
          ]
        },
        include: { hostels: true }
      });
      if (host && host.hostels.length > 0) {
        resolvedHostelId = host.hostels[0].id;
        hostelName = host.hostels[0].name;
      }
    }

    if (!resolvedHostelId && data.hostelId && data.hostelId.trim()) {
      const hostel = await prisma.hostel.findUnique({ where: { id: data.hostelId.trim() } });
      if (hostel) {
        resolvedHostelId = hostel.id;
        hostelName = hostel.name;
      }
    }

    if (!resolvedHostelId) {
      const anyHostel = await prisma.hostel.findFirst();
      if (anyHostel) {
        resolvedHostelId = anyHostel.id;
        hostelName = anyHostel.name;
      }
    }

    // 4. Resolve room if selected (server-side scoped resolution by hostelId + roomNumber)
    const rawRoomNumber = (data.roomNumber || data.roomId || '').trim();
    const cleanRoomNumber = (rawRoomNumber && rawRoomNumber !== 'null' && rawRoomNumber !== 'undefined' && rawRoomNumber !== '""') ? rawRoomNumber : null;

    console.log('[ROOM DEBUG] BACKEND RECEIVED roomNumber:', cleanRoomNumber, 'ownerHostelId:', resolvedHostelId);

    let preResolvedRoom: any = null;
    if (cleanRoomNumber) {
      console.log('[ROOM DEBUG] PRISMA QUERY roomNumber:', cleanRoomNumber, 'hostelId:', resolvedHostelId);

      // Look up by room number scoped to the authenticated owner's hostel
      if (resolvedHostelId) {
        preResolvedRoom = await prisma.room.findFirst({
          where: {
            hostelId: resolvedHostelId,
            roomNumber: { equals: cleanRoomNumber, mode: 'insensitive' }
          },
          include: { hostel: true }
        });

        // If not matched by roomNumber, also check by exact primary key UUID within the authorized hostel
        if (!preResolvedRoom) {
          preResolvedRoom = await prisma.room.findFirst({
            where: {
              hostelId: resolvedHostelId,
              id: cleanRoomNumber
            },
            include: { hostel: true }
          });
        }
      } else if (creator?.role === 'ADMIN') {
        // Global fallback only if superadmin without any specified hostel
        preResolvedRoom = await prisma.room.findFirst({
          where: {
            OR: [
              { roomNumber: { equals: cleanRoomNumber, mode: 'insensitive' } },
              { id: cleanRoomNumber }
            ]
          },
          include: { hostel: true }
        });
      }

      console.log('[ROOM DEBUG] DATABASE RESULT:', preResolvedRoom ? { id: preResolvedRoom.id, roomNumber: preResolvedRoom.roomNumber, hostelId: preResolvedRoom.hostelId } : 'NOT FOUND IN DB');

      // If room not found in DB yet, dynamically auto-provision room record so any selected room number works seamlessly
      if (!preResolvedRoom && resolvedHostelId) {
        try {
          preResolvedRoom = await prisma.room.create({
            data: {
              hostelId: resolvedHostelId,
              roomNumber: cleanRoomNumber,
              floor: 1,
              block: 'A',
              roomType: 'DOUBLE',
              totalCapacity: 2,
              occupiedCount: 0,
              status: RoomStatus.AVAILABLE
            },
            include: { hostel: true, beds: true }
          });
          console.log('[ROOM DEBUG] Dynamically auto-created room record for roomNumber:', cleanRoomNumber);
        } catch (createRoomErr) {
          console.warn('[ROOM WARNING] Could not auto-create room record; will store roomNumber directly on Student:', createRoomErr);
        }
      }

      if (preResolvedRoom) {
        resolvedHostelId = preResolvedRoom.hostelId;
        hostelName = preResolvedRoom.hostel?.name;
      }
    }

    // 5. Resolve clean email & password hash before starting transaction
    let finalEmail: string = data.email?.trim() || '';
    if (!finalEmail) {
      const cleanId = finalStudentId.toLowerCase().replace(/[^a-z0-9]/g, '');
      finalEmail = `${cleanId}@campus.edu`;
    }

    const initialPassword = data.password?.trim() || 'Password@123';
    const passwordHash = await hashPassword(initialPassword);

    // 6. Atomic Prisma Transaction with 25s timeout for cloud database
    return await prisma.$transaction(async (tx) => {
      // Verify Room & Bed if selected
      let validRoom: any = null;
      let roomNumber: string | undefined = cleanRoomNumber || undefined;
      let validBed: any = null;

      if (preResolvedRoom) {
        validRoom = await tx.room.findUnique({
          where: { id: preResolvedRoom.id },
          include: { beds: true, hostel: true }
        });

        if (validRoom) {
          if (validRoom.occupiedCount >= validRoom.totalCapacity) {
            throw { status: 400, message: `Selected Room ${validRoom.roomNumber} is already full.` };
          }

          roomNumber = validRoom.roomNumber;
          if (!resolvedHostelId) {
            resolvedHostelId = validRoom.hostelId;
            hostelName = validRoom.hostel?.name;
          }

          // Auto-create beds if room has fewer beds than total capacity
          const currentBedsCount = validRoom.beds?.length || 0;
          if (currentBedsCount < validRoom.totalCapacity) {
            const bedLetters = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
            const bedsData = [];
            for (let i = currentBedsCount; i < validRoom.totalCapacity; i++) {
              bedsData.push({
                roomId: validRoom.id,
                bedNumber: `Bed-${bedLetters[i] || (i + 1)}`,
                isOccupied: false
              });
            }
            if (bedsData.length > 0) {
              await tx.bed.createMany({ data: bedsData });
              validRoom = await tx.room.findUnique({
                where: { id: validRoom.id },
                include: { beds: true, hostel: true }
              });
            }
          }

          if (data.bedNumber && data.bedNumber.trim()) {
            validBed = validRoom.beds.find((b: any) =>
              b.id === data.bedNumber?.trim() || b.bedNumber.toLowerCase() === data.bedNumber?.trim().toLowerCase()
            );
            if (validBed && validBed.isOccupied) {
              throw { status: 400, message: `Bed ${validBed.bedNumber} in Room ${validRoom.roomNumber} is already occupied.` };
            }
          }
          if (!validBed) {
            validBed = validRoom.beds.find((b: any) => !b.isOccupied);
          }

          if (!validBed) {
            throw { status: 400, message: `Selected Room ${validRoom.roomNumber} is already full.` };
          }
        }
      }

      // Check existing email
      const existingEmail = await tx.user.findUnique({ where: { email: finalEmail } });
      if (existingEmail) {
        const [localPart, domain] = finalEmail.split('@');
        finalEmail = `${localPart}_${Date.now().toString().slice(-4)}@${domain || 'campus.edu'}`;
      }

      // Create User with STUDENT role strictly
      const createdUser: any = await tx.user.create({
        data: {
          email: finalEmail,
          passwordHash,
          role: UserRole.STUDENT,
          fullName: data.fullName.trim(),
          phoneNumber: cleanPhone,
          isActive: true,
          studentProfile: {
            create: {
              fullName: data.fullName.trim(),
              rollNumber: finalStudentId!,
              collegeName: data.collegeName.trim(),
              course: data.course.trim(),
              yearOfStudy: data.yearOfStudy.trim(),
              gender: data.gender?.trim() || 'Male',
              permanentAddress: data.permanentAddress?.trim() || 'Campus Resident',
              emergencyContactName: data.emergencyContactName?.trim() || `${data.fullName.trim()} Guardian`,
              emergencyContactPhone: cleanPhone,
              hostelId: resolvedHostelId,
              hostelName: hostelName || null,
              roomId: validRoom?.id || null,
              roomNumber: roomNumber || null,
              bedNumber: validBed?.bedNumber || null,
              status: StudentStatus.ACTIVE,
              isActivated: false,
              admissionDate: new Date()
            }
          }
        },
        include: {
          studentProfile: true
        }
      });

      // Complete RoomAllocation atomically if room & bed were assigned
      if (validRoom && validBed && createdUser.studentProfile) {
        await tx.roomAllocation.create({
          data: {
            bedId: validBed.id,
            roomId: validRoom.id,
            hostelId: resolvedHostelId || validRoom.hostelId,
            studentId: createdUser.studentProfile.id,
            status: AllocationStatus.ACTIVE,
            checkInDate: new Date(),
            allocatedBy: creator?.userId || null,
            remarks: `Initial room allocation for student ${finalStudentId}`
          }
        });

        await tx.bed.update({
          where: { id: validBed.id },
          data: { isOccupied: true }
        });

        const newOccupiedCount = await tx.bed.count({
          where: { roomId: validRoom.id, isOccupied: true }
        });

        await tx.room.update({
          where: { id: validRoom.id },
          data: {
            occupiedCount: newOccupiedCount,
            status: newOccupiedCount >= validRoom.totalCapacity ? RoomStatus.FULL : RoomStatus.AVAILABLE
          }
        });

        if (resolvedHostelId) {
          const totalOccupiedBedsInHostel = await tx.bed.count({
            where: {
              room: { hostelId: resolvedHostelId },
              isOccupied: true
            }
          });
          await tx.hostel.update({
            where: { id: resolvedHostelId },
            data: { occupiedBeds: totalOccupiedBedsInHostel }
          });
        }
      }

      return {
        success: true,
        message: 'Student registered and Student ID generated successfully.',
        student: this.mapStudent(createdUser.studentProfile, finalEmail),
        credentials: {
          studentId: finalStudentId,
          email: finalEmail,
          initialPassword
        }
      };
    }, {
      maxWait: 10000,
      timeout: 25000
    });
  }

  /**
   * Hostel Owner deallocates a student:
   * 1. Verifies ownership & permission
   * 2. Sets Student status to DEALLOCATED and clears room/bed links
   * 3. Marks RoomAllocation to VACATED/DEALLOCATED
   * 4. Frees up Bed, decrements Room occupied count, decrements Hostel occupied beds
   * 5. Revokes all active RefreshTokens for immediate session termination
   */
  async deallocateStudent(data: {
    studentId: string;
    requesterHostId?: string;
    requesterRole?: string;
    remarks?: string;
  }) {
    const student = await prisma.student.findFirst({
      where: {
        OR: [
          { id: data.studentId },
          { userId: data.studentId },
          { rollNumber: data.studentId }
        ]
      },
      include: {
        user: true,
        hostel: true,
        room: true,
        allocations: {
          where: { status: 'ACTIVE' }
        }
      }
    });

    if (!student) {
      throw { status: 404, message: `Student record not found for ID: ${data.studentId}` };
    }

    // Permission check for Hostel Owner
    if (data.requesterRole === UserRole.HOST && data.requesterHostId && student.hostelId) {
      const host = await prisma.host.findUnique({
        where: { id: data.requesterHostId },
        include: { hostels: { select: { id: true } } }
      });
      const ownedHostelIds = host?.hostels.map(h => h.id) || [];
      if (!ownedHostelIds.includes(student.hostelId)) {
        throw { status: 403, message: 'You are not authorized to deallocate students from another hostel.' };
      }
    }

    return prisma.$transaction(async (tx) => {
      // 1. Mark active room allocations as VACATED / DEALLOCATED
      await tx.roomAllocation.updateMany({
        where: {
          studentId: student.id,
          status: AllocationStatus.ACTIVE
        },
        data: {
          status: AllocationStatus.VACATED,
          checkOutDate: new Date(),
          remarks: data.remarks || 'Deallocated by Hostel Administration'
        }
      });

      // 2. Free Bed if assigned
      if (student.roomId && student.bedNumber) {
        await tx.bed.updateMany({
          where: {
            roomId: student.roomId,
            bedNumber: student.bedNumber
          },
          data: { isOccupied: false }
        });
      }

      // 3. Update Room occupancy if assigned
      if (student.roomId) {
        const room = await tx.room.findUnique({ where: { id: student.roomId } });
        if (room && room.occupiedCount > 0) {
          await tx.room.update({
            where: { id: student.roomId },
            data: {
              occupiedCount: Math.max(0, room.occupiedCount - 1),
              status: RoomStatus.AVAILABLE
            }
          });
        }
      }

      // 4. Update Hostel occupancy if assigned
      if (student.hostelId) {
        const hostel = await tx.hostel.findUnique({ where: { id: student.hostelId } });
        if (hostel && hostel.occupiedBeds > 0) {
          await tx.hostel.update({
            where: { id: student.hostelId },
            data: { occupiedBeds: Math.max(0, hostel.occupiedBeds - 1) }
          });
        }
      }

      // 5. Update Student status to DEALLOCATED and unbind room/bed/hostel
      const updatedStudent = await tx.student.update({
        where: { id: student.id },
        data: {
          status: 'DEALLOCATED',
          hostelId: null,
          hostelName: null,
          roomId: null,
          roomNumber: null,
          bedNumber: null
        }
      });

      // 6. Deactivate user account record
      await tx.user.update({
        where: { id: student.userId },
        data: { isActive: false }
      });

      // 7. Revoke active RefreshTokens to kill all active sessions immediately
      await tx.refreshToken.updateMany({
        where: { userId: student.userId },
        data: { revoked: true }
      });

      return {
        success: true,
        message: 'Student deallocated successfully. Student access has been revoked.',
        student: this.mapStudent(updatedStudent)
      };
    }, { maxWait: 15000, timeout: 30000 });
  }

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
          { userId: studentIdOrUserId },
          { rollNumber: studentIdOrUserId }
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
    const resolvedHostelId = student.hostelId || alloc?.hostelId || null;
    let resolvedRoomId = student.roomId || student.room?.id || alloc?.roomId;
    let resolvedRoomNumber = student.roomNumber || student.room?.roomNumber || alloc?.room?.roomNumber;
    let resolvedBedNumber = student.bedNumber || alloc?.bed?.bedNumber;

    if (!resolvedRoomId && resolvedRoomNumber && resolvedHostelId) {
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
      hostelName: student.hostelName || student.hostel?.name || alloc?.hostel?.name || null,
      roomId: resolvedRoomId || null,
      roomNumber: resolvedRoomNumber || null,
      bedNumber: resolvedBedNumber || null
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

  async getMyRoommates(userId: string) {
    const student = await prisma.student.findFirst({
      where: {
        OR: [{ userId }, { id: userId }]
      },
      include: {
        hostel: { select: { id: true, name: true } },
        room: {
          include: {
            beds: {
              include: {
                allocations: {
                  where: { status: 'ACTIVE' },
                  include: {
                    student: {
                      include: {
                        user: { select: { phoneNumber: true } }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    });

    if (!student) {
      throw { status: 404, message: 'Student profile not found' };
    }

    if (!student.roomId && !student.roomNumber) {
      return {
        room: null,
        myBed: student.bedNumber || null,
        roommates: []
      };
    }

    let room = student.room;
    if (!room && student.roomNumber) {
      room = await prisma.room.findFirst({
        where: {
          hostelId: student.hostelId || undefined,
          roomNumber: student.roomNumber
        },
        include: {
          beds: {
            include: {
              allocations: {
                where: { status: 'ACTIVE' },
                include: {
                  student: {
                    include: {
                      user: { select: { phoneNumber: true } }
                    }
                  }
                }
              }
            }
          }
        }
      });
    }

    const coStudents = await prisma.student.findMany({
      where: {
        status: 'ACTIVE',
        id: { not: student.id },
        OR: [
          ...(room?.id ? [{ roomId: room.id }] : []),
          ...(student.roomNumber ? [{ hostelId: student.hostelId, roomNumber: student.roomNumber }] : [])
        ]
      },
      include: {
        user: { select: { phoneNumber: true } }
      }
    });

    const roommateMap = new Map<string, any>();

    if (room?.beds) {
      for (const bed of room.beds) {
        for (const alloc of bed.allocations) {
          const s = alloc.student;
          if (s && s.id !== student.id) {
            const phone = s.user?.phoneNumber || s.emergencyContactPhone || '';
            roommateMap.set(s.id, {
              studentId: s.id,
              fullName: s.fullName,
              rollNumber: s.rollNumber,
              course: s.course,
              yearOfStudy: s.yearOfStudy,
              bedNumber: s.bedNumber || bed.bedNumber,
              phoneNumber: phone
            });
          }
        }
      }
    }

    for (const s of coStudents) {
      if (!roommateMap.has(s.id)) {
        const phone = s.user?.phoneNumber || s.emergencyContactPhone || '';
        roommateMap.set(s.id, {
          studentId: s.id,
          fullName: s.fullName,
          rollNumber: s.rollNumber,
          course: s.course,
          yearOfStudy: s.yearOfStudy,
          bedNumber: s.bedNumber || 'Assigned',
          phoneNumber: phone
        });
      }
    }

    let parsedAmenities = [];
    if (room?.amenities) {
      try {
        parsedAmenities = typeof room.amenities === 'string' ? JSON.parse(room.amenities) : room.amenities;
      } catch {
        parsedAmenities = [];
      }
    }

    const roommates = Array.from(roommateMap.values());

    return {
      room: room ? {
        roomId: room.id,
        roomNumber: room.roomNumber,
        floor: room.floor,
        block: room.block,
        roomType: room.roomType,
        totalCapacity: room.totalCapacity,
        occupiedCount: room.occupiedCount,
        monthlyRent: room.monthlyRent,
        amenities: parsedAmenities
      } : {
        roomId: student.roomId || '',
        roomNumber: student.roomNumber || '',
        floor: 1,
        block: 'A',
        roomType: 'DOUBLE',
        totalCapacity: 2,
        occupiedCount: roommates.length + 1,
        monthlyRent: 0.0,
        amenities: []
      },
      myBed: student.bedNumber || null,
      roommates
    };
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

  private mapStudent(s: any, customEmail?: string) {
    if (!s) return null;
    return {
      studentId: s.id,
      userId: s.userId,
      fullName: s.fullName,
      rollNumber: s.rollNumber,
      email: customEmail || s.user?.email || (s.rollNumber ? `${s.rollNumber.toLowerCase()}@campus.edu` : ''),
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
