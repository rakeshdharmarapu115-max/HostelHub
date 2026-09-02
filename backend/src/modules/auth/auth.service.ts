import { prisma } from '../../config/prisma';
import { hashPassword, comparePassword } from '../../utils/password';
import { generateAccessToken, generateRefreshToken, verifyRefreshToken } from '../../utils/jwt';
import { UserRole, StudentStatus } from '../../types/enums';
import { emailService } from '../../services/email.service';

export class AuthService {
  async registerStudent(data: {
    email: string;
    password: string;
    fullName: string;
    rollNumber: string;
    collegeName: string;
    course: string;
    yearOfStudy: string;
    gender: string;
    permanentAddress: string;
    emergencyContactName: string;
    emergencyContactPhone: string;
    hostelId?: string | null;
    roomId?: string | null;
    bedNumber?: string | null;
  }) {
    const existingUser = await prisma.user.findUnique({ where: { email: data.email } });
    if (existingUser) {
      throw { status: 409, message: 'Email address is already registered' };
    }

    const existingStudent = await prisma.student.findUnique({ where: { rollNumber: data.rollNumber } });
    if (existingStudent) {
      throw { status: 409, message: 'Roll number is already registered' };
    }

    const passwordHash = await hashPassword(data.password);

    const user = await prisma.$transaction(async (tx) => {
      let hostelName: string | undefined = undefined;
      let roomNumber: string | undefined = undefined;

      if (data.hostelId) {
        const hostel = await tx.hostel.findUnique({ where: { id: data.hostelId } });
        hostelName = hostel?.name;
      }
      if (data.roomId) {
        const room = await tx.room.findUnique({ where: { id: data.roomId } });
        roomNumber = room?.roomNumber;
      }

      const createdUser = await tx.user.create({
        data: {
          email: data.email,
          passwordHash,
          role: UserRole.STUDENT,
          fullName: data.fullName,
          phoneNumber: data.emergencyContactPhone,
          studentProfile: {
            create: {
              fullName: data.fullName,
              rollNumber: data.rollNumber,
              collegeName: data.collegeName,
              course: data.course,
              yearOfStudy: data.yearOfStudy,
              gender: data.gender,
              permanentAddress: data.permanentAddress,
              emergencyContactName: data.emergencyContactName,
              emergencyContactPhone: data.emergencyContactPhone,
              hostelId: data.hostelId || null,
              hostelName: hostelName || null,
              roomId: data.roomId || null,
              roomNumber: roomNumber || null,
              bedNumber: data.bedNumber || null,
              status: StudentStatus.ACTIVE
            }
          }
        },
        include: {
          studentProfile: true
        }
      });

      return createdUser;
    });

    const tokenPayload = {
      userId: user.id,
      email: user.email,
      role: user.role as UserRole,
      profileId: user.studentProfile?.id,
      hostelId: user.studentProfile?.hostelId || undefined
    };

    const accessToken = generateAccessToken(tokenPayload);
    const refreshToken = generateRefreshToken({ userId: user.id });

    await prisma.refreshToken.create({
      data: {
        userId: user.id,
        token: refreshToken,
        expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
      }
    });

    // Send cloud welcome email asynchronously
    emailService.sendWelcomeEmail(user.email, user.fullName, 'Student Resident')
      .catch(err => console.error('[EMAIL] Error sending welcome email:', err));

    return {
      user: {
        userId: user.id,
        email: user.email,
        role: user.role,
        fullName: user.fullName,
        phoneNumber: user.phoneNumber,
        avatarUrl: user.avatarUrl,
        isActive: user.isActive,
        studentId: user.studentProfile?.id || null,
        hostId: null,
        adminId: null,
        hostelId: user.studentProfile?.hostelId || null,
        createdAt: user.createdAt.getTime(),
        studentProfile: user.studentProfile
      },
      tokens: {
        accessToken,
        refreshToken
      }
    };
  }

