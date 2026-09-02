import { Router } from 'express';
import { AuthController } from './auth.controller';
import { validate } from '../../middleware/validate.middleware';
import { authenticate } from '../../middleware/auth.middleware';
import { registerStudentSchema, registerHostSchema, registerAdminSchema, loginSchema, refreshTokenSchema } from './auth.schema';

const router = Router();
const authController = new AuthController();

router.post('/register/student', validate(registerStudentSchema), (req, res, next) => authController.registerStudent(req, res, next));
router.post('/register/host', validate(registerHostSchema), (req, res, next) => authController.registerHost(req, res, next));
router.post('/register/admin', validate(registerAdminSchema), (req, res, next) => authController.registerAdmin(req, res, next));
router.post('/validate-student-id', (req, res, next) => authController.validateStudentId(req, res, next));
router.post('/activate-student', (req, res, next) => authController.activateStudent(req, res, next));
router.post('/forgot-password', (req, res, next) => authController.forgotPassword(req, res, next));
router.post('/reset-password', (req, res, next) => authController.resetPassword(req, res, next));
router.post('/login', (req, res, next) => authController.login(req, res, next));
router.post('/refresh', validate(refreshTokenSchema), (req, res, next) => authController.refresh(req, res, next));
router.post('/logout', (req, res, next) => authController.logout(req, res, next));
router.get('/me', authenticate, (req, res, next) => authController.getMe(req, res, next));

export default router;
