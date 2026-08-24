import { prisma } from '../config/prisma';
import * as bcrypt from 'bcryptjs';

export async function autoSeedIfEmpty(): Promise<void> {
  try {
    const userCount = await prisma.user.count();
    if (userCount > 0) {
      return; // Database already has users
    }

    console.log('🌱 Empty database detected on startup. Initializing default HostelHub accounts and campus data...');

    const passwordHash = await bcrypt.hash('Password@123', 12);

    // 1. Admin User
    await prisma.user.create({
      data: {
        id: 'admin_001',
        email: 'admin@campus.edu',
        passwordHash,
        role: 'ADMIN',
        fullName: 'Dean Henderson',
        phoneNumber: '+1 555-0100',
        adminProfile: {
          create: {
            id: 'adm_001',
            fullName: 'Dean Henderson',
            associationName: 'Campus Housing Association',
            designation: 'Dean of Student Welfare',
            permissions: 'ALL',
            contactPhone: '+1 555-0100'
          }
        }
      }
    });

    // 2. Warden / Host User
    const hostUser = await prisma.user.create({
      data: {
        id: 'host_001',
        email: 'warden@greenvalley.edu',
        passwordHash,
        role: 'HOST',
        fullName: 'Robert Vance',
        phoneNumber: '+1 555-HOSTEL',
        hostProfile: {
          create: {
            id: 'host_001',
            fullName: 'Robert Vance',
            businessName: 'Green Valley Residences Inc',
            contactPhone: '+1 555-HOSTEL',
            contactEmail: 'warden@greenvalley.edu',
            verifiedStatus: true
          }
        }
      }
    });

    // 3. Hostel
    const hostel = await prisma.hostel.create({
      data: {
        id: 'hostel_001',
        hostId: 'host_001',
        name: 'Green Valley Residencies',
        address: '12 North Campus Road, University District',
        city: 'Academic City',
        state: 'State',
        postalCode: '10001',
        description: 'Premium student housing with high-speed Wi-Fi, study pods, 24/7 security, gym, and nutritious catering.',
        genderType: 'COED',
        amenities: JSON.stringify(['Wi-Fi', 'Air Conditioning', 'Mess Included', '24/7 Power Backup', 'Gym', 'Laundry']),
        rules: JSON.stringify(['Curfew: 10:30 PM', 'No smoking on premises', 'Quiet hours after 11:00 PM']),
        images: JSON.stringify(['https://images.unsplash.com/photo-1555854877-bab0e564b8d5']),
        totalRooms: 30,
        totalBeds: 60,
        occupiedBeds: 2,
        baseMonthlyRent: 450.0,
        cautionDeposit: 200.0,
        rating: 4.8,
        ratingCount: 124,
        contactEmail: 'warden@greenvalley.edu',
        contactPhone: '+1 555-HOSTEL'
      }
    });

    // 4. Block, Floor, Room, Bed
    const block = await prisma.block.create({
      data: {
        id: 'blk_h1_a',
        hostelId: 'hostel_001',
        blockName: 'A',
        totalFloors: 3,
        description: 'Block A - AC Deluxe Wing'
      }
    });

    const floor = await prisma.floor.create({
      data: {
        id: 'flr_h1_a_2',
        blockId: 'blk_h1_a',
        hostelId: 'hostel_001',
        floorNumber: 2,
        totalRooms: 10
      }
    });

    const room = await prisma.room.create({
      data: {
        id: 'room_204',
        hostelId: 'hostel_001',
        blockId: 'blk_h1_a',
        floorId: 'flr_h1_a_2',
        roomNumber: 'A-204',
        floor: 2,
        block: 'A',
        roomType: 'DOUBLE',
        totalCapacity: 2,
        occupiedCount: 1,
        monthlyRent: 450.0,
        amenities: JSON.stringify(['AC', 'Attached Bath', 'Study Table', 'Balcony']),
        status: 'AVAILABLE'
      }
    });

    const bed = await prisma.bed.create({
      data: {
        id: 'bed_1',
        roomId: 'room_204',
        bedNumber: 'Bed-A',
        isOccupied: true
      }
    });

    // 5. Student User
    await prisma.user.create({
      data: {
        id: 'std_001',
        email: 'student@campus.edu',
        passwordHash,
        role: 'STUDENT',
        fullName: 'Alex Mercer',
        phoneNumber: '+1 555-0199',
        studentProfile: {
          create: {
            id: 'std_001',
            fullName: 'Alex Mercer',
            rollNumber: 'STD-2024-0042',
            collegeName: 'College of Engineering',
            course: 'B.Tech Computer Science',
            yearOfStudy: '3',
            gender: 'male',
            permanentAddress: '42 Silicon Avenue, Metro City',
            emergencyContactName: 'Sarah Mercer (Mother)',
            emergencyContactPhone: '+1 555-0144',
            hostelId: 'hostel_001',
            hostelName: 'Green Valley Residencies',
            roomId: 'room_204',
            roomNumber: 'A-204',
            bedNumber: 'Bed-A',
            status: 'ACTIVE'
          }
        }
      }
    });

    console.log('✅ Initial database seed completed! Ready for login:');
    console.log('   👤 Student:  student@campus.edu / Password@123');
    console.log('   🏢 Warden:   warden@greenvalley.edu / Password@123');
    console.log('   👑 Admin:    admin@campus.edu / Password@123');
  } catch (err: any) {
    console.warn('⚠️ Auto-seed check notice (will proceed):', err.message || err);
  }
}
