import { prisma } from '../config/prisma';
import { StudentsService } from '../modules/students/students.service';

async function testCreatorHost() {
  console.log('=== TESTING CREATOR HOST RESOLUTION ===');
  const service = new StudentsService();

  // Clean up any test user created previously with rollNumber STU-2026-9688
  const prevStudent = await prisma.student.findUnique({
    where: { rollNumber: 'STU-2026-9688' },
    include: { user: true }
  });
  if (prevStudent) {
    console.log(`Cleaning up previous test student (${prevStudent.id}, user: ${prevStudent.userId})`);
    await prisma.user.delete({ where: { id: prevStudent.userId } });
  }

  // Find the host users in DB
  const hosts = await prisma.host.findMany({
    include: { hostels: true, user: true }
  });
  console.log(`Found ${hosts.length} hosts in DB:`);
  for (const h of hosts) {
    console.log(`- Host ID: ${h.id}, User ID: ${h.userId}, Email: ${h.user?.email}`);
    console.log(`  Hostels: ${h.hostels.map(x => `${x.name} (${x.id})`).join(', ')}`);
  }

  const room304 = await prisma.room.findFirst({
    where: { roomNumber: '304' }
  });
  console.log('\nRoom 304 in DB:', room304?.id, 'Hostel:', room304?.hostelId);

  // For each host, try creating student in Room 304
  for (const h of hosts) {
    console.log(`\n--- Testing with Host: ${h.user?.email} (${h.id}) ---`);
    try {
      const res = await service.createStudentByAdmin(
        {
          fullName: 'Seulalal Host Test',
          collegeName: 'tkr',
          course: 'diploma',
          yearOfStudy: '3rd Year',
          phoneNumber: '7569804004',
          studentId: `STU-2026-${Math.floor(1000 + Math.random() * 9000)}`,
          hostelId: '', // as sent by Android when student.hostelId was ""
          roomId: room304!.id
        },
        { role: 'HOST', profileId: h.id, userId: h.userId }
      );
      console.log(`✅ SUCCESS for host ${h.user?.email}: Student ID ${res.student?.rollNumber}, Hostel: ${res.student?.hostelId}`);
      // cleanup
      await prisma.user.delete({ where: { id: res.student!.userId } });
    } catch (err: any) {
      console.error(`❌ FAILED for host ${h.user?.email}:`, err);
    }
  }
}

testCreatorHost().finally(() => prisma.$disconnect());
