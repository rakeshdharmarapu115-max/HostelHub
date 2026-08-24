import { Router } from 'express';
import { StudentsController } from './students.controller';
import { authenticate } from '../../middleware/auth.middleware';
import { authorize } from '../../middleware/role.middleware';
import { UserRole } from '../../types/enums';

const router = Router();
const studentsController = new StudentsController();

router.use(authenticate);

router.get('/', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => studentsController.getAllStudents(req, res, next));
router.get('/hostel/:hostelId', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => studentsController.getResidentsByHostel(req, res, next));
router.get('/:id', (req, res, next) => studentsController.getStudentById(req, res, next));
router.patch('/:id', (req, res, next) => studentsController.updateStudentProfile(req, res, next));
router.delete('/:id', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => studentsController.deleteStudent(req, res, next));

export default router;
