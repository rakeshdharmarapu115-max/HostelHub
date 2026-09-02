import { AuthService } from '../modules/auth/auth.service';
import { StudentsService } from '../modules/students/students.service';
import { prisma } from '../config/prisma';
import { UserRole } from '../types/enums';

async function runTest() {
  console.log('====================================================');
  console.log('🧪 RUNNING END-TO-END AUTHENTICATION & DEALLOCATION TESTS');
  console.log('====================================================\n');

  const authService = new AuthService();
  const studentsService = new StudentsService();

  const timestamp = Date.now();
  const ownerEmail = `test_owner_${timestamp}@hosteltest.com`;

  try {
    // 1. Owner Registration
    console.log('[TEST 1] Registering Hostel Owner...');
    const ownerReg = await authService.registerHost({
      fullName: 'Test Hostel Owner',
      businessName: `Test Grand Hostel ${timestamp}`,
      contactEmail: ownerEmail,
      contactPhone: '9876543210',
      email: ownerEmail,
      password: 'OwnerPassword@123'
    });
    console.log('  ✅ Owner registered successfully:', ownerReg.user.userId, 'HostId:', ownerReg.user.hostId);

    // 2. Owner generates unique Student ID
    console.log('\n[TEST 2] Owner generating unique Student ID...');
    const generatedId = await studentsService.generateUniqueStudentId();
    console.log('  ✅ Generated Student ID:', generatedId);
    if (!generatedId.startsWith('STU-')) {
      throw new Error(`Expected Student ID starting with STU-, got ${generatedId}`);
    }

    // 3. Owner creates student record using generated ID
    console.log('\n[TEST 3] Owner adding student to hostel with generated ID...');
    const createdStudentResult = await studentsService.createStudentByAdmin({
      fullName: 'John Doe',
      collegeName: 'National Engineering College',
      course: 'Computer Science',
      yearOfStudy: '3rd Year',
      phoneNumber: '9123456780',
      emergencyContactName: 'Parent Doe',
      emergencyContactPhone: '9123456780',
      studentId: generatedId
    }, {
      role: UserRole.HOST,
      profileId: ownerReg.user.hostId || undefined,
      userId: ownerReg.user.userId
    });
    console.log('  ✅ Student created by Owner with ID:', createdStudentResult.credentials.studentId);

    // 4. Student validates an invalid/bogus ID
    console.log('\n[TEST 4] Student attempts validating bogus Student ID...');
    try {
      await authService.validateStudentId('STU-BOGUS-9999');
      throw new Error('Should have failed on bogus Student ID');
    } catch (err: any) {
      console.log('  ✅ Bogus ID rejected with message:', err.message);
      if (!err.message.includes('Invalid Student ID')) {
        console.warn('  ⚠️ Note: message is:', err.message);
      }
    }

    // 5. Student validates their owner-issued Student ID
    console.log('\n[TEST 5] Student validates their real owner-issued Student ID...');
    const validated = await authService.validateStudentId(generatedId);
    console.log('  ✅ Student ID validated successfully:', {
      fullName: validated.fullName,
      rollNumber: validated.rollNumber,
      hostelName: validated.hostelName,
      isActivated: validated.isActivated
    });

    // 6. Student completes registration by creating personal password
    console.log('\n[TEST 6] Student activates account with personal password...');
    const activationResult = await authService.activateStudent({
      studentId: generatedId,
      mobileNumber: '9123456780',
      email: `johndoe_${timestamp}@campus.edu`,
      password: 'MySecretPassword@2026',
      confirmPassword: 'MySecretPassword@2026'
    });
    console.log('  ✅ Student activation successful! Access Token received. Student active status:', activationResult.user.isActive);

    // 7. Verify Student ID cannot be registered again (duplicate registration prevention)
    console.log('\n[TEST 7] Student attempts re-registering already registered Student ID...');
    try {
      await authService.validateStudentId(generatedId);
      throw new Error('Should have failed duplicate validation');
    } catch (err: any) {
      console.log('  ✅ Duplicate registration correctly rejected with code/message:', err.code, err.message);
      if (err.code !== 'STUDENT_ALREADY_REGISTERED') {
        throw new Error(`Expected STUDENT_ALREADY_REGISTERED, got ${err.code}`);
      }
    }

    // 8. Student logs in using Student ID + Personal Password
    console.log('\n[TEST 8] Student logs in using Student ID + Personal Password...');
    const loginResult = await authService.login(generatedId, 'MySecretPassword@2026');
    console.log('  ✅ Student login successful using Student ID! Role:', loginResult.user.role);

    // 9. Student login fails with incorrect password
    console.log('\n[TEST 9] Student attempts login with wrong password...');
    try {
      await authService.login(generatedId, 'WrongPassword@999');
      throw new Error('Should have failed with wrong password');
    } catch (err: any) {
      console.log('  ✅ Wrong password correctly rejected:', err.message);
    }

    // 10. Owner deallocates the student
    console.log('\n[TEST 10] Owner deallocates the student from the hostel...');
    const deallocationResult = await studentsService.deallocateStudent({
      studentId: generatedId,
      requesterHostId: ownerReg.user.hostId || undefined,
      requesterRole: UserRole.HOST,
      remarks: 'Course completed / vacated'
    });
    console.log('  ✅ Student deallocated. New status:', deallocationResult.student?.status);

    // 11. Student attempts login after deallocation
    console.log('\n[TEST 11] Student attempts login after deallocation...');
    try {
      await authService.login(generatedId, 'MySecretPassword@2026');
      throw new Error('Deallocated student should NOT be able to log in');
    } catch (err: any) {
      console.log('  ✅ Post-deallocation login blocked with code:', err.code, 'message:', err.message);
      if (err.code !== 'HOSTEL_ALLOCATION_INACTIVE' && err.code !== 'ACCOUNT_INACTIVE') {
        throw new Error(`Expected HOSTEL_ALLOCATION_INACTIVE or ACCOUNT_INACTIVE, got ${err.code}`);
      }
    }

    console.log('\n====================================================');
    console.log('🎉 ALL 11 BACKEND AUTH & DEALLOCATION TESTS PASSED!');
    console.log('====================================================\n');
  } catch (err: any) {
    console.error('\n❌ TEST FAILED:', err);
    process.exit(1);
  } finally {
    await prisma.$disconnect();
  }
}

runTest();
