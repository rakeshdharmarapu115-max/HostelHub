import { Router } from 'express';
import { FoodMenuController } from './food-menu.controller';
import { authenticate } from '../../middleware/auth.middleware';
import { authorize } from '../../middleware/role.middleware';
import { UserRole } from '../../types/enums';

const router = Router();
const foodMenuController = new FoodMenuController();

router.get('/', (req, res, next) => foodMenuController.getWeeklyMenu(req, res, next));
router.get('/:hostelId', (req, res, next) => foodMenuController.getWeeklyMenu(req, res, next));

router.post('/', authenticate, authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => foodMenuController.createOrUpdateMenu(req, res, next));
router.patch('/:id', authenticate, authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => foodMenuController.createOrUpdateMenu(req, res, next));
router.delete('/:id', authenticate, authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => foodMenuController.deleteMenu(req, res, next));

export default router;
