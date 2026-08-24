import { prisma } from '../../config/prisma';
import { AttendanceStatusEnum } from '../../types/enums';

export class AttendanceService {
  async getAttendanceForStudent(studentIdOrUserId: string, month?: number, year?: number) {
    const student = await prisma.student.findFirst({
      where: {
        OR: [{ id: studentIdOrUserId }, { userId: studentIdOrUserId }]
      }
    });

    if (!student) {
      return [];
    }

    let records = await prisma.attendanceRecord.findMany({
      where: { studentId: student.id },
      orderBy: { date: 'desc' }
    });

    if (month && year) {
      const monthStr = month.toString().padStart(2, '0');
      const prefix = `${year}-${monthStr}`;
      records = records.filter(r => r.date.startsWith(prefix));
    }

    return records.map(r => this.mapAttendance(r));
  }

  async getAttendanceForHostel(hostelId: string, date?: string) {
    const where: any = { hostelId };
    if (date) {
      where.date = date;
    }

    const records = await prisma.attendanceRecord.findMany({
      where,
      orderBy: { studentName: 'asc' }
    });

    return records.map(r => this.mapAttendance(r));
  }

  async markAttendance(data: {
    hostelId?: string;
    studentId: string;
    studentName?: string;
    roomNumber?: string;
    date: string;
    status: AttendanceStatusEnum;
    checkInTime?: number;
    remarks?: string;
    markedBy?: string;
  }) {
    const student = await prisma.student.findFirst({
      where: {
        OR: [{ id: data.studentId }, { userId: data.studentId }]
      }
    });

    if (!student) {
      throw { status: 404, message: `Student not found for ID: ${data.studentId}` };
    }

    const hostelId = data.hostelId || student.hostelId || 'hostel_001';
    const studentName = data.studentName || student.fullName;
    const roomNumber = data.roomNumber || student.roomNumber || 'A-204';

    const record = await prisma.attendanceRecord.upsert({
      where: {
        studentId_date: {
          studentId: student.id,
          date: data.date
        }
      },
      update: {
        status: data.status,
        checkInTime: data.checkInTime ? new Date(data.checkInTime) : new Date(),
        remarks: data.remarks,
        markedBy: data.markedBy || 'STUDENT_SELF'
      },
      create: {
        hostelId,
        studentId: student.id,
        studentName,
        roomNumber,
        date: data.date,
        status: data.status,
        checkInTime: data.checkInTime ? new Date(data.checkInTime) : new Date(),
        remarks: data.remarks,
        markedBy: data.markedBy || 'STUDENT_SELF'
      }
    });

    return this.mapAttendance(record);
  }

  async markBatchAttendance(records: Array<{
    hostelId?: string;
    studentId: string;
    studentName?: string;
    roomNumber?: string;
    date: string;
    status: AttendanceStatusEnum;
    remarks?: string;
    markedBy?: string;
  }>) {
    const results = [];
    for (const item of records) {
      const res = await this.markAttendance(item);
      results.push(res);
    }
    return results;
  }

  private mapAttendance(r: any) {
    return {
      attendanceId: r.id,
      hostelId: r.hostelId,
      studentId: r.studentId,
      studentName: r.studentName,
      roomNumber: r.roomNumber,
      date: r.date,
      status: r.status,
      checkInTime: r.checkInTime ? r.checkInTime.getTime() : null,
      remarks: r.remarks,
      markedBy: r.markedBy,
      leaveRequestId: r.leaveRequestId,
      createdAt: r.createdAt.getTime()
    };
  }
}
