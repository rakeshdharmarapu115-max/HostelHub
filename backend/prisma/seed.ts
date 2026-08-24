import { PrismaClient } from '@prisma/client';
import * as bcrypt from 'bcryptjs';

export const UserRole = { ADMIN: 'ADMIN', HOST: 'HOST', STUDENT: 'STUDENT', STAFF: 'STAFF' };
export const StudentStatus = { ACTIVE: 'ACTIVE', VACATED: 'VACATED', PENDING_APPROVAL: 'PENDING_APPROVAL' };
export const HostelGenderType = { BOYS: 'BOYS', GIRLS: 'GIRLS', COED: 'COED' };
export const RoomType = { SINGLE: 'SINGLE', DOUBLE: 'DOUBLE', TRIPLE: 'TRIPLE', DORMITORY: 'DORMITORY' };
export const RoomStatus = { AVAILABLE: 'AVAILABLE', FULL: 'FULL', MAINTENANCE: 'MAINTENANCE' };
export const AllocationStatus = { ACTIVE: 'ACTIVE', TRANSFERRED: 'TRANSFERRED', VACATED: 'VACATED' };
export const FeeTypeEnum = { RENT: 'RENT', MESS: 'MESS', CAUTION_DEPOSIT: 'CAUTION_DEPOSIT', ELECTRICITY: 'ELECTRICITY', FINE: 'FINE', OTHER: 'OTHER' };
export const FeeStatusEnum = { PAID: 'PAID', PARTIALLY_PAID: 'PARTIALLY_PAID', PENDING: 'PENDING', OVERDUE: 'OVERDUE' };
export const PaymentMethodEnum = { ONLINE: 'ONLINE', UPI: 'UPI', CARD: 'CARD', CASH: 'CASH', BANK_TRANSFER: 'BANK_TRANSFER' };
export const PaymentStatusEnum = { SUCCESS: 'SUCCESS', PENDING: 'PENDING', FAILED: 'FAILED' };
export const ComplaintCategoryEnum = { ELECTRICAL: 'ELECTRICAL', PLUMBING: 'PLUMBING', WIFI: 'WIFI', CLEANING: 'CLEANING', FOOD: 'FOOD', FURNITURE: 'FURNITURE', SECURITY: 'SECURITY', OTHER: 'OTHER' };
export const ComplaintUrgencyEnum = { LOW: 'LOW', MEDIUM: 'MEDIUM', HIGH: 'HIGH', CRITICAL: 'CRITICAL' };
export const ComplaintStatusEnum = { OPEN: 'OPEN', IN_PROGRESS: 'IN_PROGRESS', RESOLVED: 'RESOLVED', REJECTED: 'REJECTED' };
export const AttendanceStatusEnum = { PRESENT: 'PRESENT', ABSENT: 'ABSENT', ON_LEAVE: 'ON_LEAVE', LATE: 'LATE' };
export const LeaveStatusEnum = { PENDING: 'PENDING', APPROVED: 'APPROVED', REJECTED: 'REJECTED', CANCELLED: 'CANCELLED' };
export const VisitorStatusEnum = { INSIDE: 'INSIDE', CHECKED_OUT: 'CHECKED_OUT', DENIED: 'DENIED' };
export const AnnouncementPriorityEnum = { NORMAL: 'NORMAL', IMPORTANT: 'IMPORTANT', URGENT: 'URGENT' };
export const NotificationTypeEnum = { PAYMENT_DUE: 'PAYMENT_DUE', PAYMENT_CONFIRMED: 'PAYMENT_CONFIRMED', COMPLAINT_UPDATE: 'COMPLAINT_UPDATE', ATTENDANCE_ALERT: 'ATTENDANCE_ALERT', ANNOUNCEMENT: 'ANNOUNCEMENT', LEAVE_APPROVED: 'LEAVE_APPROVED' };

const prisma = new PrismaClient();

