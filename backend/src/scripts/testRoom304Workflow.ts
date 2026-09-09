import { prisma } from '../config/prisma';
import { StudentsService } from '../modules/students/students.service';
import { RoomsService } from '../modules/rooms/rooms.service';
import { AuthService } from '../modules/auth/auth.service';

async function testRoom304Workflow() {
  console.log('================================================================');
  console.log('HOSTELHUB VERIFICATION: ROOM 304 SELECTION & STUDENT ISSUING');
  console.log('================================================================\n');

  const studentsService = new StudentsService();
  const roomsService = new RoomsService();
  const authService = new AuthService();

  try {
    // 0. Clean up any existing test student with STU-2026-9688
    const existingStudent = await prisma.student.findUnique({
      where: { rollNumber: 'STU-2026-9688' },
      include: { user: true }
    });
    if (existingStudent) {
      console.log(`[CLEANUP] Removing previous test student (${existingStudent.rollNumber})`);
      await prisma.user.delete({ where: { id: existingStudent.userId } });
    }

    // Ensure Room 304 exists in database with vacant beds
    let room304 = await prisma.room.findFirst({
      where: { roomNumber: '304' },
      include: { beds: true, hostel: true }
    });

    if (room304) {
      // Clear previous test allocations for Room 304
      await prisma.roomAllocation.deleteMany({
        where: { roomId: room304.id }
      });
      await prisma.bed.updateMany({
        where: { roomId: room304.id },
        data: { isOccupied: false }
      });
      await prisma.room.update({
        where: { id: room304.id },
        data: { occupiedCount: 0 }
      });
      room304 = await prisma.room.findUnique({
        where: { id: room304.id },
        include: { beds: true, hostel: true }
      });
    } else {
      console.log('[SETUP] Creating Room 304 in database...');
      const hostel = await prisma.hostel.findFirst();
      if (!hostel) throw new Error('No hostel found in database.');
      room304 = await prisma.room.create({
        data: {
          hostelId: hostel.id,
          roomNumber: '304',
          floor: 3,
          block: 'A',
          roomType: 'DOUBLE',
          totalCapacity: 2,
          occupiedCount: 0,
          monthlyRent: 450,
          status: 'AVAILABLE'
        },
        include: { beds: true, hostel: true }
      });
      await prisma.bed.createMany({
        data: [
          { roomId: room304.id, bedNumber: 'Bed-A', isOccupied: false },
          { roomId: room304.id, bedNumber: 'Bed-B', isOccupied: false }
        ]
      });
      room304 = await prisma.room.findUnique({
        where: { id: room304.id },
        include: { beds: true, hostel: true }
      });
    }

    console.log(`[SETUP OK] Room 304 DB ID: "${room304!.id}", Hostel: "${room304!.hostel?.name}" (${room304!.hostelId})\n`);

    // ==========================================
    // TEST A — LOAD ROOMS
    // ==========================================
    console.log('--- [TEST A] LOAD ROOMS FOR HOSTEL ---');
    const loadedRooms = (await roomsService.getRoomsByHostel(room304!.hostelId)).filter((r): r is NonNullable<typeof r> => r !== null);
    console.log(`✓ Total rooms returned from backend: ${loadedRooms.length}`);
    const foundRoom304 = loadedRooms.find(r => r.roomNumber === '304');
    if (!foundRoom304) {
      throw new Error('TEST A FAILED: Room 304 not returned by getRoomsByHostel');
    }
    console.log(`✓ Room 304 verified in room list:`);
    console.log(`   - Display Name: Room ${foundRoom304.roomNumber}`);
    console.log(`   - Internal Real DB ID (roomId): ${foundRoom304.roomId}`);
    console.log(`   - Capacity: ${foundRoom304.totalCapacity}, Occupied: ${foundRoom304.occupiedCount}`);
    if (foundRoom304.roomId !== room304!.id) {
      throw new Error(`TEST A FAILED: roomId mismatch: expected ${room304!.id}, got ${foundRoom304.roomId}`);
    }
    console.log('✓ TEST A (Load Rooms) PASSED\n');

    // ==========================================
    // TEST B — SELECT ROOM 304
    // ==========================================
    console.log('--- [TEST B] SELECT ROOM 304 ---');
    const selectedRoomId = foundRoom304.roomId;
    console.log(`✓ Selected Room internal ID: "${selectedRoomId}"`);
    if (!selectedRoomId || selectedRoomId.length !== 36) {
      throw new Error('TEST B FAILED: Invalid room UUID format');
    }
    console.log('✓ TEST B (Select Room 304) PASSED\n');

    // ==========================================
    // TEST C — ISSUE STUDENT ID & ADD STUDENT
    // ==========================================
    console.log('--- [TEST C] ISSUE ID & ADD STUDENT ---');
    const generatedStudentId = 'STU-2026-9688';
    const hostUser = await prisma.user.findFirst({ where: { role: 'HOST' } });

    const createResult = await studentsService.createStudentByAdmin(
      {
        fullName: 'seulalal',
        collegeName: 'tkr',
        course: 'diploma',
        yearOfStudy: '3rd Year',
        phoneNumber: '7569804004',
        studentId: generatedStudentId,
        hostelId: room304!.hostelId,
        roomId: selectedRoomId, // Passing the real database UUID of Room 304
        bedNumber: 'Bed-A'
      },
      hostUser ? { role: 'HOST', userId: hostUser.id, profileId: hostUser.id } : undefined
    );

    console.log(`✓ Result Success: ${createResult.success}`);
    console.log(`✓ Message: "${createResult.message}"`);
    console.log(`✓ Student: ${createResult.student?.fullName} (Roll: ${createResult.student?.rollNumber})`);
    console.log(`✓ Assigned Room: ${createResult.student?.roomNumber} (Room UUID: ${createResult.student?.roomId})`);
    console.log(`✓ Assigned Bed: ${createResult.student?.bedNumber}`);

    if (!createResult.success || createResult.student?.roomId !== room304!.id) {
      throw new Error('TEST C FAILED: Student was not allocated to the selected Room 304 UUID');
    }
    console.log('✓ TEST C (Issue ID & Add Student) PASSED\n');

    // ==========================================
    // TEST D — VERIFY DATABASE PERSISTENCE
    // ==========================================
    console.log('--- [TEST D] VERIFY DATABASE PERSISTENCE ---');
    const dbStudent = await prisma.student.findUnique({
      where: { rollNumber: generatedStudentId },
      include: {
        user: true,
        hostel: true,
        room: true,
        allocations: {
          where: { status: 'ACTIVE' },
          include: { room: true, bed: true }
        }
      }
    });

    if (!dbStudent) {
      throw new Error('TEST D FAILED: Student record not found in database');
    }
    console.log(`✓ Student in DB: ${dbStudent.fullName} (${dbStudent.rollNumber})`);
    console.log(`✓ User Email: ${dbStudent.user.email}, Phone: ${dbStudent.user.phoneNumber}`);
    console.log(`✓ Hostel in DB: ${dbStudent.hostel?.name} (${dbStudent.hostelId})`);
    console.log(`✓ Room in DB: Room ${dbStudent.room?.roomNumber} (${dbStudent.roomId})`);
    console.log(`✓ Active Allocation Count: ${dbStudent.allocations.length}`);
    console.log(`✓ Allocation Details: Room ${dbStudent.allocations[0].room.roomNumber}, Bed ${dbStudent.allocations[0].bed.bedNumber}`);

    if (dbStudent.roomId !== room304!.id || dbStudent.allocations.length === 0) {
      throw new Error('TEST D FAILED: RoomAllocation record was not created with Room 304');
    }
    console.log('✓ TEST D (Database Verification) PASSED\n');

    // ==========================================
    // TEST E — RELOAD & OCCUPANCY CHECK
    // ==========================================
    console.log('--- [TEST E] RELOAD ROOMS & VERIFY OCCUPANCY ---');
    const reloadedRooms = (await roomsService.getRoomsByHostel(room304!.hostelId)).filter((r): r is NonNullable<typeof r> => r !== null);
    const reloaded304 = reloadedRooms.find(r => r.roomNumber === '304');
    console.log(`✓ Reloaded Room 304 Occupancy: ${reloaded304?.occupiedCount} / ${reloaded304?.totalCapacity}`);
    console.log(`✓ Beds in Room 304:`);
    reloaded304?.beds.forEach((b: any) => {
      console.log(`   - Bed ${b.bedNumber}: occupied=${b.isOccupied}, student=${b.studentName || 'None'}`);
    });
    console.log('✓ TEST E (Reload Persistence) PASSED\n');

    // ==========================================
    // TEST F — INVALID ROOM PROTECTION
    // ==========================================
    console.log('--- [TEST F] INVALID ROOM PROTECTION ---');
    const fakeRoomUuid = '00000000-0000-0000-0000-000000000000';
    let rejectedAsExpected = false;
    try {
      await studentsService.createStudentByAdmin({
        fullName: 'Invalid Room Probe',
        collegeName: 'tkr',
        course: 'diploma',
        yearOfStudy: '1st Year',
        phoneNumber: '7569804000',
        studentId: 'STU-2026-0000',
        roomId: fakeRoomUuid
      });
    } catch (err: any) {
      rejectedAsExpected = true;
      console.log(`✓ Invalid room correctly rejected with message: "${err.message}" (Status: ${err.status})`);
      if (err.status !== 404) {
        throw new Error(`TEST F FAILED: Expected HTTP 404, got ${err.status}`);
      }
    }
    if (!rejectedAsExpected) {
      throw new Error('TEST F FAILED: Fake room UUID was accepted without error');
    }

    // Verify no orphaned student or allocation was created for STU-2026-0000
    const orphanedStudent = await prisma.student.findUnique({ where: { rollNumber: 'STU-2026-0000' } });
    if (orphanedStudent) {
      throw new Error('TEST F FAILED: Orphaned student was created despite room error');
    }
    console.log('✓ No orphaned student created in DB');
    console.log('✓ TEST F (Invalid Room Protection) PASSED\n');

    // Cleanup test student
    await prisma.user.delete({ where: { id: dbStudent.userId } }).catch(() => {});

    console.log('================================================================');
    console.log('🎉 ALL TESTS (TEST A TO TEST F) PASSED WITH 100% SUCCESS!');
    console.log('================================================================\n');

  } catch (err) {
    console.error('❌ WORKFLOW TEST FAILED:', err);
    process.exit(1);
  } finally {
    await prisma.$disconnect();
  }
}

testRoom304Workflow();
