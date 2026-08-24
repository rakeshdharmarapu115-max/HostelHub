import { Router } from 'express';
import { RoomsController } from './rooms.controller';
import { authenticate } from '../../middleware/auth.middleware';
import { authorize } from '../../middleware/role.middleware';
import { UserRole } from '../../types/enums';

const router = Router();
const roomsController = new RoomsController();

router.use(authenticate);

// Room routes
router.get('/hostel/:hostelId', (req, res, next) => roomsController.getRoomsByHostel(req, res, next));
router.post('/hostel/:hostelId', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => roomsController.addRoom(req, res, next));
router.get('/:id', (req, res, next) => roomsController.getRoomById(req, res, next));
router.patch('/:id', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => roomsController.updateRoom(req, res, next));
router.delete('/:id', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => roomsController.deleteRoom(req, res, next));

// Bed routes
router.get('/:roomId/beds', (req, res, next) => roomsController.getBedsByRoom(req, res, next));
router.post('/:roomId/beds', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => roomsController.addBed(req, res, next));
router.delete('/beds/:id', authorize(UserRole.ADMIN, UserRole.HOST), (req, res, next) => roomsController.deleteBed(req, res, next));

export default router;
