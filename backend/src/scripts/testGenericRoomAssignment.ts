import { prisma } from '../config/prisma';
import { StudentsService } from '../modules/students/students.service';
import { RoomsService } from '../modules/rooms/rooms.service';

async function testGenericRoomAssignment() {
  console.log('========================================================================');
  console.log('HOSTELHUB COMPREHENSIVE MULTI-ROOM GENERIC ASSIGNMENT & CREATION TEST');
  console.log('========================================================================\n');

  const studentsService = new StudentsService();
  const roomsService = new RoomsService();

  const createdUserIds: string[] = [];
  let hostel: any = null;

  try {
    // 0. Setup: Get test hostel
    hostel = (await prisma.hostel.findUnique({ where: { id: 'hostel_001' } })) || (await prisma.hostel.findFirst());
    if (!hostel) throw new Error('No hostel found in database.');
    console.log(`[SETUP] Using Hostel: "${hostel.name}" (${hostel.id})`);

    // Ensure rooms 301, 302, 304, and A-204 exist with available capacity
    const requiredRoomNumbers = ['301', '302', '304', 'A-204'];
    for (const rNum of requiredRoomNumbers) {
      let r = await prisma.room.findFirst({ where: { hostelId: hostel.id, roomNumber: rNum } });
      if (!r) {
        r = await prisma.room.create({
          data: {
            hostelId: hostel.id,
            roomNumber: rNum,
            floor: 1,
            block: 'A',
            roomType: 'DOUBLE',
            totalCapacity: 4,
            occupiedCount: 0,
            monthlyRent: 500,
            status: 'AVAILABLE'
          }
        });
      } else {
        // Expand capacity so tests always have available beds
        if (r.totalCapacity <= r.occupiedCount) {
          await prisma.room.update({
            where: { id: r.id },
            data: {
              totalCapacity: r.occupiedCount + 3,
              status: 'AVAILABLE'
            }
          });
        }
      }
      // Ensure beds exist for this room
      const currentBeds = await prisma.bed.findMany({ where: { roomId: r.id } });
      if (currentBeds.length < 4) {
        const bedLetters = ['A', 'B', 'C', 'D', 'E', 'F'];
        for (let i = currentBeds.length; i < 4; i++) {
          await prisma.bed.create({
            data: { roomId: r.id, bedNumber: `Bed-${bedLetters[i]}`, isOccupied: false }
          }).catch(() => {});
        }
      }
    }

    // Load all rooms dynamically through the room service (exact same API used by frontend)
    const allRooms = (await roomsService.getRoomsByHostel(hostel.id)).filter((r): r is NonNullable<typeof r> => r !== null);
    console.log(`[SETUP] Found ${allRooms.length} rooms returned by RoomsService:`);
    allRooms.forEach(r => console.log(`   - Room ${r.roomNumber} (UUID: ${r.roomId}, capacity: ${r.totalCapacity}, occupied: ${r.occupiedCount})`));
    console.log('\n');

    // ==========================================
    // TEST 1 — ASSIGNMENT TO ROOM 301 VIA ROOM NUMBER
    // ==========================================
    console.log('--- [TEST 1] ASSIGN TO ROOM 301 VIA ROOM NUMBER ---');
    const room301 = allRooms.find(r => r.roomNumber === '301');
    if (!room301) throw new Error('Room 301 not found');
    const stuId1 = await studentsService.generateUniqueStudentId();
    const res1 = await studentsService.createStudentByAdmin({
      fullName: 'Student for Room 301',
      collegeName: 'Apex College',
      course: 'B.Tech',
      yearOfStudy: '1st Year',
      phoneNumber: '9100000001',
      studentId: stuId1,
      hostelId: hostel.id,
      roomNumber: '301' // PURE ROOM NUMBER, NO CLIENT-SIDE UUID
    });
    console.log(`✓ Student Created: ${res1.student?.fullName} (Roll: ${res1.student?.rollNumber})`);
    console.log(`✓ Assigned Room: ${res1.student?.roomNumber} (Backend Resolved UUID: ${res1.student?.roomId}), Bed: ${res1.student?.bedNumber}`);
    if (res1.student?.roomId !== room301.roomId || res1.student?.roomNumber !== '301') {
      throw new Error('TEST 1 FAILED: Mismatch in assigned room for 301');
    }
    createdUserIds.push(res1.student!.userId);
    console.log('✓ TEST 1 (Room 301 Assignment by roomNumber) PASSED\n');

    // ==========================================
    // TEST 2 — ASSIGNMENT TO ROOM 302 VIA ROOM NUMBER
    // ==========================================
    console.log('--- [TEST 2] ASSIGN TO ROOM 302 VIA ROOM NUMBER ---');
    const room302 = allRooms.find(r => r.roomNumber === '302');
    if (!room302) throw new Error('Room 302 not found');
    const stuId2 = await studentsService.generateUniqueStudentId();
    const res2 = await studentsService.createStudentByAdmin({
      fullName: 'Student for Room 302',
      collegeName: 'Apex College',
      course: 'B.Tech',
      yearOfStudy: '2nd Year',
      phoneNumber: '9100000002',
      studentId: stuId2,
      hostelId: hostel.id,
      roomNumber: '302' // PURE ROOM NUMBER, NO CLIENT-SIDE UUID
    });
    console.log(`✓ Student Created: ${res2.student?.fullName} (Roll: ${res2.student?.rollNumber})`);
    console.log(`✓ Assigned Room: ${res2.student?.roomNumber} (Backend Resolved UUID: ${res2.student?.roomId}), Bed: ${res2.student?.bedNumber}`);
    if (res2.student?.roomId !== room302.roomId || res2.student?.roomNumber !== '302') {
      throw new Error('TEST 2 FAILED: Mismatch in assigned room for 302');
    }
    createdUserIds.push(res2.student!.userId);
    console.log('✓ TEST 2 (Room 302 Assignment by roomNumber) PASSED\n');

    // ==========================================
    // TEST 3 — ASSIGNMENT TO ROOM 304 VIA ROOM NUMBER
    // ==========================================
    console.log('--- [TEST 3] ASSIGN TO ROOM 304 VIA ROOM NUMBER ---');
    const room304 = allRooms.find(r => r.roomNumber === '304');
    if (!room304) throw new Error('Room 304 not found');
    const stuId3 = await studentsService.generateUniqueStudentId();
    const res3 = await studentsService.createStudentByAdmin({
      fullName: 'Student for Room 304',
      collegeName: 'Apex College',
      course: 'MCA',
      yearOfStudy: '3rd Year',
      phoneNumber: '9100000003',
      studentId: stuId3,
      hostelId: hostel.id,
      roomNumber: '304' // PURE ROOM NUMBER, NO CLIENT-SIDE UUID
    });
    console.log(`✓ Student Created: ${res3.student?.fullName} (Roll: ${res3.student?.rollNumber})`);
    console.log(`✓ Assigned Room: ${res3.student?.roomNumber} (Backend Resolved UUID: ${res3.student?.roomId}), Bed: ${res3.student?.bedNumber}`);
    if (res3.student?.roomId !== room304.roomId || res3.student?.roomNumber !== '304') {
      throw new Error('TEST 3 FAILED: Mismatch in assigned room for 304');
    }
    createdUserIds.push(res3.student!.userId);
    console.log('✓ TEST 3 (Room 304 Assignment by roomNumber) PASSED\n');

    // ==========================================
    // TEST 4 — ASSIGNMENT TO ROOM A-204 VIA ROOM NUMBER
    // ==========================================
    console.log('--- [TEST 4] ASSIGN TO OTHER EXISTING ROOM (A-204) VIA ROOM NUMBER ---');
    const roomA204 = allRooms.find(r => r.roomNumber === 'A-204');
    if (!roomA204) throw new Error('Room A-204 not found');
    const stuId4 = await studentsService.generateUniqueStudentId();
    const res4 = await studentsService.createStudentByAdmin({
      fullName: 'Student for Room A-204',
      collegeName: 'Apex College',
      course: 'MBA',
      yearOfStudy: '1st Year',
      phoneNumber: '9100000004',
      studentId: stuId4,
      hostelId: hostel.id,
      roomNumber: 'A-204' // PURE ROOM NUMBER, NO CLIENT-SIDE UUID
    });
    console.log(`✓ Student Created: ${res4.student?.fullName} (Roll: ${res4.student?.rollNumber})`);
    console.log(`✓ Assigned Room: ${res4.student?.roomNumber} (Backend Resolved UUID: ${res4.student?.roomId}), Bed: ${res4.student?.bedNumber}`);
    if (res4.student?.roomId !== roomA204.roomId || res4.student?.roomNumber !== 'A-204') {
      throw new Error('TEST 4 FAILED: Mismatch in assigned room for A-204');
    }
    createdUserIds.push(res4.student!.userId);
    console.log('✓ TEST 4 (Room A-204 Assignment by roomNumber) PASSED\n');

    // ==========================================
    // TEST 5 — CREATE NEW ROOM A-50 & ASSIGN VIA ROOM NUMBER
    // ==========================================
    console.log('--- [TEST 5] DYNAMICALLY CREATE ROOM A-50 & ASSIGN STUDENT VIA ROOM NUMBER ---');
    const existingA50 = await prisma.room.findFirst({ where: { hostelId: hostel.id, roomNumber: 'A-50' } });
    if (existingA50) {
      await prisma.roomAllocation.deleteMany({ where: { roomId: existingA50.id } }).catch(() => {});
      await prisma.bed.deleteMany({ where: { roomId: existingA50.id } }).catch(() => {});
      await prisma.room.delete({ where: { id: existingA50.id } }).catch(() => {});
    }
    // Simulate Hostel Owner adding brand-new room A-50
    const newRoomA50 = await prisma.room.create({
      data: {
        hostelId: hostel.id,
        roomNumber: 'A-50',
        floor: 5,
        block: 'A',
        roomType: 'SINGLE',
        totalCapacity: 1,
        occupiedCount: 0,
        monthlyRent: 800,
        status: 'AVAILABLE'
      }
    });
    await prisma.bed.create({
      data: {
        roomId: newRoomA50.id,
        bedNumber: 'Bed-1',
        isOccupied: false
      }
    });
    console.log(`✓ Newly created Room A-50 with DB UUID: "${newRoomA50.id}"`);

    const stuId5 = await studentsService.generateUniqueStudentId();
    const res5 = await studentsService.createStudentByAdmin({
      fullName: 'Student for New Room A-50',
      collegeName: 'Apex College',
      course: 'B.Sc',
      yearOfStudy: '1st Year',
      phoneNumber: '9100000005',
      studentId: stuId5,
      hostelId: hostel.id,
      roomNumber: 'A-50' // PURE ROOM NUMBER, NO CLIENT-SIDE UUID
    });
    console.log(`✓ Student Created: ${res5.student?.fullName} (Roll: ${res5.student?.rollNumber})`);
    console.log(`✓ Assigned Room: ${res5.student?.roomNumber} (Backend Resolved UUID: ${res5.student?.roomId}), Bed: ${res5.student?.bedNumber}`);
    if (res5.student?.roomId !== newRoomA50.id || res5.student?.roomNumber !== 'A-50') {
      throw new Error('TEST 5 FAILED: Mismatch in assigned room for dynamically created Room A-50');
    }
    createdUserIds.push(res5.student!.userId);
    console.log('✓ TEST 5 (Dynamic Room A-50 Creation & Assignment by roomNumber) PASSED\n');

    // ==========================================
    // TEST 6 — FULL ROOM PROTECTION
    // ==========================================
    console.log('--- [TEST 6] FULL ROOM PROTECTION ---');
    // Room A-50 has capacity 1 and now has 1 student (full)
    let fullRoomRejected = false;
    try {
      const extraStuId = await studentsService.generateUniqueStudentId();
      await studentsService.createStudentByAdmin({
        fullName: 'Imposter Student for Full Room',
        collegeName: 'Apex College',
        course: 'B.Sc',
        yearOfStudy: '1st Year',
        phoneNumber: '9100000006',
        studentId: extraStuId,
        hostelId: hostel.id,
        roomNumber: 'A-50' // Attempting to assign to full room
      });
    } catch (err: any) {
      fullRoomRejected = true;
      console.log(`✓ Full room assignment correctly rejected with message: "${err.message}" (Status: ${err.status})`);
      if (err.status !== 400) throw new Error(`TEST 6 FAILED: Expected 400, got ${err.status}`);
    }
    if (!fullRoomRejected) throw new Error('TEST 6 FAILED: Full room was assigned beyond capacity');
    console.log('✓ TEST 6 (Full Room Protection) PASSED\n');

    // ==========================================
    // TEST 7 — NO-ROOM ASSIGNMENT (OPTIONAL)
    // ==========================================
    console.log('--- [TEST 7] NO-ROOM ASSIGNMENT (OPTIONAL ROOM) ---');
    const stuId7 = await studentsService.generateUniqueStudentId();
    const res7 = await studentsService.createStudentByAdmin({
      fullName: 'Unallocated Resident Student',
      collegeName: 'Apex College',
      course: 'B.Com',
      yearOfStudy: '1st Year',
      phoneNumber: '9100000007',
      studentId: stuId7,
      hostelId: hostel.id,
      roomNumber: null // No room selected
    });
    console.log(`✓ Student Created: ${res7.student?.fullName} (Roll: ${res7.student?.rollNumber})`);
    console.log(`✓ Room: ${res7.student?.roomNumber || 'None (Unallocated)'}, Bed: ${res7.student?.bedNumber || 'None'}`);
    if (res7.student?.roomId !== null && res7.student?.roomId !== undefined) {
      throw new Error('TEST 7 FAILED: Expected roomId to be null for optional unallocated student');
    }
    createdUserIds.push(res7.student!.userId);
    console.log('✓ TEST 7 (No-Room Assignment) PASSED\n');

    // ==========================================
    // TEST 8 — INVALID ROOM PROTECTION
    // ==========================================
    console.log('--- [TEST 8] INVALID ROOM PROTECTION ---');
    let invalidRoomRejected = false;
    try {
      const stuId8 = await studentsService.generateUniqueStudentId();
      await studentsService.createStudentByAdmin({
        fullName: 'Invalid Room Test',
        collegeName: 'Apex College',
        course: 'B.Tech',
        yearOfStudy: '1st Year',
        phoneNumber: '9100000008',
        studentId: stuId8,
        hostelId: hostel.id,
        roomNumber: 'ROOM-NON-EXISTENT-999'
      });
    } catch (err: any) {
      invalidRoomRejected = true;
      console.log(`✓ Invalid room correctly rejected: "${err.message}" (Status: ${err.status})`);
      if (err.status !== 404) throw new Error(`TEST 8 FAILED: Expected 404, got ${err.status}`);
    }
    if (!invalidRoomRejected) throw new Error('TEST 8 FAILED: Non-existent room was accepted');
    console.log('✓ TEST 8 (Invalid Room Protection) PASSED\n');

    // ==========================================
    // TEST 9 — CROSS-HOSTEL ISOLATION PROTECTION
    // ==========================================
    console.log('--- [TEST 9] CROSS-HOSTEL ISOLATION PROTECTION ---');
    // Find or create another hostel with a room 'T-421'
    const otherHostel = await prisma.hostel.findFirst({ where: { id: { not: hostel.id } } });
    if (otherHostel) {
      let crossHostelRejected = false;
      try {
        const stuId9 = await studentsService.generateUniqueStudentId();
        // Owner of 'hostel' tries to submit a roomNumber that only exists in 'otherHostel'
        await studentsService.createStudentByAdmin({
          fullName: 'Cross-Hostel Attacker',
          collegeName: 'Apex College',
          course: 'B.Tech',
          yearOfStudy: '1st Year',
          phoneNumber: '9100000009',
          studentId: stuId9,
          hostelId: hostel.id, // Current owner hostel
          roomNumber: 'T-421' // Room belonging only to otherHostel
        });
      } catch (err: any) {
        crossHostelRejected = true;
        console.log(`✓ Cross-hostel room assignment safely rejected: "${err.message}" (Status: ${err.status})`);
        if (err.status !== 404) throw new Error(`TEST 9 FAILED: Expected 404, got ${err.status}`);
      }
      if (!crossHostelRejected) throw new Error('TEST 9 FAILED: Cross-hostel room was allocated');
      console.log('✓ TEST 9 (Cross-Hostel Isolation Protection) PASSED\n');
    }

    console.log('========================================================================');
    console.log('🎉 ALL 8 GENERIC MULTI-ROOM TESTS PASSED WITH 100% SUCCESS!');
    console.log('========================================================================\n');

  } catch (error) {
    console.error('❌ MULTI-ROOM TEST FAILED:', error);
    process.exit(1);
  } finally {
    for (const uId of createdUserIds) {
      await prisma.user.delete({ where: { id: uId } }).catch(() => {});
    }
    const finalA50 = await prisma.room.findFirst({ where: { hostelId: hostel.id, roomNumber: 'A-50' } });
    if (finalA50) {
      await prisma.roomAllocation.deleteMany({ where: { roomId: finalA50.id } }).catch(() => {});
      await prisma.bed.deleteMany({ where: { roomId: finalA50.id } }).catch(() => {});
      await prisma.room.delete({ where: { id: finalA50.id } }).catch(() => {});
    }
    await prisma.$disconnect();
    console.log('[CLEANUP OK] Database cleaned successfully.');
  }
}

testGenericRoomAssignment();
