import request from 'supertest';
import { app } from '../src/server';
import { prisma } from '../src/config/prisma';
import { generateAccessToken } from '../src/utils/jwt';
import { UserRole, PaymentStatusEnum, FeeStatusEnum } from '../src/types/enums';

describe('Hostel Owner QR Payment & Multi-Tenant Isolation Tests', () => {
  const studentToken1 = generateAccessToken({
    userId: 'std_user_001',
    email: 'student1@campus.edu',
    role: UserRole.STUDENT,
    fullName: 'Student One',
    profileId: 'std_001',
    hostelId: 'hostel_001'
  });

  const studentToken2 = generateAccessToken({
    userId: 'std_user_002',
    email: 'student2@campus.edu',
    role: UserRole.STUDENT,
    fullName: 'Student Two',
    profileId: 'std_002',
    hostelId: 'hostel_002'
  });

  const hostToken1 = generateAccessToken({
    userId: 'host_user_001',
    email: 'warden@greenvalley.edu',
    role: UserRole.HOST,
    fullName: 'Robert Vance',
    profileId: 'host_001',
    hostelId: 'hostel_001'
  });

  const adminToken = generateAccessToken({
    userId: 'admin_user_001',
    email: 'admin@campus.edu',
    role: UserRole.ADMIN,
    fullName: 'Dean Henderson',
    profileId: 'adm_001'
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

  it('1. Authenticated Student 1 receives Owner A QR config automatically', async () => {
    jest.spyOn(prisma.student, 'findFirst').mockResolvedValueOnce({
      id: 'std_001',
      userId: 'std_user_001',
      fullName: 'Student One',
      rollNumber: 'ROLL-001',
      roomNumber: 'A-204',
      hostelId: 'hostel_001',
      hostel: {
        id: 'hostel_001',
        name: 'Green Valley Residencies',
        hostId: 'host_001',
        paymentAccountId: 'acc_gv_987654',
        paymentAccountStatus: 'ACTIVE',
        paymentQrUrl: 'https://qr.hostelhub.com/greenvalley.png',
        qrPaymentEnabled: true,
        upiId: 'greenvalley.hostel@hdfcbank',
        merchantName: 'Green Valley Residencies',
        host: { fullName: 'Robert Vance', contactPhone: '555-0100', contactEmail: 'warden@greenvalley.edu' }
      },
      room: { roomNumber: 'A-204' },
      fees: [
        { id: 'fee_001', amount: 8000, amountPaid: 0, status: FeeStatusEnum.PENDING }
      ]
    } as any);

    const res = await request(app)
      .get('/api/payments/config/my-hostel')
      .set('Authorization', `Bearer ${studentToken1}`);

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.hostelId).toBe('hostel_001');
    expect(res.body.data.hostelName).toBe('Green Valley Residencies');
    expect(res.body.data.paymentQrUrl).toBe('https://qr.hostelhub.com/greenvalley.png');
    expect(res.body.data.upiId).toBe('greenvalley.hostel@hdfcbank');
    expect(res.body.data.studentRollNumber).toBe('ROLL-001');
    expect(res.body.data.roomNumber).toBe('A-204');
    expect(res.body.data.totalPendingDues).toBe(8000);
  });

  it('2. Multi-Tenant QR Isolation: Student 2 receives Owner B QR config', async () => {
    jest.spyOn(prisma.student, 'findFirst').mockResolvedValueOnce({
      id: 'std_002',
      userId: 'std_user_002',
      fullName: 'Student Two',
      rollNumber: 'ROLL-002',
      roomNumber: 'B-101',
      hostelId: 'hostel_002',
      hostel: {
        id: 'hostel_002',
        name: 'St. Jude Student Suites',
        hostId: 'host_002',
        paymentAccountId: 'acc_sj_334455',
        paymentAccountStatus: 'ACTIVE',
        paymentQrUrl: 'https://qr.hostelhub.com/stjude.png',
        qrPaymentEnabled: true,
        upiId: 'stjude.suites@icici',
        merchantName: 'St. Jude Student Suites',
        host: { fullName: 'Elena Rostova', contactPhone: '555-0200', contactEmail: 'warden@stjude.edu' }
      },
      room: { roomNumber: 'B-101' },
      fees: [
        { id: 'fee_002', amount: 5000, amountPaid: 1000, status: FeeStatusEnum.PARTIALLY_PAID }
      ]
    } as any);

    const res = await request(app)
      .get('/api/payments/config/my-hostel')
      .set('Authorization', `Bearer ${studentToken2}`);

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.hostelId).toBe('hostel_002');
    expect(res.body.data.hostelName).toBe('St. Jude Student Suites');
    expect(res.body.data.paymentQrUrl).toBe('https://qr.hostelhub.com/stjude.png');
    expect(res.body.data.upiId).toBe('stjude.suites@icici');
    expect(res.body.data.totalPendingDues).toBe(4000);
  });

  it('3. Hostel Owner can update payment QR settings', async () => {
    jest.spyOn(prisma.hostel, 'findUnique').mockResolvedValueOnce({
      id: 'hostel_001',
      hostId: 'host_001',
      paymentAccountId: 'acc_gv_987654',
      paymentAccountStatus: 'ACTIVE'
    } as any);

    jest.spyOn(prisma.host, 'findUnique').mockResolvedValueOnce({
      id: 'host_001',
      userId: 'host_user_001'
    } as any);

    jest.spyOn(prisma.hostel, 'update').mockResolvedValueOnce({
      id: 'hostel_001',
      name: 'Green Valley Residencies',
      hostId: 'host_001',
      paymentAccountId: 'acc_gv_NEW123',
      paymentAccountStatus: 'ACTIVE',
      paymentQrUrl: 'https://cdn.hostelhub.com/qr/gv_updated.png',
      qrPaymentEnabled: true,
      upiId: 'greenvalley.official@hdfcbank',
      merchantName: 'Green Valley Premium Residencies',
      host: { fullName: 'Robert Vance' }
    } as any);

    const res = await request(app)
      .put('/api/payments/config/hostel/hostel_001')
      .set('Authorization', `Bearer ${hostToken1}`)
      .send({
        paymentAccountId: 'acc_gv_NEW123',
        paymentQrUrl: 'https://cdn.hostelhub.com/qr/gv_updated.png',
        upiId: 'greenvalley.official@hdfcbank',
        merchantName: 'Green Valley Premium Residencies',
        qrPaymentEnabled: true
      });

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.paymentAccountId).toBe('acc_gv_NEW123');
    expect(res.body.data.upiId).toBe('greenvalley.official@hdfcbank');
    expect(res.body.data.paymentQrUrl).toBe('https://cdn.hostelhub.com/qr/gv_updated.png');
  });

  it('3b. Rejects invalid UPI ID format with 400 Bad Request', async () => {
    jest.spyOn(prisma.hostel, 'findUnique').mockResolvedValueOnce({
      id: 'hostel_001',
      hostId: 'host_001'
    } as any);

    jest.spyOn(prisma.host, 'findUnique').mockResolvedValueOnce({
      id: 'host_001',
      userId: 'host_user_001'
    } as any);

    const res = await request(app)
      .put('/api/payments/config/hostel/hostel_001')
      .set('Authorization', `Bearer ${hostToken1}`)
      .send({
        upiId: 'invalid-upi-without-handle',
        paymentQrUrl: 'https://cdn.hostelhub.com/qr/gv_updated.png'
      });

    expect(res.status).toBe(400);
    expect(res.body.success).toBe(false);
    expect(res.body.message).toContain('Invalid UPI ID format');
  });

  it('3c. Storage endpoint uploads QR image and returns accessible URL', async () => {
    const fakeImageBuffer = Buffer.from('fake-png-image-content');

    const res = await request(app)
      .post('/api/storage/payment-qr')
      .set('Authorization', `Bearer ${hostToken1}`)
      .attach('file', fakeImageBuffer, { filename: 'payment_qr.png', contentType: 'image/png' });

    expect(res.status).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.data.url).toMatch(/^\/uploads\/payment_qrs\//);
  });

  it('4. Student submits manual static QR payment with PENDING_VERIFICATION status', async () => {
    jest.spyOn(prisma.payment, 'findUnique').mockResolvedValueOnce(null); // No duplicate ref
    jest.spyOn(prisma.fee, 'findUnique').mockResolvedValueOnce({
      id: 'fee_001',
      amount: 8000,
      amountPaid: 0,
      studentId: 'std_001',
      hostelId: 'hostel_001',
      title: 'September 2026 Room Rent',
      student: { id: 'std_001', userId: 'std_user_001', fullName: 'Student One' },
      hostel: {
        id: 'hostel_001',
        host: { userId: 'host_user_001' }
      }
    } as any);

    jest.spyOn(prisma.payment, 'create').mockResolvedValueOnce({
      id: 'pay_manual_001',
      feeId: 'fee_001',
      studentId: 'std_001',
      hostelId: 'hostel_001',
      amountPaid: 8000,
      paymentMethod: 'UPI',
      paymentGateway: 'MANUAL_QR',
      transactionReference: 'UPI-UTR-987654321012',
      paymentDate: new Date(),
      receiptUrl: null,
      status: PaymentStatusEnum.PENDING_VERIFICATION,
      verifiedByHostId: null,
      remarks: 'Paid via GPay to Owner QR',
      createdAt: new Date(),
      student: { fullName: 'Student One' },
      fee: { title: 'September 2026 Room Rent' }
    } as any);

    jest.spyOn(prisma.notification, 'create').mockResolvedValue({} as any);

    const res = await request(app)
      .post('/api/payments/manual-qr')
      .set('Authorization', `Bearer ${studentToken1}`)
      .send({
        feeId: 'fee_001',
        amountPaid: 8000,
        transactionReference: 'UPI-UTR-987654321012',
        remarks: 'Paid via GPay to Owner QR'
      });

    expect(res.status).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.data.status).toBe(PaymentStatusEnum.PENDING_VERIFICATION);
    expect(res.body.data.paymentGateway).toBe('MANUAL_QR');
    expect(res.body.data.transactionReference).toBe('UPI-UTR-987654321012');
  });

  it('5. Hostel Owner verifies and approves manual payment, updating fee to PAID', async () => {
    const mockTx = {
      payment: {
        findUnique: jest.fn().mockResolvedValue({
          id: 'pay_manual_001',
          feeId: 'fee_001',
          studentId: 'std_001',
          hostelId: 'hostel_001',
          amountPaid: 8000,
          status: PaymentStatusEnum.PENDING_VERIFICATION,
          transactionReference: 'UPI-UTR-987654321012',
          fee: { id: 'fee_001', amount: 8000, amountPaid: 0, title: 'September 2026 Room Rent' },
          student: { id: 'std_001', userId: 'std_user_001', fullName: 'Student One' },
          hostel: { host: { id: 'host_001' } }
        }),
        update: jest.fn().mockResolvedValue({
          id: 'pay_manual_001',
          feeId: 'fee_001',
          studentId: 'std_001',
          hostelId: 'hostel_001',
          amountPaid: 8000,
          paymentMethod: 'UPI',
          paymentGateway: 'MANUAL_QR',
          transactionReference: 'UPI-UTR-987654321012',
          paymentDate: new Date(),
          status: PaymentStatusEnum.SUCCESS,
          verifiedByHostId: 'host_user_001',
          remarks: 'Verified in HDFC Bank Account',
          createdAt: new Date(),
          student: { fullName: 'Student One' },
          fee: { title: 'September 2026 Room Rent' }
        })
      },
      fee: {
        update: jest.fn().mockResolvedValue({
          id: 'fee_001',
          amountPaid: 8000,
          status: FeeStatusEnum.PAID
        })
      },
      notification: {
        create: jest.fn().mockResolvedValue({})
      }
    };

    jest.spyOn(prisma, '$transaction').mockImplementationOnce(async (callback: any) => {
      return callback(mockTx);
    });

    const res = await request(app)
      .patch('/api/payments/pay_manual_001/verify-manual')
      .set('Authorization', `Bearer ${hostToken1}`)
      .send({
        approved: true,
        remarks: 'Verified in HDFC Bank Account'
      });

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.status).toBe(PaymentStatusEnum.SUCCESS);
    expect(res.body.data.verifiedByHostId).toBe('host_user_001');
    expect(mockTx.fee.update).toHaveBeenCalledWith({
      where: { id: 'fee_001' },
      data: {
        amountPaid: 8000,
        status: FeeStatusEnum.PAID
      }
    });
  });

  it('6. Admin can view payment & collection overview across all hostels', async () => {
    jest.spyOn(prisma.hostel, 'findMany').mockResolvedValueOnce([
      {
        id: 'hostel_001',
        name: 'Green Valley Residencies',
        city: 'Academic City',
        hostId: 'host_001',
        paymentAccountId: 'acc_gv_987654',
        paymentAccountStatus: 'ACTIVE',
        paymentQrUrl: 'https://qr.hostelhub.com/greenvalley.png',
        qrPaymentEnabled: true,
        upiId: 'greenvalley.hostel@hdfcbank',
        merchantName: 'Green Valley Residencies',
        host: { fullName: 'Robert Vance', contactPhone: '555-0100', contactEmail: 'warden@greenvalley.edu' },
        payments: [
          { id: 'p1', amountPaid: 8000, status: PaymentStatusEnum.SUCCESS, paymentGateway: 'RAZORPAY' },
          { id: 'p2', amountPaid: 8000, status: PaymentStatusEnum.SUCCESS, paymentGateway: 'MANUAL_QR' },
          { id: 'p3', amountPaid: 8000, status: PaymentStatusEnum.PENDING_VERIFICATION, paymentGateway: 'MANUAL_QR' }
        ]
      },
      {
        id: 'hostel_002',
        name: 'St. Jude Student Suites',
        city: 'Academic City',
        hostId: 'host_002',
        paymentAccountId: 'acc_sj_334455',
        paymentAccountStatus: 'ACTIVE',
        paymentQrUrl: 'https://qr.hostelhub.com/stjude.png',
        qrPaymentEnabled: true,
        upiId: 'stjude.suites@icici',
        merchantName: 'St. Jude Student Suites',
        host: { fullName: 'Elena Rostova', contactPhone: '555-0200', contactEmail: 'warden@stjude.edu' },
        payments: [
          { id: 'p4', amountPaid: 5000, status: PaymentStatusEnum.SUCCESS, paymentGateway: 'RAZORPAY' }
        ]
      }
    ] as any);

    const res = await request(app)
      .get('/api/payments/admin/overview')
      .set('Authorization', `Bearer ${adminToken}`);

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(Array.isArray(res.body.data)).toBe(true);
    expect(res.body.data.length).toBe(2);

    const h1 = res.body.data[0];
    expect(h1.hostelName).toBe('Green Valley Residencies');
    expect(h1.ownerName).toBe('Robert Vance');
    expect(h1.qrConfigured).toBe(true);
    expect(h1.totalCollections).toBe(16000);
    expect(h1.pendingVerificationCount).toBe(1);

    const h2 = res.body.data[1];
    expect(h2.hostelName).toBe('St. Jude Student Suites');
    expect(h2.totalCollections).toBe(5000);
    expect(h2.pendingVerificationCount).toBe(0);
  });
});
