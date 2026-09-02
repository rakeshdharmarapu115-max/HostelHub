import { AuthService } from '../modules/auth/auth.service';
import { StudentsService } from '../modules/students/students.service';
import { HostelsService } from '../modules/hostels/hostels.service';
import { prisma } from '../config/prisma';

async function runTests() {
  console.log('🧪 ========================================================');
  console.log('🧪 TESTING HOSTEL MANAGEMENT MASTER SPECIFICATION FEATURES');
  console.log('🧪 ========================================================\n');

  const authService = new AuthService();
  const studentsService = new StudentsService();
  const hostelsService = new HostelsService();

  // Test 1: Generate Unique Student ID
  console.log('▶ [1/6] Testing Student ID Generation...');
  const generatedId = await studentsService.generateUniqueStudentId();
  console.log(`   ✅ Generated Student ID: ${generatedId}`);
  if (!generatedId.startsWith('STU-')) {
    throw new Error(`Invalid Student ID format: ${generatedId}`);
  }

  // Test 2: Admin Creates Student with Generated ID
  console.log('\n▶ [2/6] Testing Admin Create Student Record with Generated ID...');
  const uniqueTestEmail = `test_stu_${Date.now()}@campus.edu`;
  const created = await studentsService.createStudentByAdmin({
    fullName: 'Test Resident Student',
    phoneNumber: '+91 9988776655',
    email: uniqueTestEmail,
    collegeName: 'Apex College of Technology',
    course: 'B.Tech Information Technology',
    yearOfStudy: '2',
    permanentAddress: '123 Test Street, Cyber City',
    emergencyContactName: 'Test Guardian',
    emergencyContactPhone: '+91 9988776600',
    studentId: generatedId,
    password: 'Password@123'
  });
  console.log(`   ✅ Created student: ${created.student!.fullName} (ID: ${created.student!.rollNumber}, Email: ${created.credentials.email})`);

  // Test 3: Student Login using generated Student ID
  console.log('\n▶ [3/6] Testing Student Login via Generated Student ID...');
  const loginResult = await authService.login(generatedId, 'Password@123');
  console.log(`   ✅ Successfully authenticated with Student ID: ${loginResult.user.fullName} (Role: ${loginResult.user.role})`);
  console.log(`   ✅ Access Token generated: ${loginResult.tokens.accessToken.substring(0, 25)}...`);

  // Test 4: Hostel Location Update
  console.log('\n▶ [4/6] Testing Hostel Location Update...');
  const allHostels = await hostelsService.getHostels();
  if (allHostels.length > 0) {
    const testHostel = allHostels[0];
    const updatedLocation = await hostelsService.updateHostelLocation(testHostel.hostelId, {
      latitude: 17.3850,
      longitude: 78.4867,
      address: '100 HiTech City Boulevard',
      city: 'Hyderabad',
      state: 'Telangana',
      postalCode: '500081'
    });
    console.log(`   ✅ Updated Hostel Location for '${updatedLocation.name}': Lat ${updatedLocation.latitude}, Lng ${updatedLocation.longitude}, City: ${updatedLocation.city}`);
  }

  // Test 5: Nearby Hostel Search with Distance Calculation
  console.log('\n▶ [5/6] Testing Nearby Hostel Location Search (Haversine Distance)...');
  const nearbyHostels = await hostelsService.searchNearbyHostels({
    lat: 17.3850,
    lng: 78.4867,
    radius: 50
  });
  console.log(`   ✅ Found ${nearbyHostels.length} hostels nearby:`);
  nearbyHostels.slice(0, 3).forEach(h => {
    console.log(`      * ${h.name}: ${h.distanceKm !== null ? h.distanceKm + ' km away' : 'Distance N/A'} (${h.city})`);
  });

  // Test 6: Deallocation and Immediate Rejection
  console.log('\n▶ [6/6] Testing Student Deallocation & Instant Authentication Block...');
  const deallocateResult = await studentsService.deallocateStudent({
    studentId: created.student!.studentId,
    remarks: 'Graduation / Vacate clearance'
  });
  console.log(`   ✅ Deallocation succeeded: ${deallocateResult.message} (Status: ${deallocateResult.student!.status})`);

  // Verify that student login is now immediately REJECTED with 403 ACCOUNT_DEALLOCATED
  try {
    await authService.login(generatedId, 'Password@123');
    throw new Error('❌ FAILURE: Deallocated student was able to login!');
  } catch (err: any) {
    if (err.code === 'ACCOUNT_DEALLOCATED' || err.status === 403) {
      console.log(`   ✅ CORRECT: Deallocated student login rejected with HTTP 403 (${err.code}): "${err.message}"`);
    } else {
      throw err;
    }
  }

  // Cleanup test user
  await prisma.user.delete({ where: { id: created.student!.userId } }).catch(() => {});
  console.log('\n🎉 ALL MASTER SPECIFICATION BACKEND TESTS PASSED SUCCESSFULLY!\n');
}

runTests()
  .then(() => process.exit(0))
  .catch((err) => {
    console.error('❌ Test failed with error:', err);
    process.exit(1);
  });
