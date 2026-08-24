import { prisma } from '../../config/prisma';

export class UsersService {
  async getAllUsers() {
    const users = await prisma.user.findMany({
      select: {
        id: true,
        email: true,
        role: true,
        fullName: true,
        phoneNumber: true,
        avatarUrl: true,
        isActive: true,
        fcmToken: true,
        createdAt: true
      },
      orderBy: { createdAt: 'desc' }
    });

    return users.map(u => ({
      userId: u.id,
      email: u.email,
      role: u.role,
      fullName: u.fullName,
      phoneNumber: u.phoneNumber || '',
      avatarUrl: u.avatarUrl,
      isActive: u.isActive,
      fcmToken: u.fcmToken,
      createdAt: u.createdAt.getTime()
    }));
  }

  async getUserById(id: string) {
    const user = await prisma.user.findUnique({
      where: { id },
      include: {
        studentProfile: true,
        hostProfile: true,
        adminProfile: true
      }
    });

    if (!user) {
      throw { status: 404, message: 'User not found' };
    }

    return {
      userId: user.id,
      email: user.email,
      role: user.role,
      fullName: user.fullName,
      phoneNumber: user.phoneNumber || '',
      avatarUrl: user.avatarUrl,
      isActive: user.isActive,
      fcmToken: user.fcmToken,
      createdAt: user.createdAt.getTime(),
      studentProfile: user.studentProfile,
      hostProfile: user.hostProfile,
      adminProfile: user.adminProfile
    };
  }

  async updateUser(id: string, data: { fullName?: string; phoneNumber?: string; avatarUrl?: string; fcmToken?: string }) {
    const updated = await prisma.user.update({
      where: { id },
      data
    });

    return {
      userId: updated.id,
      email: updated.email,
      role: updated.role,
      fullName: updated.fullName,
      phoneNumber: updated.phoneNumber || '',
      avatarUrl: updated.avatarUrl,
      isActive: updated.isActive,
      fcmToken: updated.fcmToken,
      createdAt: updated.createdAt.getTime()
    };
  }

  async toggleUserStatus(id: string, isActive: boolean) {
    const updated = await prisma.user.update({
      where: { id },
      data: { isActive }
    });

    return {
      userId: updated.id,
      isActive: updated.isActive
    };
  }
}
