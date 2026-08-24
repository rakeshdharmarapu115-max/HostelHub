import { Router } from 'express';
import { AttendanceController } from './attendance.controller';
import { authenticate } from '../../middleware/auth.middleware';

const router = Router();
const attendanceController = new AttendanceController();

router.use(authenticate);

router.get('/student/:studentId', (req, res, next) => attendanceController.getAttendanceForStudent(req, res, next));
router.get('/hostel/:hostelId', (req, res, next) => attendanceController.getAttendanceForHostel(req, res, next));
router.post('/', (req, res, next) => attendanceController.markAttendance(req, res, next));
router.post('/batch', (req, res, next) => attendanceController.markBatchAttendance(req, res, next));

export default router;
