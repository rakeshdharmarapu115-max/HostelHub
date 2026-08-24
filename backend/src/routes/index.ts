import { Router } from 'express';
import authRoutes from '../modules/auth/auth.routes';
import usersRoutes from '../modules/users/users.routes';
import studentsRoutes from '../modules/students/students.routes';
import hostsRoutes from '../modules/hosts/hosts.routes';
import hostelsRoutes from '../modules/hostels/hostels.routes';
import roomsRoutes from '../modules/rooms/rooms.routes';
import allocationsRoutes from '../modules/allocations/allocations.routes';
import attendanceRoutes from '../modules/attendance/attendance.routes';
import feesRoutes from '../modules/fees/fees.routes';
import paymentsRoutes from '../modules/payments/payments.routes';
import complaintsRoutes from '../modules/complaints/complaints.routes';
import leaveRequestsRoutes from '../modules/leave-requests/leave-requests.routes';
import visitorsRoutes from '../modules/visitors/visitors.routes';
import foodMenuRoutes from '../modules/food-menu/food-menu.routes';
import announcementsRoutes from '../modules/announcements/announcements.routes';
import notificationsRoutes from '../modules/notifications/notifications.routes';
import dashboardRoutes from '../modules/dashboard/dashboard.routes';
import storageRoutes from '../modules/storage/storage.routes';

const router = Router();

router.use('/auth', authRoutes);
router.use('/users', usersRoutes);
router.use('/students', studentsRoutes);
router.use('/hosts', hostsRoutes);
router.use('/hostels', hostelsRoutes);
router.use('/rooms', roomsRoutes);
router.use('/allocations', allocationsRoutes);
router.use('/attendance', attendanceRoutes);
router.use('/fees', feesRoutes);
router.use('/payments', paymentsRoutes);
router.use('/complaints', complaintsRoutes);
router.use('/leave-requests', leaveRequestsRoutes);
router.use('/visitors', visitorsRoutes);
router.use('/food-menu', foodMenuRoutes);
router.use('/announcements', announcementsRoutes);
router.use('/notifications', notificationsRoutes);
router.use('/dashboard', dashboardRoutes);
router.use('/storage', storageRoutes);

router.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    timestamp: new Date().toISOString(),
    service: 'HostelHub API (Cloud Production)'
  });
});

export default router;
