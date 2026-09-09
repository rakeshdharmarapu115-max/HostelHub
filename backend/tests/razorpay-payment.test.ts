import request from 'supertest';
import crypto from 'crypto';
import { app } from '../src/server';
import { prisma } from '../src/config/prisma';
import { generateAccessToken } from '../src/utils/jwt';
import { UserRole, PaymentStatusEnum, FeeStatusEnum } from '../src/types/enums';

jest.setTimeout(30000);

describe('Online Razorpay Payment & Verification Tests', () => {
  const originalEnv = process.env;

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

  beforeEach(() => {
    jest.restoreAllMocks();
    process.env = {
      ...originalEnv,
      RAZORPAY_KEY_ID: 'rzp_test_1234567890ABCD',
      RAZORPAY_KEY_SECRET: 'test_secret_key_abcdef123456'
    };

    // User validation mock for authenticate middleware
    jest.spyOn(prisma.user, 'findFirst').mockResolvedValue({
      id: 'any_user',
      isActive: true,
      role: 'STUDENT',
      studentProfile: { status: 'ACTIVE' }
    } as any);
  });

  afterAll(() => {
    process.env = originalEnv;
  });

  it('A. Missing Razorpay credentials on server returns 503 Service Unavailable', async () => {
    delete process.env.RAZORPAY_KEY_ID;
    delete process.env.RAZORPAY_KEY_SECRET;

    const res = await request(app)
      .post('/api/payments/razorpay/create-order')
      .set('Authorization', `Bearer ${studentToken1}`)
      .send({ feeId: 'fee_001' });

    expect(res.status).toBe(503);
    expect(res.body.success).toBe(false);
    expect(res.body.message).toContain('Razorpay payment gateway credentials are not configured');
  });

  it('B. Non-existent fee ID returns 404 Not Found', async () => {
    jest.spyOn(prisma.fee, 'findUnique').mockResolvedValueOnce(null);

    const res = await request(app)
      .post('/api/payments/razorpay/create-order')
      .set('Authorization', `Bearer ${studentToken1}`)
      .send({ feeId: 'non_existent_fee' });

    expect(res.status).toBe(404);
    expect(res.body.success).toBe(false);
  });

  it('C. Student cannot create payment order for another student\'s fee (403 Forbidden)', async () => {
    jest.spyOn(prisma.fee, 'findUnique').mockResolvedValueOnce({
      id: 'fee_002',
      amount: 6000,
      amountPaid: 0,
      status: FeeStatusEnum.PENDING,
      studentId: 'std_002',
      student: { userId: 'std_user_002', fullName: 'Student Two' },
      hostel: { name: 'Hostel 2' }
    } as any);

    // Student 1 tries to pay for Student 2's fee
    const res = await request(app)
      .post('/api/payments/razorpay/create-order')
      .set('Authorization', `Bearer ${studentToken1}`)
      .send({ feeId: 'fee_002' });

    expect(res.status).toBe(403);
    expect(res.body.success).toBe(false);
    expect(res.body.message).toContain('You can only create payment orders for your own fee invoice');
  });

  it('D. Already fully paid fee returns 400 Bad Request', async () => {
    jest.spyOn(prisma.fee, 'findUnique').mockResolvedValueOnce({
      id: 'fee_001',
      amount: 8000,
      amountPaid: 8000,
      status: FeeStatusEnum.PAID,
      studentId: 'std_001',
      student: { userId: 'std_user_001', fullName: 'Student One' },
      hostel: { name: 'Hostel 1' }
    } as any);

    const res = await request(app)
      .post('/api/payments/razorpay/create-order')
      .set('Authorization', `Bearer ${studentToken1}`)
      .send({ feeId: 'fee_001' });

    expect(res.status).toBe(400);
    expect(res.body.success).toBe(false);
    expect(res.body.message).toContain('already been fully paid');
  });

  it('E. Creates real Razorpay order using database fee amount and returns public checkout details', async () => {
    jest.spyOn(prisma.fee, 'findUnique').mockResolvedValueOnce({
      id: 'fee_001',
      title: 'October 2026 Room Rent',
      amount: 8000,
      amountPaid: 3000, // Pending is 5000
      status: FeeStatusEnum.PARTIALLY_PAID,
      studentId: 'std_001',
      student: {
        id: 'std_001',
        userId: 'std_user_001',
        fullName: 'Student One',
        emergencyContactPhone: '+919876543210',
        user: { email: 'student1@campus.edu', phoneNumber: '+919876543210' }
      },
      hostel: { name: 'Green Valley Residencies' }
    } as any);

    // Mock global fetch for Razorpay API call
    const mockRazorpayResponse = {
      id: 'order_rzp_live_test_9999',
      amount: 500000,
      currency: 'INR',
      status: 'created'
    };
    (global as any).fetch = jest.fn().mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockRazorpayResponse
    });

    const res = await request(app)
      .post('/api/payments/razorpay/create-order')
      .set('Authorization', `Bearer ${studentToken1}`)
      .send({
        feeId: 'fee_001',
        amount: 999999 // Malicious client tries to send wrong amount - backend must ignore/calculate 5000
      });

    expect(res.status).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.data.orderId).toBe('order_rzp_live_test_9999');
    expect(res.body.data.amount).toBe(5000); // Strict pending amount from DB
    expect(res.body.data.amountInPaise).toBe(500000);
    expect(res.body.data.currency).toBe('INR');
    expect(res.body.data.keyId).toBe('rzp_test_1234567890ABCD');
    expect(res.body.data.feeId).toBe('fee_001');
    expect(res.body.data.studentName).toBe('Student One');
  });

  it('F. Signature Verification: Rejects payment when signature is invalid (400)', async () => {
    const res = await request(app)
      .post('/api/payments/razorpay/verify')
      .set('Authorization', `Bearer ${studentToken1}`)
      .send({
        feeId: 'fee_001',
        razorpayOrderId: 'order_rzp_live_test_9999',
        razorpayPaymentId: 'pay_rzp_live_test_1111',
        razorpaySignature: 'invalid_fraudulent_signature_hash'
      });

    expect(res.status).toBe(400);
    expect(res.body.success).toBe(false);
    expect(res.body.message).toContain('Invalid Razorpay payment signature');
  });

  it('G. Signature Verification: Successfully verifies cryptographic signature, records SUCCESS, and marks fee PAID', async () => {
    const orderId = 'order_rzp_live_test_9999';
    const paymentId = 'pay_rzp_live_test_1111';
    const secret = process.env.RAZORPAY_KEY_SECRET || 'test_secret_key_abcdef123456';

    const validSignature = crypto
      .createHmac('sha256', secret)
      .update(`${orderId}|${paymentId}`)
      .digest('hex');

    jest.spyOn(prisma.payment, 'findFirst').mockResolvedValueOnce(null); // Not already verified

    const mockTx = {
      fee: {
        findUnique: jest.fn().mockResolvedValue({
          id: 'fee_001',
          title: 'October 2026 Room Rent',
          amount: 5000,
          amountPaid: 0,
          studentId: 'std_001',
          hostelId: 'hostel_001',
          student: { id: 'std_001', userId: 'std_user_001', fullName: 'Student One', user: { email: 'student1@campus.edu' } },
          hostel: { id: 'hostel_001', name: 'Green Valley Residencies' }
        }),
        update: jest.fn().mockResolvedValue({
          id: 'fee_001',
          amountPaid: 5000,
          status: FeeStatusEnum.PAID
        })
      },
      payment: {
        create: jest.fn().mockResolvedValue({
          id: 'pay_db_001',
          feeId: 'fee_001',
          studentId: 'std_001',
          hostelId: 'hostel_001',
          amountPaid: 5000,
          paymentMethod: 'UPI',
          paymentGateway: 'RAZORPAY',
          transactionReference: paymentId,
          razorpayOrderId: orderId,
          razorpayPaymentId: paymentId,
          razorpaySignature: validSignature,
          paymentDate: new Date(),
          receiptUrl: `https://receipts.hostelhub.com/${paymentId}.pdf`,
          status: PaymentStatusEnum.SUCCESS,
          createdAt: new Date(),
          student: { fullName: 'Student One' },
          fee: { title: 'October 2026 Room Rent' }
        })
      },
      notification: { create: jest.fn().mockResolvedValue({}) },
      auditLog: { create: jest.fn().mockResolvedValue({}) }
    };

    jest.spyOn(prisma, '$transaction').mockImplementationOnce(async (callback: any) => {
      return callback(mockTx);
    });

    const res = await request(app)
      .post('/api/payments/razorpay/verify')
      .set('Authorization', `Bearer ${studentToken1}`)
      .send({
        feeId: 'fee_001',
        razorpayOrderId: orderId,
        razorpayPaymentId: paymentId,
        razorpaySignature: validSignature,
        amountPaid: 5000
      });

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.status).toBe(PaymentStatusEnum.SUCCESS);
    expect(res.body.data.razorpayPaymentId).toBe(paymentId);
    expect(res.body.data.razorpayOrderId).toBe(orderId);

    expect(mockTx.fee.update).toHaveBeenCalledWith({
      where: { id: 'fee_001' },
      data: {
        amountPaid: 5000,
        status: FeeStatusEnum.PAID
      }
    });
  });

  it('H. Idempotency: Replaying verified Razorpay payment returns existing record without double-crediting fee', async () => {
    const orderId = 'order_rzp_live_test_9999';
    const paymentId = 'pay_rzp_live_test_1111';
    const secret = process.env.RAZORPAY_KEY_SECRET || 'test_secret_key_abcdef123456';

    const validSignature = crypto
      .createHmac('sha256', secret)
      .update(`${orderId}|${paymentId}`)
      .digest('hex');

    // Simulate already verified payment in database
    jest.spyOn(prisma.payment, 'findFirst').mockResolvedValueOnce({
      id: 'pay_db_001',
      feeId: 'fee_001',
      studentId: 'std_001',
      hostelId: 'hostel_001',
      amountPaid: 5000,
      paymentMethod: 'UPI',
      paymentGateway: 'RAZORPAY',
      transactionReference: paymentId,
      razorpayOrderId: orderId,
      razorpayPaymentId: paymentId,
      razorpaySignature: validSignature,
      paymentDate: new Date(),
      status: PaymentStatusEnum.SUCCESS,
      createdAt: new Date(),
      student: { fullName: 'Student One' },
      fee: { title: 'October 2026 Room Rent' }
    } as any);

    const transactionSpy = jest.spyOn(prisma, '$transaction');

    const res = await request(app)
      .post('/api/payments/razorpay/verify')
      .set('Authorization', `Bearer ${studentToken1}`)
      .send({
        feeId: 'fee_001',
        razorpayOrderId: orderId,
        razorpayPaymentId: paymentId,
        razorpaySignature: validSignature
      });

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.paymentId).toBe('pay_db_001');
    expect(res.body.data.status).toBe(PaymentStatusEnum.SUCCESS);
    // Transaction should NOT have executed again
    expect(transactionSpy).not.toHaveBeenCalled();
  });

  it('I. Recording payment failure preserves fee as unpaid and logs failure record', async () => {
    jest.spyOn(prisma.fee, 'findUnique').mockResolvedValueOnce({
      id: 'fee_001',
      amount: 5000,
      amountPaid: 0,
      studentId: 'std_001',
      hostelId: 'hostel_001',
      student: { fullName: 'Student One' },
      title: 'October 2026 Room Rent'
    } as any);

    jest.spyOn(prisma.payment, 'create').mockResolvedValueOnce({
      id: 'pay_fail_001',
      feeId: 'fee_001',
      studentId: 'std_001',
      hostelId: 'hostel_001',
      amountPaid: 5000,
      paymentMethod: 'UPI',
      paymentGateway: 'RAZORPAY',
      transactionReference: 'FAIL-123456',
      razorpayOrderId: 'order_rzp_live_test_9999',
      razorpayPaymentId: null,
      paymentDate: new Date(),
      status: PaymentStatusEnum.FAILED,
      remarks: 'Payment failed on gateway: Bank server timeout',
      createdAt: new Date(),
      student: { fullName: 'Student One' },
      fee: { title: 'October 2026 Room Rent' }
    } as any);

    const res = await request(app)
      .post('/api/payments/razorpay/failed')
      .set('Authorization', `Bearer ${studentToken1}`)
      .send({
        feeId: 'fee_001',
        razorpayOrderId: 'order_rzp_live_test_9999',
        errorMessage: 'Bank server timeout'
      });

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.status).toBe(PaymentStatusEnum.FAILED);
  });
});