  async registerHost(data: {
    email: string;
    password: string;
    fullName: string;
    businessName: string;
    contactPhone: string;
    contactEmail: string;
  }) {
    const existingUser = await prisma.user.findUnique({ where: { email: data.email } });
    if (existingUser) {
      throw { status: 409, message: 'Email address is already registered' };
    }

    const passwordHash = await hashPassword(data.password);

    const user = await prisma.user.create({
      data: {
        email: data.email,
        passwordHash,
        role: UserRole.HOST,
        fullName: data.fullName,
        phoneNumber: data.contactPhone,
        hostProfile: {
          create: {
            fullName: data.fullName,
            businessName: data.businessName,
            contactPhone: data.contactPhone,
            contactEmail: data.contactEmail,
            verifiedStatus: true // In dev/demo, auto-verified
          }
        }
      },
      include: {
        hostProfile: true
      }
    });

    const tokenPayload = {
      userId: user.id,
      email: user.email,
      role: user.role as UserRole,
      profileId: user.hostProfile?.id
    };

    if (user.hostProfile) {
      try {
        const initialHostel = await prisma.hostel.create({
          data: {
            name: data.businessName || `${data.fullName}'s Hostel`,
            address: 'Campus Road',
            city: 'Campus Town',
            state: 'State',
            postalCode: '500001',
            contactEmail: data.contactEmail,
            contactPhone: data.contactPhone,
            hostId: user.hostProfile.id,
            totalRooms: 0,
            occupiedBeds: 0,
            totalBeds: 0
          }
        });
        (tokenPayload as any).hostelId = initialHostel.id;
      } catch (err) {
        console.warn('[HOST_REGISTER] Default hostel auto-creation skipped:', err);
      }
    }

    const accessToken = generateAccessToken(tokenPayload);
    const refreshToken = generateRefreshToken({ userId: user.id });

    await prisma.refreshToken.create({
      data: {
        userId: user.id,
        token: refreshToken,
        expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
      }
    });

    // Send cloud welcome email asynchronously
    emailService.sendWelcomeEmail(user.email, user.fullName, 'Host / Property Owner')
      .catch(err => console.error('[EMAIL] Error sending welcome email:', err));

    return {
      user: {
        userId: user.id,
        email: user.email,
        role: user.role as UserRole,
        fullName: user.fullName,
        phoneNumber: user.phoneNumber,
        avatarUrl: user.avatarUrl,
        isActive: user.isActive,
        studentId: null,
        hostId: user.hostProfile?.id || null,
        adminId: null,
        hostelId: null,
        createdAt: user.createdAt.getTime(),
        hostProfile: user.hostProfile
      },
      tokens: {
        accessToken,
        refreshToken
      }
    };
  }

  async registerAdmin(data: {
    email: string;
    password: string;
    fullName: string;
    associationName: string;
    designation: string;
    contactPhone?: string | null;
  }) {
    const existingUser = await prisma.user.findUnique({ where: { email: data.email } });
    if (existingUser) {
      throw { status: 409, message: 'Email address is already registered' };
    }

    const passwordHash = await hashPassword(data.password);

    const user = await prisma.user.create({
      data: {
        email: data.email,
        passwordHash,
        role: UserRole.ADMIN,
        fullName: data.fullName,
        phoneNumber: data.contactPhone || '',
        adminProfile: {
          create: {
            fullName: data.fullName,
            associationName: data.associationName || 'Campus Housing Association',
            designation: data.designation || 'Association Head',
            contactPhone: data.contactPhone || '',
            permissions: 'ALL'
          }
        }
      },
      include: {
        adminProfile: true
      }
    });

    const tokenPayload = {
      userId: user.id,
      email: user.email,
      role: user.role as UserRole,
      profileId: user.adminProfile?.id
    };

    const accessToken = generateAccessToken(tokenPayload);
    const refreshToken = generateRefreshToken({ userId: user.id });

    await prisma.refreshToken.create({
      data: {
        userId: user.id,
        token: refreshToken,
        expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
      }
    });

    // Send cloud welcome email asynchronously
    emailService.sendWelcomeEmail(user.email, user.fullName, 'Association Administrator')
      .catch(err => console.error('[EMAIL] Error sending welcome email:', err));

    return {
      user: {
        userId: user.id,
        email: user.email,
        role: user.role as UserRole,
        fullName: user.fullName,
        phoneNumber: user.phoneNumber,
        avatarUrl: user.avatarUrl,
        isActive: user.isActive,
        studentId: null,
        hostId: null,
        adminId: user.adminProfile?.id || null,
        hostelId: null,
        createdAt: user.createdAt.getTime(),
        adminProfile: user.adminProfile
      },
      tokens: {
        accessToken,
        refreshToken
      }
    };
  }

