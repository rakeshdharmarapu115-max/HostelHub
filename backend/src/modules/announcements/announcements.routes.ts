import { Router } from 'express';
import { AnnouncementsController } from './announcements.controller';
import { authenticate } from '../../middleware/auth.middleware';
import { authorize } from '../../middleware/role.middleware';
import { UserRole } from '../../types/enums';

const router = Router();
const announcementsController = new AnnouncementsController();

router.use(authenticate);

router.get('/', (req, res, next) => announcementsController.getAnnouncements(req, res, next));
router.get('/:id', (req, res, next) => announcementsController.getAnnouncementById(req, res, next));
router.post('/', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => announcementsController.createAnnouncement(req, res, next));
router.delete('/:id', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => announcementsController.deleteAnnouncement(req, res, next));

export default router;
