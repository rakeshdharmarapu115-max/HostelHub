import { Router } from 'express';
import { VisitorsController } from './visitors.controller';
import { authenticate } from '../../middleware/auth.middleware';
import { authorize } from '../../middleware/role.middleware';
import { UserRole } from '../../types/enums';

const router = Router();
const visitorsController = new VisitorsController();

router.use(authenticate);

router.get('/hostel/:hostelId', (req, res, next) => visitorsController.getVisitorsForHostel(req, res, next));
router.get('/student/:studentId', (req, res, next) => visitorsController.getVisitorsForStudent(req, res, next));
router.post('/', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => visitorsController.registerVisitor(req, res, next));
router.patch('/:id/checkout', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => visitorsController.checkoutVisitor(req, res, next));

export default router;