  async validateStudentId(studentId: string) {
    const rawId = (studentId || '').trim();
    if (!rawId) {
      throw { status: 400, message: 'Student ID is required.' };
    }

    const student = await prisma.student.findFirst({
      where: {
        OR: [
          { rollNumber: { equals: rawId, mode: 'insensitive' } },
          { id: rawId }
        ]
      },
      include: {
        hostel: { select: { id: true, name: true, city: true } },
        room: { select: { roomNumber: true } }
      }
    });

    if (!student) {
      throw {
        status: 404,
        message: 'Invalid Student ID. Please contact your hostel owner.'
      };
    }

    if (student.status === 'DEALLOCATED') {
      throw {
        status: 403,
        code: 'HOSTEL_ALLOCATION_INACTIVE',
        message: 'Your hostel allocation has ended. Please contact your hostel owner.'
      };
    }

    if (student.isActivated) {
      throw {
        status: 409,
        code: 'STUDENT_ALREADY_REGISTERED',
        message: 'This Student ID has already been registered.'
      };
    }

    return {
      valid: true,
      studentId: student.id,
      rollNumber: student.rollNumber,
      fullName: student.fullName,
      collegeName: student.collegeName,
      course: student.course,
      yearOfStudy: student.yearOfStudy,
      hostelId: student.hostelId,
      hostelName: student.hostel?.name || student.hostelName || 'Campus Hostel',
      roomNumber: student.room?.roomNumber || student.roomNumber || null,
      isActivated: false
    };
  }

