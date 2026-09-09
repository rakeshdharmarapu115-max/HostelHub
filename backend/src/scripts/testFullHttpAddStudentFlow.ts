import request from 'supertest';
import { app } from '../server';
import { prisma } from '../config/prisma';

async function runHttpE2ETest() {
  console.log('========================================================================');
  console.log('🌐 RUNNING END-TO-END HTTP API TEST: ADD STUDENT & STUDENT ID WORKFLOW');
  console.log('========================================================================');

  const createdUserIds: string[] = [];
  const createdStudentIds: string[] = [];
  const createdRoomIds: string[] = [];

  try {
    // 1. Authenticate as Hostel Owner
    console.log('\n--- [HTTP STEP 1] HOSTEL OWNER LOGIN ---');
    // Ensure test rooms have sufficient capacity for repeat test runs
    await prisma.room.updateMany({
      where: {
        hostelId: 'hostel_001',
        roomNumber: { in: ['301', '302', '304', 'A-204'] }
      },
      data: { totalCapacity: 20 }
    });

    const loginRes = await request(app)
      .post('/api/auth/login')
      .send({
        emailOrStudentIdOrPhone: 'warden@greenvalley.edu',
        password: 'Password@123'
      });

    if (loginRes.status !== 200 || !loginRes.body?.data?.tokens?.accessToken) {
      throw new Error(`Owner login failed: ${loginRes.status} ${JSON.stringify(loginRes.body)}`);
    }

    const hostToken = loginRes.body.data.tokens.accessToken;
    const hostUser = loginRes.body.data.user;
    console.log(`✓ Owner Logged In: ${hostUser.fullName} (Hostel ID: ${hostUser.hostelId || hostUser.hostProfile?.hostels?.[0]?.id || 'hostel_001'})`);

    // 2. Generate Unique Student ID via API
    console.log('\n--- [HTTP STEP 2] GENERATE STUDENT ID (GET /api/students/generate-id) ---');
    const genIdRes = await request(app)
      .get('/api/students/generate-id')
      .set('Authorization', `Bearer ${hostToken}`);

    if (genIdRes.status !== 200 || !genIdRes.body?.data?.studentId) {
      throw new Error(`Generate ID failed: ${genIdRes.status} ${JSON.stringify(genIdRes.body)}`);
    }

    const generatedId1 = genIdRes.body.data.studentId;
    console.log(`✓ Generated Student ID: ${generatedId1}`);

    // 3. Test Student Creation WITHOUT Room (Optional Room Selection)
    console.log('\n--- [HTTP STEP 3] CREATE STUDENT WITHOUT ROOM (POST /api/students/admin-create) ---');
    const noRoomPayload = {
      fullName: 'HTTP Test Student NoRoom',
      collegeName: 'National University of Tech',
      course: 'B.Sc Computing',
      yearOfStudy: '1st Year',
      phoneNumber: '9876543210',
      emergencyContactPhone: '9876543210',
      studentId: generatedId1,
      hostelId: 'hostel_001',
      roomNumber: null
    };

    const noRoomRes = await request(app)
      .post('/api/students/admin-create')
      .set('Authorization', `Bearer ${hostToken}`)
      .send(noRoomPayload);

    if (noRoomRes.status !== 201 || !noRoomRes.body?.data?.student) {
      throw new Error(`Student creation without room failed: ${noRoomRes.status} ${JSON.stringify(noRoomRes.body)}`);
    }

    const createdNoRoomStudent = noRoomRes.body.data.student;
    createdUserIds.push(createdNoRoomStudent.userId);
    createdStudentIds.push(createdNoRoomStudent.studentId);
    console.log(`✓ Student Created without Room: ${createdNoRoomStudent.fullName} (${createdNoRoomStudent.rollNumber})`);
    console.log(`  Room Number: ${createdNoRoomStudent.roomNumber} (null as expected)`);

    // 4. Test Student Creation with Room 302
    console.log('\n--- [HTTP STEP 4] CREATE STUDENT WITH ROOM 302 ---');
    const genIdRes2 = await request(app)
      .get('/api/students/generate-id')
      .set('Authorization', `Bearer ${hostToken}`);
    const studentId2 = genIdRes2.body.data.studentId;

    const room302Res = await request(app)
      .post('/api/students/admin-create')
      .set('Authorization', `Bearer ${hostToken}`)
      .send({
        fullName: 'HTTP Student Room 302',
        collegeName: 'National University of Tech',
        course: 'Mechanical Engineering',
        yearOfStudy: '2nd Year',
        phoneNumber: '9876543211',
        studentId: studentId2,
        hostelId: 'hostel_001',
        roomNumber: '302'
      });

    if (room302Res.status !== 201) {
      throw new Error(`Room 302 creation failed: ${room302Res.status} ${JSON.stringify(room302Res.body)}`);
    }
    const student302 = room302Res.body.data.student;
    createdUserIds.push(student302.userId);
    createdStudentIds.push(student302.studentId);
    console.log(`✓ Student Created & Assigned to Room 302: ${student302.fullName} (${student302.rollNumber}), Room: ${student302.roomNumber}`);

    // 5. Test Student Creation with Room 304
    console.log('\n--- [HTTP STEP 5] CREATE STUDENT WITH ROOM 304 ---');
    const genIdRes3 = await request(app)
      .get('/api/students/generate-id')
      .set('Authorization', `Bearer ${hostToken}`);
    const studentId3 = genIdRes3.body.data.studentId;

    const room304Res = await request(app)
      .post('/api/students/admin-create')
      .set('Authorization', `Bearer ${hostToken}`)
      .send({
        fullName: 'HTTP Student Room 304',
        collegeName: 'National University of Tech',
        course: 'Electrical Engineering',
        yearOfStudy: '3rd Year',
        phoneNumber: '9876543212',
        studentId: studentId3,
        hostelId: 'hostel_001',
        roomNumber: '304'
      });

    if (room304Res.status !== 201) {
      throw new Error(`Room 304 creation failed: ${room304Res.status} ${JSON.stringify(room304Res.body)}`);
    }
    const student304 = room304Res.body.data.student;
    createdUserIds.push(student304.userId);
    createdStudentIds.push(student304.studentId);
    console.log(`✓ Student Created & Assigned to Room 304: ${student304.fullName} (${student304.rollNumber}), Room: ${student304.roomNumber}`);

    // 6. Test Student Creation with Room A-204
    console.log('\n--- [HTTP STEP 6] CREATE STUDENT WITH ROOM A-204 ---');
    const genIdRes4 = await request(app)
      .get('/api/students/generate-id')
      .set('Authorization', `Bearer ${hostToken}`);
    const studentId4 = genIdRes4.body.data.studentId;

    const roomA204Res = await request(app)
      .post('/api/students/admin-create')
      .set('Authorization', `Bearer ${hostToken}`)
      .send({
        fullName: 'HTTP Student Room A-204',
        collegeName: 'National University of Tech',
        course: 'Civil Engineering',
        yearOfStudy: '1st Year',
        phoneNumber: '9876543213',
        studentId: studentId4,
        hostelId: 'hostel_001',
        roomNumber: 'A-204'
      });

    if (roomA204Res.status !== 201) {
      throw new Error(`Room A-204 creation failed: ${roomA204Res.status} ${JSON.stringify(roomA204Res.body)}`);
    }
    const studentA204 = roomA204Res.body.data.student;
    createdUserIds.push(studentA204.userId);
    createdStudentIds.push(studentA204.studentId);
    console.log(`✓ Student Created & Assigned to Room A-204: ${studentA204.fullName} (${studentA204.rollNumber}), Room: ${studentA204.roomNumber}`);

    // 7. Create a New Room (Dynamic Room Number) via API and Assign Student
    const dynamicRoomNumber = `A-${Math.floor(500 + Math.random() * 400)}`;
    console.log(`\n--- [HTTP STEP 7] CREATE NEW ROOM ${dynamicRoomNumber} & ASSIGN STUDENT ---`);
    const createRoomRes = await request(app)
      .post('/api/rooms')
      .set('Authorization', `Bearer ${hostToken}`)
      .send({
        hostelId: 'hostel_001',
        roomNumber: dynamicRoomNumber,
        floor: 1,
        block: 'A',
        roomType: 'DOUBLE',
        totalCapacity: 2,
        monthlyRent: 400
      });

    if (createRoomRes.status !== 201 && createRoomRes.status !== 200) {
      throw new Error(`Room ${dynamicRoomNumber} creation failed: ${createRoomRes.status} ${JSON.stringify(createRoomRes.body)}`);
    }
    const newRoomData = createRoomRes.body.data;
    const resolvedRoomId = newRoomData?.id || newRoomData?.roomId;
    if (resolvedRoomId) createdRoomIds.push(resolvedRoomId);
    console.log(`✓ New Room ${dynamicRoomNumber} Created: ID ${resolvedRoomId}`);

    const genIdRes5 = await request(app)
      .get('/api/students/generate-id')
      .set('Authorization', `Bearer ${hostToken}`);
    const studentId5 = genIdRes5.body.data.studentId;

    const dynamicRoomStudentRes = await request(app)
      .post('/api/students/admin-create')
      .set('Authorization', `Bearer ${hostToken}`)
      .send({
        fullName: `HTTP Student Room ${dynamicRoomNumber}`,
        collegeName: 'National University of Tech',
        course: 'Aerospace Engineering',
        yearOfStudy: '4th Year',
        phoneNumber: '9876543214',
        studentId: studentId5,
        hostelId: 'hostel_001',
        roomNumber: dynamicRoomNumber
      });

    if (dynamicRoomStudentRes.status !== 201) {
      throw new Error(`Room ${dynamicRoomNumber} student creation failed: ${dynamicRoomStudentRes.status} ${JSON.stringify(dynamicRoomStudentRes.body)}`);
    }
    const studentDynamic = dynamicRoomStudentRes.body.data.student;
    createdUserIds.push(studentDynamic.userId);
    createdStudentIds.push(studentDynamic.studentId);
    console.log(`✓ Student Created & Assigned to New Room ${dynamicRoomNumber}: ${studentDynamic.fullName} (${studentDynamic.rollNumber}), Room: ${studentDynamic.roomNumber}`);

    // 8. Test Duplicate Student ID Protection
    console.log('\n--- [HTTP STEP 8] TEST DUPLICATE STUDENT ID PROTECTION ---');
    const dupRes = await request(app)
      .post('/api/students/admin-create')
      .set('Authorization', `Bearer ${hostToken}`)
      .send({
        fullName: 'Duplicate ID Hacker',
        collegeName: 'National University of Tech',
        course: 'Cybersecurity',
        yearOfStudy: '1st Year',
        phoneNumber: '9876543299',
        studentId: studentId5, // reusing studentId5
        hostelId: 'hostel_001',
        roomNumber: '302'
      });

    if (dupRes.status === 409) {
      console.log(`✓ Duplicate Student ID safely rejected with HTTP 409: "${dupRes.body.message}"`);
    } else {
      throw new Error(`Expected HTTP 409 for duplicate Student ID, but got: ${dupRes.status} ${JSON.stringify(dupRes.body)}`);
    }

    // 9. Verify Hostel Owner Residents Directory
    console.log('\n--- [HTTP STEP 9] GET HOSTEL RESIDENTS DIRECTORY (GET /api/students/hostel/hostel_001) ---');
    const residentsRes = await request(app)
      .get('/api/students/hostel/hostel_001')
      .set('Authorization', `Bearer ${hostToken}`);

    if (residentsRes.status !== 200 || !Array.isArray(residentsRes.body?.data)) {
      throw new Error(`Fetch residents failed: ${residentsRes.status}`);
    }

    const residents = residentsRes.body.data;
    console.log(`✓ Retrieved ${residents.length} residents from database for hostel_001.`);
    const foundNoRoom = residents.find((r: any) => r.rollNumber === generatedId1);
    const found302 = residents.find((r: any) => r.rollNumber === studentId2);
    const found304 = residents.find((r: any) => r.rollNumber === studentId3);
    const foundA204 = residents.find((r: any) => r.rollNumber === studentId4);
    const foundA50 = residents.find((r: any) => r.rollNumber === studentId5);

    if (!foundNoRoom || !found302 || !found304 || !foundA204 || !foundA50) {
      throw new Error(`One or more created students missing from owner resident list!`);
    }
    console.log(`✓ All 5 newly created students are present in the owner residents directory.`);

    // 10. Student ID Validation (Unregistered Student)
    console.log('\n--- [HTTP STEP 10] STUDENT VALIDATE ID (POST /api/auth/validate-student-id) ---');
    const validateRes = await request(app)
      .post('/api/auth/validate-student-id')
      .send({ studentId: studentId5 });

    if (validateRes.status !== 200 || !validateRes.body?.data?.valid) {
      throw new Error(`Student ID validation failed: ${validateRes.status} ${JSON.stringify(validateRes.body)}`);
    }
    console.log(`✓ Student ID ${studentId5} successfully validated: Name=${validateRes.body.data.fullName}, Room=${validateRes.body.data.roomNumber}`);

    // 11. Student Activation & Registration
    console.log('\n--- [HTTP STEP 11] STUDENT ACTIVATION (POST /api/auth/activate-student) ---');
    const activateRes = await request(app)
      .post('/api/auth/activate-student')
      .send({
        studentId: studentId5,
        email: 'aerospace.student@testcampus.edu',
        password: 'NewStudent@2026',
        confirmPassword: 'NewStudent@2026'
      });

    if (activateRes.status !== 200 || !activateRes.body?.data?.tokens?.accessToken) {
      throw new Error(`Student activation failed: ${activateRes.status} ${JSON.stringify(activateRes.body)}`);
    }
    console.log(`✓ Student Account Activated. Access Token issued.`);

    // 12. Student Login with Student ID
    console.log('\n--- [HTTP STEP 12] STUDENT LOGIN WITH ISSUED STUDENT ID ---');
    const studentLoginRes = await request(app)
      .post('/api/auth/login')
      .send({
        emailOrStudentIdOrPhone: studentId5,
        password: 'NewStudent@2026'
      });

    if (studentLoginRes.status !== 200 || studentLoginRes.body?.data?.user?.role !== 'STUDENT') {
      throw new Error(`Student login failed: ${studentLoginRes.status} ${JSON.stringify(studentLoginRes.body)}`);
    }
    console.log(`✓ Student successfully logged in via Student ID: ${studentLoginRes.body.data.user.fullName} (${studentLoginRes.body.data.user.email})`);

    console.log('\n========================================================================');
    console.log('🎉 ALL 12 HTTP END-TO-END TESTS PASSED WITH 100% SUCCESS!');
    console.log('========================================================================');
  } finally {
    // Cleanup created test records
    console.log('\n[CLEANUP] Cleaning up test records...');
    for (const sId of createdStudentIds) {
      await prisma.roomAllocation.deleteMany({ where: { studentId: sId } }).catch(() => {});
      await prisma.student.deleteMany({ where: { id: sId } }).catch(() => {});
    }
    for (const uId of createdUserIds) {
      await prisma.refreshToken.deleteMany({ where: { userId: uId } }).catch(() => {});
      await prisma.user.deleteMany({ where: { id: uId } }).catch(() => {});
    }
    for (const rId of createdRoomIds) {
      if (rId) {
        await prisma.roomAllocation.deleteMany({ where: { roomId: rId } }).catch(() => {});
        await prisma.bed.deleteMany({ where: { roomId: rId } }).catch(() => {});
        await prisma.room.deleteMany({ where: { id: rId } }).catch(() => {});
      }
    }
    console.log('[CLEANUP] Complete.');
  }
}

runHttpE2ETest()
  .then(() => process.exit(0))
  .catch((err) => {
    console.error('❌ HTTP TEST ERROR:', err);
    process.exit(1);
  });
