import { Router } from 'express';
import { StudentsController } from './students.controller';
import { authenticate } from '../../middleware/auth.middleware';
import { authorize } from '../../middleware/role.middleware';
import { UserRole } from '../../types/enums';

const router = Router();
const studentsController = new StudentsController();

router.use(authenticate);

// Generate unique Student ID
router.get('/generate-id', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => studentsController.generateStudentId(req, res, next));

// Admin / Hostel Owner creates student with controlled Student ID
router.post('/admin-create', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => studentsController.createStudentByAdmin(req, res, next));
router.post('/', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => studentsController.createStudentByAdmin(req, res, next));

// Hostel Owner / Admin deallocates student
router.post('/:id/deallocate', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => studentsController.deallocateStudent(req, res, next));
router.post('/deallocate', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => studentsController.deallocateStudent(req, res, next));

router.get('/', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => studentsController.getAllStudents(req, res, next));
router.get('/hostel/:hostelId', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => studentsController.getResidentsByHostel(req, res, next));
router.get('/:id', (req, res, next) => studentsController.getStudentById(req, res, next));
router.patch('/:id', (req, res, next) => studentsController.updateStudentProfile(req, res, next));
router.delete('/:id', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => studentsController.deleteStudent(req, res, next));

export default router;