  async activateStudent(data: {
    studentId: string;
    emailOrPhone?: string;
    email?: string;
    mobileNumber?: string;
    phoneNumber?: string;
    password: string;
    confirmPassword?: string;
  }) {
    const rawId = (data.studentId || '').trim();
    const identifier = (data.email || data.mobileNumber || data.phoneNumber || data.emailOrPhone || '').trim();
    const newPassword = (data.password || '').trim();

    if (!rawId) {
      throw { status: 400, message: 'Student ID is required.' };
    }
    if (!newPassword || newPassword.length < 6) {
      throw { status: 400, message: 'Password must be at least 6 characters.' };
    }
    if (data.confirmPassword && newPassword !== data.confirmPassword.trim()) {
      throw { status: 400, message: 'Passwords do not match.' };
    }

    const explicitEmail = (data.email || (identifier.includes('@') ? identifier : '')).trim().toLowerCase();
    const explicitPhone = (data.mobileNumber || data.phoneNumber || (!identifier.includes('@') ? identifier : '')).trim();

    const emailToSet = explicitEmail || undefined;
    const phoneToSet = explicitPhone || undefined;

    const student = await prisma.student.findFirst({
      where: {
        OR: [
          { rollNumber: { equals: rawId, mode: 'insensitive' } },
          { id: rawId }
        ]
      },
      include: {
        user: true,
        hostel: true,
        room: true
      }
    });

    if (!student) {
      throw { status: 404, message: `Student ID '${rawId}' is not found.` };
    }

    if (student.isActivated) {
      throw { status: 409, message: 'This Student ID is already activated. Please log in with your credentials.' };
    }

    // Check if new email is already taken by another user
    if (emailToSet && emailToSet !== student.user.email.toLowerCase()) {
      const existingUserWithEmail = await prisma.user.findUnique({
        where: { email: emailToSet }
      });
      if (existingUserWithEmail && existingUserWithEmail.id !== student.userId) {
        throw { status: 409, message: `Email address '${emailToSet}' is already linked to another account.` };
      }
    }

    const passwordHash = await hashPassword(newPassword);

    const updatedUser = await prisma.$transaction(async (tx) => {
      // 1. Update Student record
      await tx.student.update({
        where: { id: student.id },
        data: {
          isActivated: true,
          status: StudentStatus.ACTIVE
        }
      });

      // 2. Update User credentials
      const user = await tx.user.update({
        where: { id: student.userId },
        data: {
          passwordHash,
          isActive: true,
          ...(emailToSet && { email: emailToSet }),
          ...(phoneToSet && { phoneNumber: phoneToSet })
        },
        include: {
          studentProfile: {
            include: { hostel: true, room: true }
          }
        }
      });

      return user;
    });

    const tokenPayload = {
      userId: updatedUser.id,
      email: updatedUser.email,
      role: updatedUser.role as UserRole,
      profileId: updatedUser.studentProfile?.id,
      hostelId: updatedUser.studentProfile?.hostelId || undefined
    };

    const accessToken = generateAccessToken(tokenPayload);
    const refreshToken = generateRefreshToken({ userId: updatedUser.id });

    await prisma.refreshToken.create({
      data: {
        userId: updatedUser.id,
        token: refreshToken,
        expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
      }
    });

    return {
      user: {
        userId: updatedUser.id,
        email: updatedUser.email,
        role: updatedUser.role,
        fullName: updatedUser.fullName,
        phoneNumber: updatedUser.phoneNumber,
        avatarUrl: updatedUser.avatarUrl,
        isActive: updatedUser.isActive,
        studentId: updatedUser.studentProfile?.id || null,
        hostId: null,
        adminId: null,
        hostelId: updatedUser.studentProfile?.hostelId || null,
        createdAt: updatedUser.createdAt.getTime(),
        studentProfile: updatedUser.studentProfile
      },
      tokens: {
        accessToken,
        refreshToken
      }
    };
  }

  async forgotPassword(identifier: string) {
    const raw = (identifier || '').trim();
    if (!raw) {
      throw { status: 400, message: 'Mobile number, Gmail, or Student ID is required.' };
    }

    const normalizedEmail = raw.toLowerCase();

    const user = await prisma.user.findFirst({
      where: {
        OR: [
          { email: { equals: normalizedEmail, mode: 'insensitive' } },
          { phoneNumber: raw },
          { studentProfile: { rollNumber: { equals: raw, mode: 'insensitive' } } },
          { studentProfile: { id: raw } }
        ]
      }
    });

    if (!user) {
      throw { status: 404, message: 'No registered account found matching that email, phone, or Student ID.' };
    }

    // Generate 6-digit verification code
    const otp = String(Math.floor(100000 + Math.random() * 900000));

    // Send email alert if email is registered
    if (user.email.includes('@')) {
      emailService.sendNotificationEmail(
        user.email,
        'HostelHub Password Reset Verification',
        `Hello ${user.fullName},\n\nYour 6-digit password reset verification code is: ${otp}\n\nThis code expires in 15 minutes.`
      ).catch(err => console.error('[EMAIL] Forgot password notification error:', err));
    }

    return {
      success: true,
      message: `Password reset verification code generated for ${user.fullName}.`,
      otpPreview: otp,
      identifier: raw
    };
  }

