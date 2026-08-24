import { prisma } from '../../config/prisma';
import { RoomType, RoomStatus } from '../../types/enums';

export class RoomsService {
  async getRoomsByHostel(hostelId: string) {
    const rooms = await prisma.room.findMany({
      where: { hostelId },
      include: {
        beds: {
          include: {
            allocations: {
              where: { status: 'ACTIVE' },
              include: { student: { select: { id: true, fullName: true } } }
            }
          }
        }
      },
      orderBy: { roomNumber: 'asc' }
    });

    return rooms.map(r => this.mapRoom(r));
  }

  async getRoomById(id: string) {
    let room = await prisma.room.findUnique({
      where: { id },
      include: {
        beds: {
          include: {
            allocations: {
              where: { status: 'ACTIVE' },
              include: { student: { select: { id: true, fullName: true } } }
            }
          }
        }
      }
    });

    if (!room) {
      room = await prisma.room.findFirst({
        where: {
          OR: [
            { id },
            { roomNumber: id }
          ]
        },
        include: {
          beds: {
            include: {
              allocations: {
                where: { status: 'ACTIVE' },
                include: { student: { select: { id: true, fullName: true } } }
              }
            }
          }
        }
      });
    }

    if (!room) {
      throw { status: 404, message: `Room not found for ID: ${id}` };
    }

    return this.mapRoom(room);
  }

  async addRoom(data: {
    hostelId: string;
    blockId?: string;
    floorId?: string;
    roomNumber: string;
    floor?: number;
    block?: string;
    roomType?: RoomType;
    totalCapacity?: number;
    monthlyRent?: number;
    amenities?: string[];
  }) {
    const capacity = data.totalCapacity || 2;

    const created = await prisma.$transaction(async (tx) => {
      const room = await tx.room.create({
        data: {
          hostelId: data.hostelId,
          blockId: data.blockId,
          floorId: data.floorId,
          roomNumber: data.roomNumber,
          floor: data.floor || 1,
          block: data.block || 'A',
          roomType: data.roomType || RoomType.DOUBLE,
          totalCapacity: capacity,
          occupiedCount: 0,
          monthlyRent: data.monthlyRent || 0.0,
          amenities: Array.isArray(data.amenities) ? JSON.stringify(data.amenities) : (data.amenities || '[]'),
          status: RoomStatus.AVAILABLE
        }
      });

      // Automatically create beds for the room (Bed-A, Bed-B, etc.)
      const bedLetters = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
      const bedsData = [];
      for (let i = 0; i < capacity; i++) {
        bedsData.push({
          roomId: room.id,
          bedNumber: `Bed-${bedLetters[i] || (i + 1)}`,
          isOccupied: false
        });
      }
      await tx.bed.createMany({ data: bedsData });

      // Update hostel total rooms & beds count
      await tx.hostel.update({
        where: { id: data.hostelId },
        data: {
          totalRooms: { increment: 1 },
          totalBeds: { increment: capacity }
        }
      });

      return tx.room.findUnique({
        where: { id: room.id },
        include: { beds: true }
      });
    });

    return this.mapRoom(created);
  }

  async updateRoom(id: string, data: Partial<any>) {
    const updatePayload: any = { ...data };
    if (data.amenities !== undefined) {
      updatePayload.amenities = Array.isArray(data.amenities) ? JSON.stringify(data.amenities) : data.amenities;
    }

    const updated = await prisma.room.update({
      where: { id },
      data: updatePayload,
      include: {
        beds: {
          include: {
            allocations: {
              where: { status: 'ACTIVE' },
              include: { student: { select: { id: true, fullName: true } } }
            }
          }
        }
      }
    });

    return this.mapRoom(updated);
  }

  async deleteRoom(id: string) {
    await prisma.$transaction(async (tx) => {
      const room = await tx.room.findUnique({ where: { id } });
      if (room) {
        await tx.hostel.update({
          where: { id: room.hostelId },
          data: {
            totalRooms: { decrement: 1 },
            totalBeds: { decrement: room.totalCapacity }
          }
        });
      }
      await tx.bed.deleteMany({ where: { roomId: id } });
      await tx.room.delete({ where: { id } });
    });

    return { success: true };
  }

  // Bed operations
  async getBedsByRoom(roomId: string) {
    const beds = await prisma.bed.findMany({
      where: { roomId },
      include: {
        allocations: {
          where: { status: 'ACTIVE' },
          include: { student: { select: { id: true, fullName: true } } }
        }
      }
    });

    return beds.map(b => this.mapBed(b));
  }

  async addBed(roomId: string, bedNumber: string) {
    const bed = await prisma.$transaction(async (tx) => {
      const b = await tx.bed.create({
        data: { roomId, bedNumber, isOccupied: false }
      });

      await tx.room.update({
        where: { id: roomId },
        data: { totalCapacity: { increment: 1 } }
      });

      const room = await tx.room.findUnique({ where: { id: roomId } });
      if (room) {
        await tx.hostel.update({
          where: { id: room.hostelId },
          data: { totalBeds: { increment: 1 } }
        });
      }

      return b;
    });

    return this.mapBed(bed);
  }

  async deleteBed(id: string) {
    await prisma.$transaction(async (tx) => {
      const bed = await tx.bed.findUnique({ where: { id } });
      if (bed) {
        await tx.room.update({
          where: { id: bed.roomId },
          data: { totalCapacity: { decrement: 1 } }
        });

        const room = await tx.room.findUnique({ where: { id: bed.roomId } });
        if (room) {
          await tx.hostel.update({
            where: { id: room.hostelId },
            data: { totalBeds: { decrement: 1 } }
          });
        }
      }
      await tx.bed.delete({ where: { id } });
    });

    return { success: true };
  }

  private mapRoom(r: any) {
    let parsedAmenities: string[] = [];
    try {
      parsedAmenities = typeof r.amenities === 'string' ? JSON.parse(r.amenities) : (r.amenities || []);
    } catch {
      parsedAmenities = [];
    }
    return {
      roomId: r.id,
      hostelId: r.hostelId,
      roomNumber: r.roomNumber,
      floor: r.floor,
      block: r.block,
      roomType: r.roomType,
      totalCapacity: r.totalCapacity,
      occupiedCount: r.occupiedCount,
      monthlyRent: r.monthlyRent,
      amenities: parsedAmenities,
      beds: (r.beds || []).map((b: any) => this.mapBed(b)),
      status: r.status,
      createdAt: r.createdAt.getTime()
    };
  }

  private mapBed(b: any) {
    const activeAlloc = b.allocations && b.allocations.length > 0 ? b.allocations[0] : null;
    return {
      bedId: b.id,
      bedNumber: b.bedNumber,
      studentId: activeAlloc?.student?.id || null,
      studentName: activeAlloc?.student?.fullName || null,
      isOccupied: b.isOccupied
    };
  }
}
