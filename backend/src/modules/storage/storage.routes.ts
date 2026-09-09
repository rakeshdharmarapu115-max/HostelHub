import { Router } from 'express';
import multer from 'multer';
import { StorageController } from './storage.controller';
import { authenticate } from '../../middleware/auth.middleware';

const router = Router();
const controller = new StorageController();

// Use memory storage so files are streamed directly to Cloudinary or memory buffers
const upload = multer({
  storage: multer.memoryStorage(),
  limits: {
    fileSize: 10 * 1024 * 1024 // 10MB maximum file size
  },
  fileFilter: (req, file, cb) => {
    if (file.mimetype.startsWith('image/') || file.mimetype === 'application/pdf') {
      cb(null, true);
    } else {
      cb(new Error('Only image files (JPEG, PNG, WEBP, GIF) and PDFs are allowed'));
    }
  }
});

// Storage Service Health & Status (Public)
router.get('/status', (req, res) => controller.getStorageStatus(req, res));

// Upload general image / file
router.post('/upload', authenticate, upload.any(), (req, res, next) => {
  console.log('[QR_UPLOAD_ROUTE_HIT] POST /storage/upload');
  controller.uploadSingle(req, res, next);
});

// Upload user avatar
router.post('/avatar', authenticate, upload.any(), (req, res, next) => controller.uploadAvatar(req, res, next));

// Upload payment receipt
router.post('/receipt', authenticate, upload.any(), (req, res, next) => controller.uploadReceipt(req, res, next));

// Upload hostel room / building gallery images
router.post('/hostel-images', authenticate, upload.array('images', 10), (req, res, next) => controller.uploadHostelImages(req, res, next));

// Upload hostel owner official payment QR code (supports multiple paths & field names)
router.post('/payment-qr', authenticate, upload.any(), (req, res, next) => {
  console.log('[QR_UPLOAD_ROUTE_HIT] POST /storage/payment-qr');
  controller.uploadPaymentQr(req, res, next);
});

router.post('/qr', authenticate, upload.any(), (req, res, next) => {
  console.log('[QR_UPLOAD_ROUTE_HIT] POST /storage/qr');
  controller.uploadPaymentQr(req, res, next);
});

router.post('/payment/qr', authenticate, upload.any(), (req, res, next) => {
  console.log('[QR_UPLOAD_ROUTE_HIT] POST /storage/payment/qr');
  controller.uploadPaymentQr(req, res, next);
});

export default router;