  async resetPassword(data: { identifier: string; otp?: string; newPassword: string }) {
    const raw = (data.identifier || '').trim();
    const newPassword = (data.newPassword || '').trim();

    if (!raw) {
      throw { status: 400, message: 'Identifier is required.' };
    }
    if (!newPassword || newPassword.length < 6) {
      throw { status: 400, message: 'New password must be at least 6 characters.' };
    }

    const normalizedEmail = raw.toLowerCase();

    const user = await prisma.user.findFirst({
      where: {
        OR: [
          { email: { equals: normalizedEmail, mode: 'insensitive' } },
          { phoneNumber: raw },
          { studentProfile: { rollNumber: { equals: raw, mode: 'insensitive' } } },
          { studentProfile: { id: raw } }
        ]
      }
    });

    if (!user) {
      throw { status: 404, message: 'Account not found.' };
    }

    const passwordHash = await hashPassword(newPassword);

    await prisma.$transaction(async (tx) => {
      await tx.user.update({
        where: { id: user.id },
        data: { passwordHash }
      });

      // Revoke existing refresh tokens
      await tx.refreshToken.updateMany({
        where: { userId: user.id },
        data: { revoked: true }
      });
    });

    return {
      success: true,
      message: 'Your personal password has been updated securely. You can now log in.'
    };
  }

  async login(emailOrStudentIdOrPhone: string, password: string) {
    const rawIdentifier = (emailOrStudentIdOrPhone || '').trim();
    const normalizedEmail = rawIdentifier.toLowerCase();

    const user = await prisma.user.findFirst({
      where: {
        OR: [
          {
            email: {
              equals: normalizedEmail,
              mode: 'insensitive'
            }
          },
          {
            phoneNumber: rawIdentifier
          },
          {
            studentProfile: {
              rollNumber: {
                equals: rawIdentifier,
                mode: 'insensitive'
              }
            }
          },
          {
            studentProfile: {
              id: {
                equals: rawIdentifier
              }
            }
          }
        ]
      },
      include: {
        studentProfile: true,
        hostProfile: {
          include: { hostels: { select: { id: true, name: true } } }
        },
        adminProfile: true
      }
    });

    if (!user) {
      throw { status: 401, message: 'Invalid Mobile/Email/Student ID or password' };
    }

    if (!user.isActive) {
      throw { status: 403, code: 'ACCOUNT_INACTIVE', message: 'User account is inactive or disabled' };
    }

    // Check student-specific allocation status
    if (user.role === UserRole.STUDENT && user.studentProfile) {
      if (user.studentProfile.status === 'DEALLOCATED') {
        throw {
          status: 403,
          code: 'HOSTEL_ALLOCATION_INACTIVE',
          message: 'Your hostel allocation has ended. You have been logged out.'
        };
      }
      if (user.studentProfile.status === 'INACTIVE') {
        throw {
          status: 403,
          code: 'ACCOUNT_INACTIVE',
          message: 'This Student ID is inactive. Please contact your hostel administrator for assistance.'
        };
      }
    }

    const isMatch = await comparePassword(password, user.passwordHash);
    if (!isMatch) {
      throw { status: 401, message: 'Invalid Student ID/Email or password' };
    }

    let profileId: string | undefined = undefined;
    let hostelId: string | undefined = undefined;

    if (user.role === UserRole.STUDENT && user.studentProfile) {
      profileId = user.studentProfile.id;
      hostelId = user.studentProfile.hostelId || undefined;
    } else if (user.role === UserRole.HOST && user.hostProfile) {
      profileId = user.hostProfile.id;
      hostelId = user.hostProfile.hostels[0]?.id;
    } else if (user.role === UserRole.ADMIN && user.adminProfile) {
      profileId = user.adminProfile.id;
    }

    const tokenPayload = {
      userId: user.id,
      email: user.email,
      role: user.role as UserRole,
      profileId,
      hostelId
    };

    const accessToken = generateAccessToken(tokenPayload);
    const refreshToken = generateRefreshToken({ userId: user.id });

    await prisma.refreshToken.create({
      data: {
        userId: user.id,
        token: refreshToken,
        expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
      }
    });

    return {
      user: {
        userId: user.id,
        email: user.email,
        role: user.role as UserRole,
        fullName: user.fullName,
        phoneNumber: user.phoneNumber,
        avatarUrl: user.avatarUrl,
        isActive: user.isActive,
        studentId: user.studentProfile?.id || null,
        hostId: user.hostProfile?.id || null,
        adminId: user.adminProfile?.id || null,
        hostelId: hostelId || null,
        createdAt: user.createdAt.getTime(),
        studentProfile: user.studentProfile,
        hostProfile: user.hostProfile,
        adminProfile: user.adminProfile
      },
      tokens: {
        accessToken,
        refreshToken
      }
    };
  }

