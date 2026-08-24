import { Router } from 'express';
import { HostsController } from './hosts.controller';
import { authenticate } from '../../middleware/auth.middleware';
import { authorize } from '../../middleware/role.middleware';
import { UserRole } from '../../types/enums';

const router = Router();
const hostsController = new HostsController();

router.use(authenticate);

router.get('/', authorize(UserRole.ADMIN), (req, res, next) => hostsController.getAllHosts(req, res, next));
router.get('/:id', (req, res, next) => hostsController.getHostById(req, res, next));
router.patch('/:id/verify', authorize(UserRole.ADMIN), (req, res, next) => hostsController.verifyHost(req, res, next));

export default router;