async function main() {
  console.log('Seeding HostelHub database...');

  // 1. Password hashing (Default dev password: Password@123)
  const passwordHash = await bcrypt.hash('Password@123', 12);

  // 2. Clear existing records in reverse dependency order
  await prisma.auditLog.deleteMany();
  await prisma.notification.deleteMany();
  await prisma.announcement.deleteMany();
  await prisma.foodMenu.deleteMany();
  await prisma.visitor.deleteMany();
  await prisma.attendanceRecord.deleteMany();
  await prisma.leaveRequest.deleteMany();
  await prisma.maintenanceLog.deleteMany();
  await prisma.complaint.deleteMany();
  await prisma.payment.deleteMany();
  await prisma.fee.deleteMany();
  await prisma.feeType.deleteMany();
  await prisma.roomAllocation.deleteMany();
  await prisma.bed.deleteMany();
  await prisma.room.deleteMany();
  await prisma.floor.deleteMany();
  await prisma.block.deleteMany();
  await prisma.student.deleteMany();
  await prisma.staff.deleteMany();
  await prisma.hostel.deleteMany();
  await prisma.host.deleteMany();
  await prisma.admin.deleteMany();
  await prisma.refreshToken.deleteMany();
  await prisma.user.deleteMany();

  // 3. Create Users & Profiles
  // Admin User
  const adminUser = await prisma.user.create({
    data: {
      id: 'admin_001',
      email: 'admin@campus.edu',
      passwordHash,
      role: UserRole.ADMIN,
      fullName: 'Dean Henderson',
      phoneNumber: '+1 555-0100',
      avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb',
      adminProfile: {
        create: {
          id: 'adm_001',
          fullName: 'Dean Henderson',
          associationName: 'Campus Housing Association',
          designation: 'Dean of Student Welfare',
          permissions: JSON.stringify(['ALL', 'APPROVE_HOSTEL', 'MANAGE_FINANCES', 'BROADCAST_ALL']),
          contactPhone: '+1 555-0100'
        }
      }
    }
  });

  // Host Users
  const hostUser1 = await prisma.user.create({
    data: {
      id: 'host_001',
      email: 'warden@greenvalley.edu',
      passwordHash,
      role: UserRole.HOST,
      fullName: 'Robert Vance',
      phoneNumber: '+1 555-HOSTEL',
      avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d',
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

  const hostUser2 = await prisma.user.create({
    data: {
      id: 'host_002',
      email: 'warden@stjude.edu',
      passwordHash,
      role: UserRole.HOST,
      fullName: 'Sister Claire',
      phoneNumber: '+1 555-STJUDE',
      avatarUrl: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2',
      hostProfile: {
        create: {
          id: 'host_002',
          fullName: 'Sister Claire',
          businessName: 'St. Jude Housing Trust',
          contactPhone: '+1 555-STJUDE',
          contactEmail: 'warden@stjude.edu',
          verifiedStatus: true
        }
      }
    }
  });

  // Hostels
  const hostel1 = await prisma.hostel.create({
    data: {
      id: 'hostel_001',
      hostId: 'host_001',
      name: 'Green Valley Residencies',
      address: '12 North Campus Road, University District',
      city: 'Academic City',
      state: 'State',
      postalCode: '10001',
      latitude: 40.7128,
      longitude: -74.006,
      description: 'Premium student housing with high-speed Wi-Fi, modern study pods, 24/7 security, gym, and nutritious catering.',
      genderType: HostelGenderType.COED,
      amenities: JSON.stringify(['Wi-Fi', 'Air Conditioning', 'Mess Included', '24/7 Power Backup', 'Gym', 'Laundry']),
      rules: JSON.stringify(['Curfew: 10:30 PM', 'No smoking on premises', 'Quiet hours after 11:00 PM']),
      images: JSON.stringify(['https://images.unsplash.com/photo-1555854877-bab0e564b8d5']),
      totalRooms: 30,
      totalBeds: 60,
      occupiedBeds: 52,
      baseMonthlyRent: 450.0,
      cautionDeposit: 200.0,
      rating: 4.8,
      ratingCount: 124,
      contactEmail: 'warden@greenvalley.edu',
      contactPhone: '+1 555-HOSTEL'
    }
  });

  const hostel2 = await prisma.hostel.create({
    data: {
      id: 'hostel_002',
      hostId: 'host_002',
      name: 'St. Jude Student Suites',
      address: '45 West Avenue, Campus Perimeter',
      city: 'Academic City',
      state: 'State',
      postalCode: '10002',
      latitude: 40.7135,
      longitude: -74.008,
      description: 'Cozy and affordable student dormitory close to the central library and sports pavilion.',
      genderType: HostelGenderType.BOYS,
      amenities: JSON.stringify(['Wi-Fi', 'Mess Included', 'CCTV Security', 'Study Hall']),
      rules: JSON.stringify(['Curfew: 10:00 PM', 'Guests allowed till 8 PM']),
      images: JSON.stringify(['https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf']),
      totalRooms: 25,
      totalBeds: 50,
      occupiedBeds: 40,
      baseMonthlyRent: 380.0,
      cautionDeposit: 150.0,
      rating: 4.5,
      ratingCount: 88,
      contactEmail: 'warden@stjude.edu',
      contactPhone: '+1 555-STJUDE'
    }
  });

  // Blocks & Floors for Hostel 1
  const blockA = await prisma.block.create({
    data: {
      id: 'blk_h1_a',
      hostelId: 'hostel_001',
      blockName: 'A',
      totalFloors: 3,
      description: 'Block A - AC Deluxe Wing'
    }
  });

  const blockB = await prisma.block.create({
    data: {
      id: 'blk_h1_b',
      hostelId: 'hostel_001',
      blockName: 'B',
      totalFloors: 3,
      description: 'Block B - Standard Single & Double Wing'
    }
  });

  const floorA2 = await prisma.floor.create({
    data: {
      id: 'flr_h1_a_2',
      blockId: 'blk_h1_a',
      hostelId: 'hostel_001',
      floorNumber: 2,
      totalRooms: 10
    }
  });

  const floorB1 = await prisma.floor.create({
    data: {
      id: 'flr_h1_b_1',
      blockId: 'blk_h1_b',
      hostelId: 'hostel_001',
      floorNumber: 1,
      totalRooms: 10
    }
  });

  // Rooms
  const room204 = await prisma.room.create({
    data: {
      id: 'room_204',
      hostelId: 'hostel_001',
      blockId: 'blk_h1_a',
      floorId: 'flr_h1_a_2',
      roomNumber: 'A-204',
      floor: 2,
      block: 'A',
      roomType: RoomType.DOUBLE,
      totalCapacity: 2,
      occupiedCount: 2,
      monthlyRent: 450.0,
      amenities: JSON.stringify(['AC', 'Attached Bath', 'Study Table', 'Balcony']),
      status: RoomStatus.FULL
    }
  });

  const room205 = await prisma.room.create({
    data: {
      id: 'room_205',
      hostelId: 'hostel_001',
      blockId: 'blk_h1_a',
      floorId: 'flr_h1_a_2',
      roomNumber: 'A-205',
      floor: 2,
      block: 'A',
      roomType: RoomType.DOUBLE,
      totalCapacity: 2,
      occupiedCount: 1,
      monthlyRent: 450.0,
      amenities: JSON.stringify(['AC', 'Attached Bath', 'Study Table']),
      status: RoomStatus.AVAILABLE
    }
  });

  const room101 = await prisma.room.create({
    data: {
      id: 'room_101',
      hostelId: 'hostel_001',
      blockId: 'blk_h1_b',
      floorId: 'flr_h1_b_1',
      roomNumber: 'B-101',
      floor: 1,
      block: 'B',
      roomType: RoomType.SINGLE,
      totalCapacity: 1,
      occupiedCount: 1,
      monthlyRent: 600.0,
      amenities: JSON.stringify(['AC', 'Attached Bath', 'Fridge', 'Study Table']),
      status: RoomStatus.FULL
    }
  });

  // Beds
  await prisma.bed.createMany({
    data: [
      { id: 'bed_1', roomId: 'room_204', bedNumber: 'Bed-A', isOccupied: true },
      { id: 'bed_2', roomId: 'room_204', bedNumber: 'Bed-B', isOccupied: true },
      { id: 'bed_3', roomId: 'room_205', bedNumber: 'Bed-A', isOccupied: true },
      { id: 'bed_4', roomId: 'room_205', bedNumber: 'Bed-B', isOccupied: false },
      { id: 'bed_5', roomId: 'room_101', bedNumber: 'Bed-A', isOccupied: true }
    ]
  });

  // Students
  const stdUser1 = await prisma.user.create({
    data: {
      id: 'std_001',
      email: 'student@campus.edu',
      passwordHash,
      role: UserRole.STUDENT,
      fullName: 'Alex Mercer',
      phoneNumber: '+1 555-0199',
      avatarUrl: 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6',
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
          status: StudentStatus.ACTIVE
        }
      }
    }
  });

  const stdUser2 = await prisma.user.create({
    data: {
      id: 'std_002',
      email: 'david.miller@campus.edu',
      passwordHash,
      role: UserRole.STUDENT,
      fullName: 'David Miller',
      phoneNumber: '+1 555-0188',
      avatarUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e',
      studentProfile: {
        create: {
          id: 'std_002',
          fullName: 'David Miller',
          rollNumber: 'STD-2024-0043',
          collegeName: 'School of Management',
          course: 'BBA',
          yearOfStudy: '2',
          gender: 'male',
          permanentAddress: '88 Wall Street, Metro City',
          emergencyContactName: 'James Miller (Father)',
          emergencyContactPhone: '+1 555-0145',
          hostelId: 'hostel_001',
          hostelName: 'Green Valley Residencies',
          roomId: 'room_204',
          roomNumber: 'A-204',
          bedNumber: 'Bed-B',
          status: StudentStatus.ACTIVE
        }
      }
    }
  });

  const stdUser3 = await prisma.user.create({
    data: {
      id: 'std_003',
      email: 'jordan.reed@campus.edu',
      passwordHash,
      role: UserRole.STUDENT,
      fullName: 'Jordan Reed',
      phoneNumber: '+1 555-0177',
      avatarUrl: 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7',
      studentProfile: {
        create: {
          id: 'std_003',
          fullName: 'Jordan Reed',
          rollNumber: 'STD-2024-0088',
          collegeName: 'Faculty of Arts & Sciences',
          course: 'B.Sc Physics',
          yearOfStudy: '1',
          gender: 'male',
          permanentAddress: '15 Newton Drive, Cambridge City',
          emergencyContactName: 'Arthur Reed (Father)',
          emergencyContactPhone: '+1 555-0146',
          hostelId: 'hostel_001',
          hostelName: 'Green Valley Residencies',
          roomId: 'room_101',
          roomNumber: 'B-101',
          bedNumber: 'Bed-A',
          status: StudentStatus.ACTIVE
        }
      }
    }
  });

  // Room Allocations
  await prisma.roomAllocation.createMany({
    data: [
      {
        id: 'alloc_001',
        bedId: 'bed_1',
        roomId: 'room_204',
        hostelId: 'hostel_001',
        studentId: 'std_001',
        status: AllocationStatus.ACTIVE,
        allocatedBy: 'host_001',
        remarks: 'Regular term allotment'
      },
      {
        id: 'alloc_002',
        bedId: 'bed_2',
        roomId: 'room_204',
        hostelId: 'hostel_001',
        studentId: 'std_002',
        status: AllocationStatus.ACTIVE,
        allocatedBy: 'host_001',
        remarks: 'Roommate preference matched'
      }
    ]
  });

  // Fee Types & Fees
  await prisma.feeType.createMany({
    data: [
      { id: 'ft_001', hostelId: 'hostel_001', name: 'RENT', defaultAmount: 450.0, billingCycle: 'MONTHLY' },
      { id: 'ft_002', hostelId: 'hostel_001', name: 'MESS', defaultAmount: 120.0, billingCycle: 'MONTHLY' },
      { id: 'ft_003', hostelId: 'hostel_001', name: 'CAUTION_DEPOSIT', defaultAmount: 200.0, billingCycle: 'ONE_TIME' }
    ]
  });

  const fee1 = await prisma.fee.create({
    data: {
      id: 'fee_001',
      hostelId: 'hostel_001',
      studentId: 'std_001',
      roomId: 'room_204',
      title: 'October 2026 Accommodation & Mess',
      feeType: FeeTypeEnum.RENT,
      amount: 450.0,
      amountPaid: 450.0,
      dueDate: new Date('2026-10-10'),
      billingMonth: 10,
      billingYear: 2026,
      status: FeeStatusEnum.PAID
    }
  });

  const fee2 = await prisma.fee.create({
    data: {
      id: 'fee_002',
      hostelId: 'hostel_001',
      studentId: 'std_001',
      roomId: 'room_204',
      title: 'November 2026 Accommodation & Mess',
      feeType: FeeTypeEnum.RENT,
      amount: 450.0,
      amountPaid: 0.0,
      dueDate: new Date('2026-11-10'),
      billingMonth: 11,
      billingYear: 2026,
      status: FeeStatusEnum.PENDING
    }
  });

  // Payments
  await prisma.payment.create({
    data: {
      id: 'pay_101',
      feeId: 'fee_001',
      studentId: 'std_001',
      hostelId: 'hostel_001',
      amountPaid: 450.0,
      paymentMethod: PaymentMethodEnum.UPI,
      transactionReference: 'TXN-98421049-OCT',
      paymentDate: new Date('2026-10-09'),
      receiptUrl: 'https://receipts.campus.edu/pay_101.pdf',
      status: PaymentStatusEnum.SUCCESS,
      verifiedByHostId: 'host_001',
      remarks: 'Cleared via GPay UPI'
    }
  });

  // Complaints
  await prisma.complaint.create({
    data: {
      id: 'comp_001',
      hostelId: 'hostel_001',
      studentId: 'std_001',
      studentName: 'Alex Mercer',
      roomNumber: 'A-204',
      category: ComplaintCategoryEnum.ELECTRICAL,
      title: 'Study lamp socket sparking',
      description: 'The main wall power outlet near desk 1 has intermittent sparks when plugging in laptops.',
      attachments: JSON.stringify(['https://images.unsplash.com/photo-1581092160607-ee22621dd758']),
      urgency: ComplaintUrgencyEnum.HIGH,
      status: ComplaintStatusEnum.IN_PROGRESS,
      assignedStaffName: 'Carl Johnson (Electrician)',
      hostNotes: 'Technician dispatched for morning inspection.'
    }
  });

  await prisma.complaint.create({
    data: {
      id: 'comp_002',
      hostelId: 'hostel_001',
      studentId: 'std_001',
      studentName: 'Alex Mercer',
      roomNumber: 'A-204',
      category: ComplaintCategoryEnum.PLUMBING,
      title: 'Bathroom faucet low pressure',
      description: 'Water flow is very low in the morning hours.',
      urgency: ComplaintUrgencyEnum.LOW,
      status: ComplaintStatusEnum.RESOLVED,
      assignedStaffName: 'Mario Rossi',
      hostNotes: 'Assigned plumber',
      resolutionSummary: 'Aerator cleaned and valve adjusted.',
      resolvedAt: new Date()
    }
  });

  // Attendance
  await prisma.attendanceRecord.createMany({
    data: [
      {
        id: 'att_std1_20261018',
        hostelId: 'hostel_001',
        studentId: 'std_001',
        studentName: 'Alex Mercer',
        roomNumber: 'A-204',
        date: '2026-10-18',
        status: AttendanceStatusEnum.PRESENT,
        markedBy: 'STUDENT_SELF'
      },
      {
        id: 'att_std1_20261019',
        hostelId: 'hostel_001',
        studentId: 'std_001',
        studentName: 'Alex Mercer',
        roomNumber: 'A-204',
        date: '2026-10-19',
        status: AttendanceStatusEnum.PRESENT,
        markedBy: 'STUDENT_SELF'
      },
      {
        id: 'att_std1_20261020',
        hostelId: 'hostel_001',
        studentId: 'std_001',
        studentName: 'Alex Mercer',
        roomNumber: 'A-204',
        date: '2026-10-20',
        status: AttendanceStatusEnum.PRESENT,
        markedBy: 'STUDENT_SELF'
      }
    ]
  });

  // Food Menu
  const sampleSchedule = JSON.stringify({
    monday: {
      breakfast: ['Poha', 'Boiled Eggs / Sprouts', 'Tea & Coffee'],
      lunch: ['Steamed Rice', 'Yellow Dal Tadka', 'Paneer Butter Masala', 'Curd'],
      snacks: ['Vegetable Samosa', 'Masala Chai'],
      dinner: ['Butter Roti', 'Mixed Vegetable Curry', 'Jeera Rice', 'Gulab Jamun']
    },
    tuesday: {
      breakfast: ['Idli & Vada', 'Coconut Chutney', 'Sambar', 'Filter Coffee'],
      lunch: ['Jeera Rice', 'Rajma Masala', 'Aloo Gobi', 'Salad'],
      snacks: ['Cookies & Biscuits', 'Tea'],
      dinner: ['Phulka Roti', 'Chicken Curry / Paneer Kadhai', 'Dal Fry', 'Ice Cream']
    },
    wednesday: {
      breakfast: ['Aloo Paratha', 'Curd & Pickle', 'Tea'],
      lunch: ['Veg Biryani', 'Mirchi Ka Salan', 'Raita', 'Papad'],
      snacks: ['Sandwich', 'Coffee'],
      dinner: ['Roti', 'Dal Makhani', 'Bhindi Masala', 'Kheer']
    },
    thursday: {
      breakfast: ['Upma', 'Sambar', 'Boiled Eggs', 'Tea'],
      lunch: ['Rice', 'Chole Masala', 'Bhature', 'Salad'],
      snacks: ['Puffs', 'Tea'],
      dinner: ['Roti', 'Egg Curry / Malai Kofta', 'Rice', 'Custard']
    },
    friday: {
      breakfast: ['Masala Dosa', 'Sambar', 'Chutney', 'Coffee'],
      lunch: ['Fried Rice', 'Chilli Paneer / Manchurian', 'Soup'],
      snacks: ['Pakora', 'Tea'],
      dinner: ['Roti', 'Dal Tadka', 'Dum Aloo', 'Jalebi']
    },
    saturday: {
      breakfast: ['Puri Bhaji', 'Halwa', 'Tea'],
      lunch: ['Curd Rice', 'Lemon Rice', 'Potato Fry', 'Papad'],
      snacks: ['Bhel Puri', 'Juice'],
      dinner: ['Naan', 'Paneer Tikka Masala', 'Pulao', 'Rasgulla']
    },
    sunday: {
      breakfast: ['Chole Bhature', 'Lassi / Sweet Tea'],
      lunch: ['Special Chicken Biryani / Hyderabadi Veg Biryani', 'Raita', 'Sweet'],
      snacks: ['Pastry', 'Cold Coffee'],
      dinner: ['Light Khichdi', 'Kadhi', 'Papad', 'Fruit Salad']
    }
  });

  await prisma.foodMenu.create({
    data: {
      id: 'menu_h1_current',
      hostelId: 'hostel_001',
      weekStartDate: '2026-10-19',
      scheduleJson: sampleSchedule,
      specialNotice: 'Sunday Special Feast will be served between 12:30 PM and 3:00 PM.',
      isPublished: true
    }
  });

  // Announcements
  await prisma.announcement.createMany({
    data: [
      {
        id: 'anc_1',
        hostelId: 'hostel_001',
        senderId: 'host_001',
        senderRole: UserRole.HOST,
        senderName: 'Hostel Warden',
        title: 'Monthly Wi-Fi Maintenance on Saturday',
        message: 'High-speed network maintenance will occur between 2:00 AM and 5:00 AM on Saturday.',
        priority: AnnouncementPriorityEnum.NORMAL,
        targetAudience: 'ALL'
      },
      {
        id: 'anc_2',
        hostelId: 'GLOBAL_CAMPUS',
        senderId: 'admin_001',
        senderRole: UserRole.ADMIN,
        senderName: 'Campus Housing Association',
        title: 'Winter Break Hostel Guidelines',
        message: 'All residents planning to stay during the winter term must submit vacation permission slips by Nov 25th.',
        priority: AnnouncementPriorityEnum.IMPORTANT,
        targetAudience: 'ALL'
      }
    ]
  });

  // Notifications
  await prisma.notification.createMany({
    data: [
      {
        id: 'notif_1',
        recipientUserId: 'std_001',
        title: 'Rent Due Reminder',
        body: 'Your accommodation fee for November is due in 5 days.',
        type: NotificationTypeEnum.PAYMENT_DUE,
        relatedEntityId: 'fee_002',
        isRead: false
      },
      {
        id: 'notif_2',
        recipientUserId: 'std_001',
        title: 'Complaint Status Updated',
        body: 'Your complaint #comp_001 has been assigned to Carl Johnson (Electrician).',
        type: NotificationTypeEnum.COMPLAINT_UPDATE,
        relatedEntityId: 'comp_001',
        isRead: false
      }
    ]
  });

  console.log('Database seeded successfully!');
}

main()
  .catch((e) => {
    console.error('Error seeding database:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
