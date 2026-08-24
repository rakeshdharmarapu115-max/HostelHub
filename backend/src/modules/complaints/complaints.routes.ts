import { Router } from 'express';
import { ComplaintsController } from './complaints.controller';
import { authenticate } from '../../middleware/auth.middleware';
import { authorize } from '../../middleware/role.middleware';
import { UserRole } from '../../types/enums';

const router = Router();
const complaintsController = new ComplaintsController();

router.use(authenticate);

router.get('/student/:studentId', (req, res, next) => complaintsController.getComplaintsForStudent(req, res, next));
router.get('/hostel/:hostelId', (req, res, next) => complaintsController.getComplaintsForHostel(req, res, next));
router.get('/', authorize(UserRole.ADMIN), (req, res, next) => complaintsController.getAllComplaints(req, res, next));
router.get('/:id', (req, res, next) => complaintsController.getComplaintById(req, res, next));
router.post('/', (req, res, next) => complaintsController.submitComplaint(req, res, next));
router.patch('/:id', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => complaintsController.updateComplaintStatus(req, res, next));
router.delete('/:id', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => complaintsController.deleteComplaint(req, res, next));

export default router;
