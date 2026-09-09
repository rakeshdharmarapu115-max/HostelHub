import { prisma } from '../config/prisma';
import { StudentsService } from '../modules/students/students.service';
import { RoomsService } from '../modules/rooms/rooms.service';
import { AuthService } from '../modules/auth/auth.service';

async function runComprehensiveVerification() {
  console.log('========================================================================');
  console.log('🔍 COMPREHENSIVE HOSTELHUB STUDENT ID LIFECYCLE & MULTI-ROOM AUDIT');
  console.log('========================================================================\n');

  const studentsService = new StudentsService();
  const roomsService = new RoomsService();
  const authService = new AuthService();

  const createdUserIds: string[] = [];
  const createdRoomIds: string[] = [];

  try {
    // 0. Locate Primary Hostel
    const hostels = await prisma.hostel.findMany();
    if (hostels.length === 0) throw new Error('No hostels found in database.');
    const primaryHostel = hostels.find(h => h.id === 'hostel_001') || hostels[0];
    console.log(`[SETUP] Primary Hostel: "${primaryHostel.name}" (ID: ${primaryHostel.id})\n`);

    // ========================================================================
    // TEST 1: STUDENT ID GENERATION FORMAT & UNIQUENESS
    // ========================================================================
    console.log('--- [TEST 1] UNIQUE STUDENT ID GENERATION ---');
    const id1 = await studentsService.generateUniqueStudentId();
    console.log(`✓ Generated Student ID: ${id1}`);
    const regex = /^STU-\d{4}-\d{4}$/;
    if (!regex.test(id1)) {
      throw new Error(`TEST 1 FAILED: Generated ID ${id1} does not match STU-YYYY-XXXX format`);
    }
    console.log('✓ TEST 1 PASSED: Unique Student ID format valid.\n');

    // ========================================================================
    // TEST 2: CREATE & ASSIGN ROOM 301
    // ========================================================================
    console.log('--- [TEST 2] CREATE STUDENT & ASSIGN TO ROOM 301 ---');
    let room301 = await prisma.room.findFirst({
      where: { hostelId: primaryHostel.id, roomNumber: '301' },
      include: { beds: true }
    });
    if (!room301) {
      room301 = await prisma.room.create({
        data: {
          hostelId: primaryHostel.id,
          roomNumber: '301',
          floor: 3,
          block: 'A',
          roomType: 'DOUBLE',
          totalCapacity: 4,
          occupiedCount: 0,
          monthlyRent: 4500,
          status: 'AVAILABLE'
        },
        include: { beds: true }
      });
      createdRoomIds.push(room301.id);
    }
    // Ensure room 301 has capacity
    await prisma.room.update({
      where: { id: room301.id },
      data: {
        totalCapacity: Math.max(room301.totalCapacity, room301.occupiedCount + 2),
        status: 'AVAILABLE'
      }
    });

    const studentId1 = await studentsService.generateUniqueStudentId();
    const studentRes1 = await studentsService.createStudentByAdmin({
      fullName: 'Rajesh Kumar',
      collegeName: 'National Tech University',
      course: 'B.Tech Mechanical',
      yearOfStudy: '1st Year',
      phoneNumber: '9876543210',
      studentId: studentId1,
      hostelId: primaryHostel.id,
      roomNumber: '301'
    });

    console.log(`✓ Student Created: ${studentRes1.student?.fullName} (${studentRes1.student?.rollNumber})`);
    console.log(`✓ Assigned Room: ${studentRes1.student?.roomNumber} (ID: ${studentRes1.student?.roomId}), Bed: ${studentRes1.student?.bedNumber}`);
    if (studentRes1.student?.roomId !== room301.id || studentRes1.student?.roomNumber !== '301') {
      throw new Error(`TEST 2 FAILED: Room assignment mismatch for 301`);
    }
    createdUserIds.push(studentRes1.student!.userId);
    console.log('✓ TEST 2 PASSED: Room 301 assigned and persisted.\n');

    // ========================================================================
    // TEST 3: CREATE & ASSIGN ROOM 302 (VACANT ROOM)
    // ========================================================================
    console.log('--- [TEST 3] CREATE STUDENT & ASSIGN TO ROOM 302 ---');
    let room302 = await prisma.room.findFirst({
      where: { hostelId: primaryHostel.id, roomNumber: '302' },
      include: { beds: true }
    });
    if (!room302) {
      room302 = await prisma.room.create({
        data: {
          hostelId: primaryHostel.id,
          roomNumber: '302',
          floor: 3,
          block: 'A',
          roomType: 'DOUBLE',
          totalCapacity: 2,
          occupiedCount: 0,
          monthlyRent: 4500,
          status: 'AVAILABLE'
        },
        include: { beds: true }
      });
      createdRoomIds.push(room302.id);
    }
    await prisma.room.update({
      where: { id: room302.id },
      data: {
        totalCapacity: Math.max(room302.totalCapacity, room302.occupiedCount + 2),
        status: 'AVAILABLE'
      }
    });

    const studentId2 = await studentsService.generateUniqueStudentId();
    const studentRes2 = await studentsService.createStudentByAdmin({
      fullName: 'Vikram Patel',
      collegeName: 'National Tech University',
      course: 'B.Tech Information Technology',
      yearOfStudy: '2nd Year',
      phoneNumber: '9876543211',
      studentId: studentId2,
      hostelId: primaryHostel.id,
      roomNumber: '302'
    });

    console.log(`✓ Student Created: ${studentRes2.student?.fullName} (${studentRes2.student?.rollNumber})`);
    console.log(`✓ Assigned Room: ${studentRes2.student?.roomNumber} (ID: ${studentRes2.student?.roomId}), Bed: ${studentRes2.student?.bedNumber}`);
    if (studentRes2.student?.roomId !== room302.id || studentRes2.student?.roomNumber !== '302') {
      throw new Error(`TEST 3 FAILED: Room assignment mismatch. Expected ${room302.id}, got ${studentRes2.student?.roomId}`);
    }
    createdUserIds.push(studentRes2.student!.userId);
    console.log('✓ TEST 3 PASSED: Room 302 assigned and persisted.\n');

    // ========================================================================
    // TEST 4: CREATE & ASSIGN ROOM 304
    // ========================================================================
    console.log('--- [TEST 4] CREATE STUDENT & ASSIGN TO ROOM 304 ---');
    let room304 = await prisma.room.findFirst({
      where: { hostelId: primaryHostel.id, roomNumber: '304' },
      include: { beds: true }
    });
    if (!room304) {
      room304 = await prisma.room.create({
        data: {
          hostelId: primaryHostel.id,
          roomNumber: '304',
          floor: 3,
          block: 'A',
          roomType: 'DOUBLE',
          totalCapacity: 2,
          occupiedCount: 0,
          monthlyRent: 4500,
          status: 'AVAILABLE'
        },
        include: { beds: true }
      });
      createdRoomIds.push(room304.id);
    }
    await prisma.room.update({
      where: { id: room304.id },
      data: {
        totalCapacity: Math.max(room304.totalCapacity, room304.occupiedCount + 2),
        status: 'AVAILABLE'
      }
    });

    const studentId3 = await studentsService.generateUniqueStudentId();
    const studentRes3 = await studentsService.createStudentByAdmin({
      fullName: 'Priya Sharma',
      collegeName: 'National Tech University',
      course: 'MCA',
      yearOfStudy: '1st Year',
      phoneNumber: '9876543212',
      studentId: studentId3,
      hostelId: primaryHostel.id,
      roomNumber: '304'
    });

    console.log(`✓ Student Created: ${studentRes3.student?.fullName} (${studentRes3.student?.rollNumber})`);
    console.log(`✓ Assigned Room: ${studentRes3.student?.roomNumber} (ID: ${studentRes3.student?.roomId}), Bed: ${studentRes3.student?.bedNumber}`);
    if (studentRes3.student?.roomId !== room304.id || studentRes3.student?.roomNumber !== '304') {
      throw new Error(`TEST 4 FAILED: Room assignment mismatch for 304`);
    }
    createdUserIds.push(studentRes3.student!.userId);
    console.log('✓ TEST 4 PASSED: Room 304 assigned and persisted.\n');

    // ========================================================================
    // TEST 5: DYNAMIC NEW ROOM (ROOM 999) CREATION & ASSIGNMENT
    // ========================================================================
    console.log('--- [TEST 5] NEW ROOM (ROOM 999) DYNAMIC CREATION & ASSIGNMENT ---');
    const newRoom999 = await roomsService.addRoom({
      hostelId: primaryHostel.id,
      roomNumber: '999',
      floor: 9,
      block: 'Penthouse',
      roomType: 'DELUXE' as any,
      totalCapacity: 2,
      monthlyRent: 8000,
      amenities: ['AC', 'WiFi', 'Attached Bathroom']
    });
    if (newRoom999) {
      createdRoomIds.push(newRoom999.roomId);
      console.log(`✓ Created New Room dynamically: Room ${newRoom999.roomNumber} (ID: ${newRoom999.roomId})`);
    }

    const studentId4 = await studentsService.generateUniqueStudentId();
    const studentRes4 = await studentsService.createStudentByAdmin({
      fullName: 'Ananya Reddy',
      collegeName: 'Apex Institute of Science',
      course: 'M.Tech AI',
      yearOfStudy: '1st Year',
      phoneNumber: '9876543213',
      studentId: studentId4,
      hostelId: primaryHostel.id,
      roomNumber: '999'
    });

    console.log(`✓ Student Created: ${studentRes4.student?.fullName} (${studentRes4.student?.rollNumber})`);
    console.log(`✓ Assigned to Newly Created Room: ${studentRes4.student?.roomNumber} (ID: ${studentRes4.student?.roomId})`);
    if (studentRes4.student?.roomId !== newRoom999?.roomId || studentRes4.student?.roomNumber !== '999') {
      throw new Error(`TEST 5 FAILED: New room dynamic assignment mismatch`);
    }
    createdUserIds.push(studentRes4.student!.userId);
    console.log('✓ TEST 5 PASSED: Dynamic Room 999 creation and assignment successful.\n');

    // ========================================================================
    // TEST 6: NO-ROOM ASSIGNMENT (OPTIONAL ROOM SELECTION)
    // ========================================================================
    console.log('--- [TEST 6] NO-ROOM ASSIGNMENT (OPTIONAL ROOM SELECTION) ---');
    const studentId5 = await studentsService.generateUniqueStudentId();
    const studentRes5 = await studentsService.createStudentByAdmin({
      fullName: 'Rahul Verma',
      collegeName: 'City Commerce College',
      course: 'B.Com Honours',
      yearOfStudy: '3rd Year',
      phoneNumber: '9876543214',
      studentId: studentId5,
      hostelId: primaryHostel.id,
      roomNumber: null
    });

    console.log(`✓ Student Created without Room: ${studentRes5.student?.fullName} (${studentRes5.student?.rollNumber})`);
    console.log(`✓ Room ID: ${studentRes5.student?.roomId} (Null), Room Number: ${studentRes5.student?.roomNumber} (Null)`);
    if (studentRes5.student?.roomId !== null || studentRes5.student?.roomNumber !== null) {
      throw new Error(`TEST 6 FAILED: Expected null roomId for no-room selection`);
    }
    createdUserIds.push(studentRes5.student!.userId);
    console.log('✓ TEST 6 PASSED: No-room optional assignment handled cleanly.\n');

    // ========================================================================
    // TEST 7: FULL-ROOM PROTECTION
    // ========================================================================
    console.log('--- [TEST 7] FULL-ROOM PROTECTION ---');
    const singleRoom = await prisma.room.create({
      data: {
        hostelId: primaryHostel.id,
        roomNumber: 'FULL-101',
        floor: 1,
        block: 'A',
        roomType: 'SINGLE',
        totalCapacity: 1,
        occupiedCount: 1,
        monthlyRent: 6000,
        status: 'FULL'
      }
    });
    createdRoomIds.push(singleRoom.id);
    await prisma.bed.create({
      data: {
        roomId: singleRoom.id,
        bedNumber: 'Bed-A',
        isOccupied: true
      }
    });

    let fullRoomRejected = false;
    try {
      const fullRoomStudentId = await studentsService.generateUniqueStudentId();
      await studentsService.createStudentByAdmin({
        fullName: 'Rejected Full Room Student',
        collegeName: 'National Tech University',
        course: 'B.Tech Mechanical',
        yearOfStudy: '1st Year',
        phoneNumber: '9876543215',
        studentId: fullRoomStudentId,
        hostelId: primaryHostel.id,
        roomNumber: 'FULL-101'
      });
    } catch (err: any) {
      fullRoomRejected = true;
      console.log(`✓ Correctly rejected full room assignment with message: "${err.message}"`);
      if (err.status !== 400) throw new Error(`TEST 7 FAILED: Expected status 400, got ${err.status}`);
    }
    if (!fullRoomRejected) throw new Error(`TEST 7 FAILED: Full room was assigned beyond capacity`);
    console.log('✓ TEST 7 PASSED: Full room protection verified.\n');

    // ========================================================================
    // TEST 8: NON-BLOCKING DYNAMIC ROOM PROVISIONING & ASSIGNMENT
    // ========================================================================
    console.log('--- [TEST 8] NON-BLOCKING DYNAMIC ROOM PROVISIONING & ASSIGNMENT ---');
    const dynamicRoomStudentId = await studentsService.generateUniqueStudentId();
    const dynamicRoomStudentRes = await studentsService.createStudentByAdmin({
      fullName: 'Dynamic Room Student',
      collegeName: 'National Tech University',
      course: 'B.Tech Mechanical',
      yearOfStudy: '1st Year',
      phoneNumber: '9876543216',
      studentId: dynamicRoomStudentId,
      hostelId: primaryHostel.id,
      roomNumber: 'A-20'
    });
    createdUserIds.push(dynamicRoomStudentRes.student!.userId);
    console.log(`✓ Student Created & Assigned to Room A-20: ${dynamicRoomStudentRes.student?.fullName} (${dynamicRoomStudentRes.student?.rollNumber}), Room: ${dynamicRoomStudentRes.student?.roomNumber}`);
    if (dynamicRoomStudentRes.student?.roomNumber !== 'A-20') {
      throw new Error(`TEST 8 FAILED: Expected roomNumber 'A-20', got ${dynamicRoomStudentRes.student?.roomNumber}`);
    }
    console.log('✓ TEST 8 PASSED: Dynamic Room A-20 non-blocking assignment verified.\n');

    // ========================================================================
    // TEST 9: DUPLICATE STUDENT ID PROTECTION
    // ========================================================================
    console.log('--- [TEST 9] DUPLICATE STUDENT ID PROTECTION ---');
    let duplicateIdErrorCaught = false;
    try {
      await studentsService.createStudentByAdmin({
        fullName: 'Imposter Student',
        collegeName: 'Apex College',
        course: 'B.Sc',
        yearOfStudy: '1st Year',
        phoneNumber: '9876543217',
        studentId: studentId3,
        hostelId: primaryHostel.id,
        roomId: null
      });
    } catch (err: any) {
      duplicateIdErrorCaught = true;
      console.log(`✓ Correctly rejected duplicate Student ID with message: "${err.message}"`);
    }
    if (!duplicateIdErrorCaught) {
      throw new Error('TEST 9 FAILED: System allowed duplicate Student ID creation');
    }
    console.log('✓ TEST 9 PASSED: Duplicate Student ID creation prevented.\n');

    // ========================================================================
    // TEST 10: STUDENT VALIDATION & REGISTRATION FLOW WITH ISSUED ID
    // ========================================================================
    console.log('--- [TEST 10] STUDENT VALIDATION & REGISTRATION FLOW ---');
    const validLookup = await authService.validateStudentId(studentId4);
    console.log(`✓ Validated Issued ID ${studentId4}:`);
    console.log(`   - Full Name: ${validLookup.fullName}`);
    console.log(`   - College: ${validLookup.collegeName}`);
    console.log(`   - Course: ${validLookup.course}`);
    console.log(`   - Hostel: ${validLookup.hostelName}`);
    console.log(`   - Room: ${validLookup.roomNumber}`);
    console.log(`   - Activated: ${validLookup.isActivated}`);

    if (!validLookup.valid || validLookup.rollNumber !== studentId4) {
      throw new Error('TEST 10 FAILED: validateStudentId did not recognize issued ID');
    }

    const studentPassword = 'PersonalPassword@2026';
    const activationRes = await authService.activateStudent({
      studentId: studentId4,
      email: 'ananya.reddy@testcampus.edu',
      phoneNumber: '9876543213',
      password: studentPassword,
      confirmPassword: studentPassword
    });

    console.log(`✓ Student Activation Successful:`);
    console.log(`   - User ID: ${activationRes.user.userId}`);
    console.log(`   - Email: ${activationRes.user.email}`);
    console.log(`   - JWT Access Token: ${activationRes.tokens.accessToken.substring(0, 25)}...`);
    console.log('✓ TEST 10 PASSED: Issued Student ID used to complete student registration.\n');

    // ========================================================================
    // TEST 11: STUDENT AUTHENTICATION / LOGIN WITH ISSUED STUDENT ID
    // ========================================================================
    console.log('--- [TEST 11] STUDENT LOGIN WITH ISSUED STUDENT ID ---');
    const loginByRoll = await authService.login(studentId4, studentPassword);
    console.log(`✓ Logged in via Student ID (${studentId4}):`);
    console.log(`   - Logged in User: ${loginByRoll.user.fullName} (${loginByRoll.user.email})`);
    console.log(`   - Role: ${loginByRoll.user.role}`);
    console.log(`   - Profile ID: ${loginByRoll.user.studentId}`);

    if (loginByRoll.user.fullName !== 'Ananya Reddy' || loginByRoll.user.studentProfile?.rollNumber !== studentId4) {
      throw new Error('TEST 11 FAILED: Login returned unexpected user');
    }

    let wrongPassCaught = false;
    try {
      await authService.login(studentId4, 'WrongPassword@999');
    } catch (err: any) {
      wrongPassCaught = true;
      console.log(`✓ Correctly rejected wrong password on Student ID login: "${err.message}"`);
    }
    if (!wrongPassCaught) {
      throw new Error('TEST 11 FAILED: Login succeeded with wrong password');
    }
    console.log('✓ TEST 11 PASSED: Student login using issued Student ID verified.\n');

    console.log('========================================================================');
    console.log('🎉 ALL 11 COMPREHENSIVE TESTS PASSED WITH ZERO ERRORS!');
    console.log('========================================================================\n');

  } finally {
    console.log('[CLEANUP] Cleaning up test records...');
    for (const uid of createdUserIds) {
      try {
        await prisma.user.delete({ where: { id: uid } });
      } catch {}
    }
    for (const rid of createdRoomIds) {
      try {
        await prisma.bed.deleteMany({ where: { roomId: rid } });
        await prisma.room.delete({ where: { id: rid } });
      } catch {}
    }
    await prisma.$disconnect();
    console.log('[CLEANUP] Cleanup completed.');
  }
}

runComprehensiveVerification().catch(err => {
  console.error('❌ VERIFICATION FAILURE:', err);
  process.exit(1);
});
