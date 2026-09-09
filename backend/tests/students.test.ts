import request from 'supertest';
import { app } from '../src/server';
import { prisma } from '../src/config/prisma';
import { generateAccessToken } from '../src/utils/jwt';
import { UserRole } from '../src/types/enums';

describe('Student Roommates Isolation API (GET /api/students/my-roommates)', () => {
  const studentToken1 = generateAccessToken({
    userId: 'std_user_001',
    email: 'student1@campus.edu',
    role: UserRole.STUDENT,
    fullName: 'Student One',
    profileId: 'std_001',
    hostelId: 'hostel_001'
  });

  const studentTokenNoRoom = generateAccessToken({
    userId: 'std_user_unassigned',
    email: 'unassigned@campus.edu',
    role: UserRole.STUDENT,
    fullName: 'Unassigned Student',
    profileId: 'std_unassigned'
  });

  beforeEach(() => {
    jest.restoreAllMocks();
    // Default mock for user validation in authenticate middleware
    jest.spyOn(prisma.user, 'findFirst').mockResolvedValue({
      id: 'any_user',
      isActive: true,
      role: 'STUDENT',
      studentProfile: { status: 'ACTIVE' }
    } as any);
  });

  it('1. Successfully returns only other roommates in the same room, excluding self', async () => {
    // Mock student profile with room
    jest.spyOn(prisma.student, 'findFirst').mockResolvedValueOnce({
      id: 'std_001',
      userId: 'std_user_001',
      fullName: 'Student One',
      rollNumber: 'ROLL-001',
      roomId: 'room_204',
      roomNumber: 'A-204',
      bedNumber: '1',
      hostelId: 'hostel_001',
      room: {
        id: 'room_204',
        roomNumber: 'A-204',
        floor: 2,
        block: 'A',
        totalCapacity: 3,
        occupiedCount: 3,
        roomType: 'TRIPLE',
        monthlyRent: 8000,
        amenities: '["AC", "Attached Bath"]'
      }
    } as any);

    // Mock other roommates in the room
    jest.spyOn(prisma.student, 'findMany').mockResolvedValueOnce([
      {
        id: 'std_002',
        fullName: 'Rahul Sharma',
        rollNumber: 'ROLL-002',
        course: 'B.Tech CSE',
        yearOfStudy: '3rd Year',
        bedNumber: '2',
        user: {
          phoneNumber: '+91 98765 43210'
        },
        emergencyContactPhone: '+91 98765 43211'
      },
      {
        id: 'std_003',
        fullName: 'Priya Patel',
        rollNumber: 'ROLL-003',
        course: 'B.Tech ECE',
        yearOfStudy: '3rd Year',
        bedNumber: '3',
        user: {
          phoneNumber: '+91 91234 56789'
        },
        emergencyContactPhone: '+91 91234 56780'
      }
    ] as any);

    const res = await request(app)
      .get('/api/students/my-roommates')
      .set('Authorization', `Bearer ${studentToken1}`);

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.room).toBeDefined();
    expect(res.body.data.room.roomNumber).toBe('A-204');
    expect(res.body.data.room.roomType).toBe('TRIPLE');
    expect(res.body.data.room.totalCapacity).toBe(3);
    expect(res.body.data.room.floor).toBe(2);
    expect(res.body.data.myBed).toBe('1');
    expect(res.body.data.roommates).toHaveLength(2);

    // Verify self is not in roommates list
    const roommateIds = res.body.data.roommates.map((r: any) => r.studentId);
    expect(roommateIds).not.toContain('std_001');
    expect(roommateIds).toContain('std_002');
    expect(roommateIds).toContain('std_003');

    // Verify phone numbers and details are returned
    expect(res.body.data.roommates[0].phoneNumber).toBe('+91 98765 43210');
    expect(res.body.data.roommates[0].bedNumber).toBe('2');
    expect(res.body.data.roommates[1].phoneNumber).toBe('+91 91234 56789');
    expect(res.body.data.roommates[1].bedNumber).toBe('3');
  });

  it('2. Returns empty roommates list when student is sole occupant of room', async () => {
    jest.spyOn(prisma.student, 'findFirst').mockResolvedValueOnce({
      id: 'std_001',
      userId: 'std_user_001',
      fullName: 'Student One',
      rollNumber: 'ROLL-001',
      roomId: 'room_101',
      roomNumber: 'Single-101',
      bedNumber: '1',
      hostelId: 'hostel_001',
      room: {
        id: 'room_101',
        roomNumber: 'Single-101',
        floor: 1,
        block: 'A',
        totalCapacity: 1,
        occupiedCount: 1,
        roomType: 'SINGLE',
        monthlyRent: 12000,
        amenities: []
      }
    } as any);

    jest.spyOn(prisma.student, 'findMany').mockResolvedValueOnce([]);

    const res = await request(app)
      .get('/api/students/my-roommates')
      .set('Authorization', `Bearer ${studentToken1}`);

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.room.roomNumber).toBe('Single-101');
    expect(res.body.data.roommates).toEqual([]);
  });

  it('3. Returns null room and empty roommates when student has no assigned room', async () => {
    jest.spyOn(prisma.student, 'findFirst').mockResolvedValueOnce({
      id: 'std_unassigned',
      userId: 'std_user_unassigned',
      fullName: 'Unassigned Student',
      roomId: null,
      roomNumber: null,
      bedNumber: null,
      hostelId: null,
      room: null
    } as any);

    const res = await request(app)
      .get('/api/students/my-roommates')
      .set('Authorization', `Bearer ${studentTokenNoRoom}`);

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.room).toBeNull();
    expect(res.body.data.roommates).toEqual([]);
  });

  it('4. Returns 401 when unauthenticated', async () => {
    const res = await request(app).get('/api/students/my-roommates');
    expect(res.status).toBe(401);
  });
});
