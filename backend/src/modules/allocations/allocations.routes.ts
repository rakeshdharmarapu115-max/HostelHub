import { Router } from 'express';
import { AllocationsController } from './allocations.controller';
import { authenticate } from '../../middleware/auth.middleware';
import { authorize } from '../../middleware/role.middleware';
import { UserRole } from '../../types/enums';

const router = Router();
const allocationsController = new AllocationsController();

router.use(authenticate);

router.post('/', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => allocationsController.allocateBed(req, res, next));
router.patch('/:id/checkout', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => allocationsController.vacateBed(req, res, next));
router.post('/vacate', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => allocationsController.vacateBed(req, res, next));
router.get('/student/:studentId', (req, res, next) => allocationsController.getAllocationsByStudent(req, res, next));

export default router;
