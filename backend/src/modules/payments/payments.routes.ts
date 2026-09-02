import { Router } from 'express';
import { PaymentsController } from './payments.controller';
import { authenticate } from '../../middleware/auth.middleware';

const router = Router();
const paymentsController = new PaymentsController();

router.use(authenticate);

router.post('/razorpay/create-order', (req, res, next) => paymentsController.createRazorpayOrder(req, res, next));
router.post('/razorpay/verify', (req, res, next) => paymentsController.verifyRazorpayPayment(req, res, next));
router.post('/razorpay/failed', (req, res, next) => paymentsController.recordPaymentFailure(req, res, next));
router.get('/history', (req, res, next) => paymentsController.getTransactionHistory(req, res, next));

router.get('/student/:studentId', (req, res, next) => paymentsController.getPaymentsForStudent(req, res, next));
router.get('/hostel/:hostelId', (req, res, next) => paymentsController.getPaymentsForHostel(req, res, next));
router.get('/:id', (req, res, next) => paymentsController.getPaymentById(req, res, next));
router.post('/', (req, res, next) => paymentsController.recordPayment(req, res, next));

export default router;