  async refreshToken(token: string) {
    try {
      const payload = verifyRefreshToken(token);

      const storedToken = await prisma.refreshToken.findUnique({
        where: { token },
        include: { user: { include: { studentProfile: true, hostProfile: { include: { hostels: true } }, adminProfile: true } } }
      });

      if (!storedToken || storedToken.revoked || storedToken.expiresAt < new Date()) {
        throw { status: 401, message: 'Invalid or expired refresh token' };
      }

      const user = storedToken.user;
      if (!user.isActive) {
        throw { status: 403, message: 'User account is inactive' };
      }

      let profileId: string | undefined = undefined;
      let hostelId: string | undefined = undefined;

      if (user.role === UserRole.STUDENT && user.studentProfile) {
        profileId = user.studentProfile.id;
        hostelId = user.studentProfile.hostelId || undefined;
      } else if (user.role === UserRole.HOST && user.hostProfile) {
        profileId = user.hostProfile.id;
        hostelId = user.hostProfile.hostels[0]?.id;
      } else if (user.role === UserRole.ADMIN && user.adminProfile) {
        profileId = user.adminProfile.id;
      }

      const newAccessToken = generateAccessToken({
        userId: user.id,
        email: user.email,
        role: user.role as UserRole,
        profileId,
        hostelId
      });

      return { accessToken: newAccessToken };
    } catch (err) {
      throw { status: 401, message: 'Refresh token verification failed' };
    }
  }

  async logout(token?: string) {
    if (token) {
      await prisma.refreshToken.updateMany({
        where: { token },
        data: { revoked: true }
      });
    }
  }

  async getMe(userId: string) {
    const user = await prisma.user.findFirst({
      where: {
        OR: [
          { id: userId },
          { studentProfile: { id: userId } },
          { hostProfile: { id: userId } },
          { adminProfile: { id: userId } }
        ]
      },
      include: {
        studentProfile: {
          include: {
            hostel: true,
            room: true
          }
        },
        hostProfile: {
          include: {
            hostels: true
          }
        },
        adminProfile: true
      }
    });

    if (!user) {
      throw { status: 404, message: 'User not found' };
    }

    let hostelId: string | undefined = undefined;
    if (user.role === UserRole.STUDENT) {
      hostelId = user.studentProfile?.hostelId || undefined;
    } else if (user.role === UserRole.HOST) {
      hostelId = user.hostProfile?.hostels?.[0]?.id || undefined;
    }

    return {
      userId: user.id,
      email: user.email,
      role: user.role,
      fullName: user.fullName,
      phoneNumber: user.phoneNumber,
      avatarUrl: user.avatarUrl,
      isActive: user.isActive,
      studentId: user.studentProfile?.id || null,
      hostId: user.hostProfile?.id || null,
      adminId: user.adminProfile?.id || null,
      hostelId: hostelId || null,
      createdAt: user.createdAt.getTime(),
      studentProfile: user.studentProfile,
      hostProfile: user.hostProfile,
      adminProfile: user.adminProfile
    };
  }
}
