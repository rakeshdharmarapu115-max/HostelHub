import { Router } from 'express';
import { UsersController } from './users.controller';
import { authenticate } from '../../middleware/auth.middleware';
import { authorize } from '../../middleware/role.middleware';
import { UserRole } from '../../types/enums';

const router = Router();
const usersController = new UsersController();

router.use(authenticate);

router.get('/', authorize(UserRole.ADMIN), (req, res, next) => usersController.getAllUsers(req, res, next));
router.get('/:id', (req, res, next) => usersController.getUserById(req, res, next));
router.patch('/:id', (req, res, next) => usersController.updateUser(req, res, next));
router.patch('/:id/status', authorize(UserRole.ADMIN), (req, res, next) => usersController.toggleStatus(req, res, next));

export default router;
