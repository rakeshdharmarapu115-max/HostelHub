import { prisma } from '../../config/prisma';
import { FeeStatusEnum, ComplaintStatusEnum, AttendanceStatusEnum } from '../../types/enums';

export class DashboardService {
  async getStudentDashboardStats(studentIdOrUserId: string) {
    const student = await prisma.student.findFirst({
      where: {
        OR: [{ id: studentIdOrUserId }, { userId: studentIdOrUserId }]
      },
      include: {
        hostel: { select: { name: true } },
        room: { select: { roomNumber: true } }
      }
    });

    if (!student) {
      return {
        roomNumber: 'N/A',
        bedNumber: 'N/A',
        hostelName: 'No Hostel Assigned',
        pendingFees: 0.0,
        activeComplaints: 0,
        attendanceRate: 100
      };
    }

    // Pending fees
    const pendingFeesList = await prisma.fee.findMany({
      where: {
        studentId: student.id,
        status: { in: [FeeStatusEnum.PENDING, FeeStatusEnum.PARTIALLY_PAID, FeeStatusEnum.OVERDUE] }
      }
    });
    const pendingFees = pendingFeesList.reduce((sum, f) => sum + (f.amount - f.amountPaid), 0);

    // Active complaints
    const activeComplaints = await prisma.complaint.count({
      where: {
        studentId: student.id,
        status: { in: [ComplaintStatusEnum.OPEN, ComplaintStatusEnum.IN_PROGRESS] }
      }
    });

    // Attendance rate
    const totalAttendance = await prisma.attendanceRecord.count({
      where: { studentId: student.id }
    });
    const presentAttendance = await prisma.attendanceRecord.count({
      where: {
        studentId: student.id,
        status: { in: [AttendanceStatusEnum.PRESENT, AttendanceStatusEnum.LATE] }
      }
    });

    const attendanceRate = totalAttendance > 0
      ? Math.round((presentAttendance / totalAttendance) * 100)
      : 100;

    return {
      roomNumber: student.roomNumber || student.room?.roomNumber || 'N/A',
      bedNumber: student.bedNumber || 'N/A',
      hostelName: student.hostelName || student.hostel?.name || 'No Hostel Assigned',
      pendingFees,
      activeComplaints,
      attendanceRate
    };
  }

  async getHostDashboardStats(hostelId?: string, hostUserId?: string) {
    let resolvedHostelId = hostelId;

    if (!resolvedHostelId && hostUserId) {
      const host = await prisma.host.findUnique({
        where: { userId: hostUserId },
        include: { hostels: { select: { id: true } } }
      });
      resolvedHostelId = host?.hostels[0]?.id;
    }

    if (!resolvedHostelId) {
      const firstHostel = await prisma.hostel.findFirst({ select: { id: true } });
      resolvedHostelId = firstHostel?.id || 'hostel_001';
    }

    const hostel = await prisma.hostel.findUnique({
      where: { id: resolvedHostelId },
      include: {
        rooms: { select: { totalCapacity: true, occupiedCount: true } }
      }
    });

    if (!hostel) {
      return {
        totalRooms: 0,
        totalBeds: 0,
        occupiedBeds: 0,
        availableBeds: 0,
        pendingFeeCount: 0,
        pendingFeeAmount: 0.0,
        pendingComplaints: 0,
        todayPresent: 0
      };
    }

    const totalRooms = hostel.rooms.length;
    const totalBeds = hostel.rooms.reduce((sum, r) => sum + r.totalCapacity, 0);
    const occupiedBeds = hostel.rooms.reduce((sum, r) => sum + r.occupiedCount, 0);
    const availableBeds = Math.max(0, totalBeds - occupiedBeds);

    // Pending Fees
    const pendingFees = await prisma.fee.findMany({
      where: {
        hostelId: resolvedHostelId,
        status: { in: [FeeStatusEnum.PENDING, FeeStatusEnum.PARTIALLY_PAID, FeeStatusEnum.OVERDUE] }
      }
    });
    const pendingFeeCount = pendingFees.length;
    const pendingFeeAmount = pendingFees.reduce((sum, f) => sum + (f.amount - f.amountPaid), 0);

    // Pending Complaints
    const pendingComplaints = await prisma.complaint.count({
      where: {
        hostelId: resolvedHostelId,
        status: { in: [ComplaintStatusEnum.OPEN, ComplaintStatusEnum.IN_PROGRESS] }
      }
    });

    // Today Attendance Present
    const todayStr = new Date().toISOString().split('T')[0];
    const todayPresent = await prisma.attendanceRecord.count({
      where: {
        hostelId: resolvedHostelId,
        date: todayStr,
        status: { in: [AttendanceStatusEnum.PRESENT, AttendanceStatusEnum.LATE] }
      }
    });

    return {
      totalRooms,
      totalBeds,
      occupiedBeds,
      availableBeds,
      pendingFeeCount,
      pendingFeeAmount,
      pendingComplaints,
      todayPresent
    };
  }

  async getAdminDashboardStats() {
    const totalHostels = await prisma.hostel.count();
    const totalStudents = await prisma.student.count();

    const rooms = await prisma.room.findMany({
      select: { totalCapacity: true, occupiedCount: true }
    });

    const totalRooms = rooms.length;
    const totalBeds = rooms.reduce((sum, r) => sum + r.totalCapacity, 0);
    const occupiedBeds = rooms.reduce((sum, r) => sum + r.occupiedCount, 0);

    const payments = await prisma.payment.findMany({
      where: { status: 'SUCCESS' },
      select: { amountPaid: true }
    });
    const totalRevenue = payments.reduce((sum, p) => sum + p.amountPaid, 0);

    const pendingComplaints = await prisma.complaint.count({
      where: { status: { in: [ComplaintStatusEnum.OPEN, ComplaintStatusEnum.IN_PROGRESS] } }
    });

    return {
      totalHostels,
      totalStudents,
      totalRooms,
      totalBeds,
      occupiedBeds,
      totalRevenue,
      pendingComplaints
    };
  }
}
