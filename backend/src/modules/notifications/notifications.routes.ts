import { Router } from 'express';
import { NotificationsController } from './notifications.controller';
import { authenticate } from '../../middleware/auth.middleware';

const router = Router();
const notificationsController = new NotificationsController();

router.use(authenticate);

router.get('/', (req, res, next) => notificationsController.getNotifications(req, res, next));
router.patch('/read-all', (req, res, next) => notificationsController.markAllAsRead(req, res, next));
router.patch('/:id/read', (req, res, next) => notificationsController.markAsRead(req, res, next));

export default router;
