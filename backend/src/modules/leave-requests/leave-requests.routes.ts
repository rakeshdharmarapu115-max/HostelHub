import { Router } from 'express';
import { LeaveRequestsController } from './leave-requests.controller';
import { authenticate } from '../../middleware/auth.middleware';
import { authorize } from '../../middleware/role.middleware';
import { UserRole } from '../../types/enums';

const router = Router();
const leaveRequestsController = new LeaveRequestsController();

router.use(authenticate);

router.get('/student/:studentId', (req, res, next) => leaveRequestsController.getLeaveRequestsForStudent(req, res, next));
router.get('/hostel/:hostelId', (req, res, next) => leaveRequestsController.getLeaveRequestsForHostel(req, res, next));
router.post('/', (req, res, next) => leaveRequestsController.createLeaveRequest(req, res, next));
router.patch('/:id/status', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => leaveRequestsController.updateLeaveStatus(req, res, next));

export default router;
