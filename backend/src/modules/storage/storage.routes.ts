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
router.post('/upload', authenticate, upload.single('file'), (req, res, next) => controller.uploadSingle(req, res, next));

// Upload user avatar
router.post('/avatar', authenticate, upload.single('avatar'), (req, res, next) => controller.uploadAvatar(req, res, next));

// Upload payment receipt
router.post('/receipt', authenticate, upload.single('receipt'), (req, res, next) => controller.uploadReceipt(req, res, next));

// Upload hostel room / building gallery images
router.post('/hostel-images', authenticate, upload.array('images', 10), (req, res, next) => controller.uploadHostelImages(req, res, next));

export default router;
