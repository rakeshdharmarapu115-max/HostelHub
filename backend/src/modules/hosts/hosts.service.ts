import { prisma } from '../../config/prisma';

export class HostsService {
  async getAllHosts() {
    const hosts = await prisma.host.findMany({
      include: {
        hostels: { select: { id: true, name: true } }
      },
      orderBy: { fullName: 'asc' }
    });

    return hosts.map(h => ({
      hostId: h.id,
      userId: h.userId,
      fullName: h.fullName,
      businessName: h.businessName,
      contactPhone: h.contactPhone,
      contactEmail: h.contactEmail,
      hostelIds: h.hostels.map(hostel => hostel.id),
      verifiedStatus: h.verifiedStatus,
      createdAt: h.createdAt.getTime()
    }));
  }

  async getHostById(hostIdOrUserId: string) {
    const host = await prisma.host.findFirst({
      where: {
        OR: [{ id: hostIdOrUserId }, { userId: hostIdOrUserId }]
      },
      include: {
        hostels: true
      }
    });

    if (!host) {
      throw { status: 404, message: 'Host not found' };
    }

    return {
      hostId: host.id,
      userId: host.userId,
      fullName: host.fullName,
      businessName: host.businessName,
      contactPhone: host.contactPhone,
      contactEmail: host.contactEmail,
      hostelIds: host.hostels.map(h => h.id),
      hostels: host.hostels,
      verifiedStatus: host.verifiedStatus,
      createdAt: host.createdAt.getTime()
    };
  }

  async verifyHost(hostId: string, verified: boolean) {
    const updated = await prisma.host.update({
      where: { id: hostId },
      data: { verifiedStatus: verified }
    });

    return {
      hostId: updated.id,
      verifiedStatus: updated.verifiedStatus
    };
  }
}
