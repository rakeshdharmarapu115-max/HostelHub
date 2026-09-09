import { prisma } from '../config/prisma';
import { StudentsService } from '../modules/students/students.service';

async function testRoom304() {
  console.log('=== TESTING ROOM 304 LOOKUP & CREATION ===');
  const service = new StudentsService();

  const room304 = await prisma.room.findFirst({
    where: { roomNumber: '304' }
  });
  console.log('Room 304 in DB:', room304);

  if (room304) {
    try {
      console.log(`Calling createStudentByAdmin with roomId="${room304.id}"...`);
      const res = await service.createStudentByAdmin({
        fullName: 'Test Student Seulalal',
        collegeName: 'tkr',
        course: 'diploma',
        yearOfStudy: '3rd Year',
        phoneNumber: '7569804004',
        studentId: 'STU-2026-9688',
        roomId: room304.id
      });
      console.log('SUCCESS:', res);
    } catch (err) {
      console.error('ERROR during createStudentByAdmin:', err);
    }
  }
}

testRoom304().finally(() => prisma.$disconnect());
