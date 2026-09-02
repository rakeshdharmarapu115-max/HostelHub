import { Router } from 'express';
import { HostelsController } from './hostels.controller';
import { authenticate } from '../../middleware/auth.middleware';
import { authorize } from '../../middleware/role.middleware';
import { UserRole } from '../../types/enums';

const router = Router();
const hostelsController = new HostelsController();

// Public / Authenticated read
router.get('/search', (req, res, next) => hostelsController.searchNearby(req, res, next));
router.get('/', (req, res, next) => hostelsController.getHostels(req, res, next));
router.get('/:id', (req, res, next) => hostelsController.getHostelById(req, res, next));
router.get('/:id/reviews', (req, res, next) => hostelsController.getReviews(req, res, next));

// Student rating & reviews
router.post('/:id/reviews', authenticate, (req, res, next) => hostelsController.addReview(req, res, next));

// Host/Admin operations, Location & Image uploads
router.put('/:id/location', authenticate, authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => hostelsController.updateLocation(req, res, next));
router.patch('/:id/location', authenticate, authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => hostelsController.updateLocation(req, res, next));
router.post('/:id/images', authenticate, authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => hostelsController.addHostelImages(req, res, next));
router.post('/', authenticate, authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => hostelsController.createHostel(req, res, next));
router.patch('/:id', authenticate, authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => hostelsController.updateHostel(req, res, next));
router.delete('/:id', authenticate, authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => hostelsController.deleteHostel(req, res, next));

export default router;
