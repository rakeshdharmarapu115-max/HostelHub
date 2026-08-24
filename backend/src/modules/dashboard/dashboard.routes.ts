import { Router } from 'express';
import { DashboardController } from './dashboard.controller';
import { authenticate } from '../../middleware/auth.middleware';

const router = Router();
const dashboardController = new DashboardController();

router.use(authenticate);

router.get('/student', (req, res, next) => dashboardController.getStudentDashboard(req, res, next));
router.get('/host', (req, res, next) => dashboardController.getHostDashboard(req, res, next));
router.get('/admin', (req, res, next) => dashboardController.getAdminDashboard(req, res, next));

export default router;
