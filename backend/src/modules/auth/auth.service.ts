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

  async login(email: string, password: string) {
    const normalizedEmail = (email || '').trim().toLowerCase();
    const user = await prisma.user.findFirst({
      where: {
        email: {
          equals: normalizedEmail,
          mode: 'insensitive'
        }
      },
      include: {
        studentProfile: true,
        hostProfile: {
          include: { hostels: { select: { id: true, name: true } } }
        },
        adminProfile: true
      }
    });

    if (!user || !user.isActive) {
      throw { status: 401, message: 'Invalid email or password' };
    }

    const isMatch = await comparePassword(password, user.passwordHash);
    if (!isMatch) {
      throw { status: 401, message: 'Invalid email or password' };
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
