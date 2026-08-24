import { Request, Response, NextFunction } from 'express';
import { storageService } from '../../services/storage.service';
import { sendSuccess, sendError } from '../../utils/apiResponse';
import { isCloudinaryConfigured } from '../../config/storage.config';

export class StorageController {
  async getStorageStatus(req: Request, res: Response): Promise<void> {
    sendSuccess(res, 'Storage service status retrieved', {
      configured: isCloudinaryConfigured,
      activeProvider: isCloudinaryConfigured ? 'cloudinary' : 'local_data_uri',
      supportedFeatures: [
        'Image Auto-Optimization (WebP/AVIF)',
        'CDN Fast Delivery',
        'Responsive Image Transformations',
        'Automatic Thumbnails'
      ]
    });
  }

  async uploadSingle(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const file = (req as any).file;
      const folder = (req.body.folder as string) || 'general';

      if (!file) {
        // Check if base64 image or url passed in body
        if (req.body.image || req.body.url || req.body.data) {
          const raw = req.body.image || req.body.url || req.body.data;
          const result = await storageService.uploadBase64OrUrl(raw, folder);
          sendSuccess(res, 'Image processed successfully', result);
          return;
        }
        sendError(res, 'No file uploaded or image data provided', 400);
        return;
      }

      const result = await storageService.uploadFile(file.buffer, {
        folder,
        filename: file.originalname,
        mimeType: file.mimetype
      });

      sendSuccess(res, 'File uploaded successfully', result, 201);
    } catch (error) {
      next(error);
    }
  }

  async uploadAvatar(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const file = (req as any).file;
      if (!file && !req.body.image) {
        sendError(res, 'Avatar image is required', 400);
        return;
      }

      let result;
      if (file) {
        result = await storageService.uploadFile(file.buffer, {
          folder: 'avatars',
          filename: file.originalname,
          mimeType: file.mimetype,
          transformation: [
            { width: 400, height: 400, crop: 'fill', gravity: 'face' },
            { quality: 'auto', fetch_format: 'auto' }
          ]
        });
      } else {
        result = await storageService.uploadBase64OrUrl(req.body.image, 'avatars');
      }

      sendSuccess(res, 'Avatar uploaded and optimized successfully', result, 201);
    } catch (error) {
      next(error);
    }
  }

  async uploadReceipt(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const file = (req as any).file;
      if (!file && !req.body.image) {
        sendError(res, 'Receipt image is required', 400);
        return;
      }

      let result;
      if (file) {
        result = await storageService.uploadFile(file.buffer, {
          folder: 'receipts',
          filename: file.originalname,
          mimeType: file.mimetype
        });
      } else {
        result = await storageService.uploadBase64OrUrl(req.body.image, 'receipts');
      }

      sendSuccess(res, 'Payment receipt uploaded successfully', result, 201);
    } catch (error) {
      next(error);
    }
  }

  async uploadHostelImages(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const files = (req as any).files as Express.Multer.File[];
      const urls: string[] = [];

      if (files && files.length > 0) {
        for (const file of files) {
          const uploaded = await storageService.uploadFile(file.buffer, {
            folder: 'hostels',
            filename: file.originalname,
            mimeType: file.mimetype,
            transformation: [
              { width: 1200, crop: 'limit' },
              { quality: 'auto', fetch_format: 'auto' }
            ]
          });
          urls.push(uploaded.url);
        }
      } else if (Array.isArray(req.body.images)) {
        for (const img of req.body.images) {
          const uploaded = await storageService.uploadBase64OrUrl(img, 'hostels');
          urls.push(uploaded.url);
        }
      } else if (req.body.imageUrl || req.body.image) {
        const uploaded = await storageService.uploadBase64OrUrl(req.body.imageUrl || req.body.image, 'hostels');
        urls.push(uploaded.url);
      }

      if (urls.length === 0) {
        sendError(res, 'No hostel images provided', 400);
        return;
      }

      sendSuccess(res, `${urls.length} hostel image(s) processed successfully`, {
        urls,
        count: urls.length
      }, 201);
    } catch (error) {
      next(error);
    }
  }
}
