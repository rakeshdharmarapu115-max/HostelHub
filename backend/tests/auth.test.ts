import request from 'supertest';
import { app } from '../src/server';
import { prisma } from '../src/config/prisma';

describe('Auth & API Endpoints', () => {
  it('GET /health should return 200 OK', async () => {
    const res = await request(app).get('/health');
    expect(res.status).toBe(200);
    expect(res.body.status).toBe('ok');
    expect(res.body.service).toBe('HostelHub Backend');
  });

  it('POST /api/auth/login with missing fields should return 400 Validation Error', async () => {
    const res = await request(app)
      .post('/api/auth/login')
      .send({ email: 'invalid-email' });

    expect(res.status).toBe(400);
    expect(res.body.success).toBe(false);
    expect(res.body.errors).toBeDefined();
  });

  it('GET /api/users without token should return 401 Unauthorized', async () => {
    const res = await request(app).get('/api/users');
    expect(res.status).toBe(401);
    expect(res.body.success).toBe(false);
  });

  it('GET /api/hostels should return list of hostels', async () => {
    // Mock database query for unit test isolation
    jest.spyOn(prisma.hostel, 'findMany').mockResolvedValueOnce([
      {
        id: 'hostel_001',
        name: 'Green Valley Residences',
        city: 'Academic City',
        state: 'State',
        genderType: 'COED',
        baseMonthlyRent: 450,
        cautionDeposit: 200,
        rating: 4.8,
        ratingCount: 120,
        totalRooms: 30,
        totalBeds: 60,
        occupiedBeds: 50,
        amenities: '["Wi-Fi"]',
        rules: '[]',
        images: '[]',
        hostId: 'host_001',
        host: { fullName: 'Robert Vance', businessName: 'GV Residences', contactPhone: '555-0100', contactEmail: 'host@gv.com' },
        rooms: [],
        createdAt: new Date(),
        updatedAt: new Date()
      } as any
    ]);

    const res = await request(app).get('/api/hostels');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(Array.isArray(res.body.data)).toBe(true);
    expect(res.body.data.length).toBe(1);
    expect(res.body.data[0].name).toBe('Green Valley Residences');
  });

  it('GET /api/storage/status should return 200 with cloud storage capability', async () => {
    const res = await request(app).get('/api/storage/status');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.activeProvider).toBeDefined();
  });
});
