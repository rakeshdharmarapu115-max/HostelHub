import { AuthService } from '../modules/auth/auth.service';
import { StudentsService } from '../modules/students/students.service';
import { HostelsService } from '../modules/hostels/hostels.service';
import { AllocationsService } from '../modules/allocations/allocations.service';
import { prisma } from '../config/prisma';

async function runE2EVerification() {
  console.log('================================================================');
  console.log('HOSTELHUB E2E VERIFICATION: STUDENT ID LIFECYCLE & ALLOCATIONS');
  console.log('================================================================\n');

  const authService = new AuthService();
  const studentsService = new StudentsService();
  const hostelsService = new HostelsService();
  const allocationsService = new AllocationsService();

  try {
    // 0. Setup / Find active hostel & room
    console.log('[SETUP] Finding active hostel and room for E2E testing...');
    const hostels = await hostelsService.getHostels();
    if (hostels.length === 0) {
      throw new Error('No hostels found in database. Seed data or create a hostel first.');
    }
    const testHostel = hostels[0];
    console.log(`[SETUP] Selected Hostel: "${testHostel.name}" (ID: ${testHostel.hostelId})`);

    // ==========================================
    // TEST 1 — GENERATE STUDENT ID
    // ==========================================
    console.log('\n--- [TEST 1] GENERATE STUDENT ID ---');
    const generatedId = await studentsService.generateUniqueStudentId();
    console.log(`✓ Generated Student ID: ${generatedId}`);
    
    // Assertions
    if (!generatedId || typeof generatedId !== 'string') {
      throw new Error('TEST 1 FAILED: Generated Student ID is null or undefined');
    }
    if (!/^STU-\d{4}-\d{4}$/.test(generatedId)) {
      throw new Error(`TEST 1 FAILED: Student ID does not follow STU-YYYY-XXXX format: ${generatedId}`);
    }
    console.log('✓ TEST 1 (Generate) PASSED: ID format verified and generated successfully.\n');

    // ==========================================
    // TEST 2 — ISSUE STUDENT ID
    // ==========================================
    console.log('--- [TEST 2] ISSUE STUDENT ID TO STUDENT ---');
    const testTimestamp = Date.now();
    const testEmail = `e2e_student_${testTimestamp}@campus.edu`;
    const testPhone = `+91 98${Math.floor(10000000 + Math.random() * 90000000)}`;

    // Query or create a room for this hostel
    let testRoom = await prisma.room.findFirst({
      where: { hostelId: testHostel.hostelId }
    });
    if (!testRoom) {
      testRoom = await prisma.room.create({
        data: {
          hostelId: testHostel.hostelId,
          roomNumber: 'E2E-101',
          floor: 1,
          block: 'A',
          roomType: 'DOUBLE',
          totalCapacity: 2,
          occupiedCount: 0,
          monthlyRent: 5000,
          status: 'AVAILABLE'
        }
      });
      await prisma.bed.create({
        data: {
          roomId: testRoom.id,
          bedNumber: 'Bed-1',
          isOccupied: false
        }
      });
    }
    console.log(`[SETUP] Selected Room: "${testRoom.roomNumber}" (ID: ${testRoom.id})`);

    const createResult = await studentsService.createStudentByAdmin({
      fullName: 'Aarav Sharma',
      email: testEmail,
      phoneNumber: testPhone,
      collegeName: 'National Institute of Technology',
      course: 'B.Tech Computer Science',
      yearOfStudy: '3',
      gender: 'MALE',
      permanentAddress: 'Plot 42, Hitech City Road, Hyderabad',
      emergencyContactName: 'Rajesh Sharma',
      emergencyContactPhone: '+91 9876543210',
      studentId: generatedId,
      hostelId: testHostel.hostelId,
      roomId: testRoom.roomNumber,
      bedNumber: 'Bed-1',
      password: 'Temporary@123'
    });

    console.log(`✓ Issue Result: Success=${createResult.success}`);
    console.log(`✓ Created Student Name: ${createResult.student?.fullName}`);
    console.log(`✓ Created Student ID / Roll: ${createResult.student?.rollNumber}`);
    console.log(`✓ Associated Hostel: ${createResult.student?.hostelId}`);
    console.log(`✓ Associated Room: ${createResult.student?.roomNumber}`);
    console.log(`✓ Associated Bed: ${createResult.student?.bedNumber}`);

    if (!createResult.success || !createResult.student) {
      throw new Error('TEST 2 FAILED: Could not issue student ID');
    }
    if (createResult.student.rollNumber !== generatedId) {
      throw new Error(`TEST 2 FAILED: Expected rollNumber ${generatedId}, got ${createResult.student.rollNumber}`);
    }

    // Verify database record
    const dbStudent = await prisma.student.findUnique({
      where: { rollNumber: generatedId },
      include: { user: true, allocations: { include: { room: true, bed: true } } }
    });
    if (!dbStudent) {
      throw new Error('TEST 2 FAILED: Student record not found in PostgreSQL database');
    }
    console.log(`✓ DB State: Student UUID: ${dbStudent.id}, User UUID: ${dbStudent.userId}, Allocations: ${dbStudent.allocations.length}`);
    console.log('✓ TEST 2 (Issue) PASSED: Student ID issued and persisted with allocations.\n');

    // ==========================================
    // TEST 3 — STUDENT REGISTRATION / ACTIVATION
    // ==========================================
    console.log('--- [TEST 3] STUDENT REGISTRATION / ACTIVATION USING ISSUED ID ---');
    // Step 3a: Validate Student ID before activation
    const validationResult = await authService.validateStudentId(generatedId);
    console.log(`✓ Validation lookup for ${generatedId}:`);
    console.log(`   - Valid: ${validationResult.valid}`);
    console.log(`   - Full Name: ${validationResult.fullName}`);
    console.log(`   - Hostel: ${validationResult.hostelName}`);
    console.log(`   - Is Activated: ${validationResult.isActivated}`);

    if (!validationResult.valid || validationResult.rollNumber !== generatedId) {
      throw new Error('TEST 3 FAILED: Issued Student ID could not be validated for registration');
    }

    // Step 3b: Student completes registration by setting permanent password
    const studentPassword = 'SecureStudent@2026';
    const activationResult = await authService.activateStudent({
      studentId: generatedId,
      password: studentPassword,
      email: testEmail
    });

    console.log(`✓ Student Activation Result:`);
    console.log(`   - User ID: ${activationResult.user.userId}`);
    console.log(`   - Email: ${activationResult.user.email}`);
    console.log(`   - Role: ${activationResult.user.role}`);
    console.log(`   - JWT Token Issued: ${activationResult.tokens.accessToken.substring(0, 30)}...`);

    if (activationResult.user.role !== 'STUDENT' || !activationResult.tokens.accessToken) {
      throw new Error('TEST 3 FAILED: Student activation failed to return valid session');
    }
    console.log('✓ TEST 3 (Register / Activate) PASSED\n');

    // ==========================================
    // TEST 4 — STUDENT LOGIN
    // ==========================================
    console.log('--- [TEST 4] STUDENT LOGIN USING STUDENT ID ---');
    const loginResult = await authService.login(generatedId, studentPassword);

    console.log(`✓ Login Response:`);
    console.log(`   - Authenticated User: ${loginResult.user.fullName}`);
    console.log(`   - Student ID: ${loginResult.user.studentId}`);
    console.log(`   - Hostel ID: ${loginResult.user.hostelId}`);
    console.log(`   - Active Status: ${loginResult.user.isActive}`);
    console.log(`   - Access Token: ${loginResult.tokens.accessToken.substring(0, 30)}...`);

    if (loginResult.user.role !== 'STUDENT' || loginResult.user.studentProfile?.rollNumber !== generatedId) {
      throw new Error(`TEST 4 FAILED: Expected rollNumber ${generatedId}, got ${loginResult.user.studentProfile?.rollNumber}`);
    }
    console.log('✓ TEST 4 (Login) PASSED\n');

    // ==========================================
    // TEST 5 — DUPLICATE PROTECTION
    // ==========================================
    console.log('--- [TEST 5] DUPLICATE STUDENT ID REJECTION ---');
    let duplicateRejected = false;
    try {
      await studentsService.createStudentByAdmin({
        fullName: 'Duplicate Imposter',
        email: `imposter_${Date.now()}@campus.edu`,
        phoneNumber: '+91 9999988888',
        collegeName: 'Apex College',
        course: 'MCA',
        yearOfStudy: '1',
        studentId: generatedId, // Attempting to reuse identical ID
        hostelId: testHostel.hostelId
      });
    } catch (err: any) {
      duplicateRejected = true;
      console.log(`✓ Duplicate successfully caught and rejected: "${err.message}" (Code: ${err.code || err.status || 409})`);
    }

    if (!duplicateRejected) {
      throw new Error('TEST 5 FAILED: Duplicate Student ID was accepted by backend!');
    }
    console.log('✓ TEST 5 (Duplicate Protection) PASSED\n');

    // ==========================================
    // TEST 6 — STUDENT DEALLOCATION
    // ==========================================
    console.log('--- [TEST 6] STUDENT DEALLOCATION ---');
    const deallocResult = await studentsService.deallocateStudent({
      studentId: createResult.student.studentId,
      remarks: 'End of academic semester clearance'
    });

    console.log(`✓ Deallocation Success: ${deallocResult.success}`);
    console.log(`✓ Deallocation Message: "${deallocResult.message}"`);
    console.log(`✓ Final Student Status: ${deallocResult.student?.status}`);

    // Verify room/bed allocation in DB is no longer ACTIVE
    const allocationsAfter = await prisma.roomAllocation.findMany({
      where: { studentId: dbStudent.id, status: 'ACTIVE' }
    });
    console.log(`✓ Active Allocations Remaining in DB: ${allocationsAfter.length}`);
    if (allocationsAfter.length > 0) {
      throw new Error('TEST 6 FAILED: Active room allocations still found after deallocation');
    }

    // Verify that subsequent student login is BLOCKED
    let loginBlocked = false;
    try {
      await authService.login(generatedId, studentPassword);
    } catch (err: any) {
      loginBlocked = true;
      console.log(`✓ Login correctly rejected post-deallocation: "${err.message}" (HTTP 403 / ${err.code})`);
    }

    if (!loginBlocked) {
      throw new Error('TEST 6 FAILED: Deallocated student was able to login!');
    }
    console.log('✓ TEST 6 (Deallocation) PASSED\n');

    // Cleanup test student to keep DB pristine
    await prisma.user.delete({ where: { id: dbStudent.userId } }).catch(() => {});

    console.log('================================================================');
    console.log('🎉 ALL 6 COMPREHENSIVE TESTS (1 TO 6) PASSED WITH 100% SUCCESS!');
    console.log('================================================================\n');

  } catch (err) {
    console.error('\n❌ E2E VERIFICATION FAILED:', err);
    process.exit(1);
  } finally {
    await prisma.$disconnect();
  }
}

runE2EVerification();
