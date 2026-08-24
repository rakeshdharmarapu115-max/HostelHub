import { Router } from 'express';
import { FeesController } from './fees.controller';
import { authenticate } from '../../middleware/auth.middleware';
import { authorize } from '../../middleware/role.middleware';
import { UserRole } from '../../types/enums';

const router = Router();
const feesController = new FeesController();

router.use(authenticate);

router.get('/student/:studentId', (req, res, next) => feesController.getFeesForStudent(req, res, next));
router.get('/hostel/:hostelId', (req, res, next) => feesController.getFeesForHostel(req, res, next));
router.get('/', authorize(UserRole.ADMIN), (req, res, next) => feesController.getAllFees(req, res, next));
router.post('/', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => feesController.createFee(req, res, next));
router.patch('/:id', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => feesController.updateFee(req, res, next));

export default router;
