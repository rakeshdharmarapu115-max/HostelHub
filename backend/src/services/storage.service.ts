import { cloudinary, isCloudinaryConfigured } from '../config/storage.config';
import { env } from '../config/env';
import * as fs from 'fs';
import * as path from 'path';

export interface UploadResult {
  url: string;
  publicId?: string;
  format?: string;
  width?: number;
  height?: number;
  bytes?: number;
  provider: 'cloudinary' | 'local' | 'fallback';
}

export class StorageService {
  /**
   * Uploads a file buffer directly to Cloudinary (or stores locally in uploads directory if offline/not configured)
   */
  async uploadFile(
    fileBuffer: Buffer,
    options: {
      folder?: string;
      filename?: string;
      mimeType?: string;
      tags?: string[];
      transformation?: any[];
    } = {}
  ): Promise<UploadResult> {
    const targetSubfolder = options.folder || 'uploads';
    const folder = `${env.cloudinary.folder}/${targetSubfolder}`;

    if (isCloudinaryConfigured) {
      try {
        const uploadResponse = await new Promise<any>((resolve, reject) => {
          const uploadStream = cloudinary.uploader.upload_stream(
            {
              folder,
              public_id: options.filename ? options.filename.replace(/\.[^/.]+$/, '') : undefined,
              resource_type: 'auto',
              transformation: options.transformation || [
                { quality: 'auto', fetch_format: 'auto' }
              ],
              tags: options.tags || ['hostelhub']
            },
            (error, result) => {
              if (error) return reject(error);
              resolve(result);
            }
          );
          uploadStream.end(fileBuffer);
        });

        return {
          url: uploadResponse.secure_url,
          publicId: uploadResponse.public_id,
          format: uploadResponse.format,
          width: uploadResponse.width,
          height: uploadResponse.height,
          bytes: uploadResponse.bytes,
          provider: 'cloudinary'
        };
      } catch (err: any) {
        console.error('Cloudinary upload error, using local storage fallback:', err.message);
      }
    }

    // Local Disk Storage fallback
    try {
      const uploadsDir = path.join(process.cwd(), 'uploads', targetSubfolder);
      if (!fs.existsSync(uploadsDir)) {
        fs.mkdirSync(uploadsDir, { recursive: true });
      }

      const ext = options.mimeType ? (options.mimeType.split('/')[1] || 'png') : 'png';
      const cleanExt = ext.replace('jpeg', 'jpg');
      const safeFilename = options.filename || `file_${Date.now()}_${Math.random().toString(36).substring(2, 8)}.${cleanExt}`;
      const filePath = path.join(uploadsDir, safeFilename);

      fs.writeFileSync(filePath, fileBuffer);

      const localUrl = `/uploads/${targetSubfolder}/${safeFilename}`;
      return {
        url: localUrl,
        publicId: safeFilename,
        format: cleanExt,
        bytes: fileBuffer.length,
        provider: 'local'
      };
    } catch (diskErr: any) {
      console.error('Disk storage error, using base64 fallback:', diskErr.message);
      const mime = options.mimeType || 'image/png';
      const base64Data = `data:${mime};base64,${fileBuffer.toString('base64')}`;
      return {
        url: base64Data,
        bytes: fileBuffer.length,
        provider: 'fallback'
      };
    }
  }

  /**
   * Upload an image from base64 string or remote URL
   */
  async uploadBase64OrUrl(
    dataUriOrUrl: string,
    folder: string = 'general'
  ): Promise<UploadResult> {
    if (dataUriOrUrl.startsWith('http://') || dataUriOrUrl.startsWith('https://')) {
      // If already a remote URL (like Cloudinary, Unsplash, or S3), return directly or upload to Cloudinary
      if (isCloudinaryConfigured && !dataUriOrUrl.includes('cloudinary.com')) {
        try {
          const result = await cloudinary.uploader.upload(dataUriOrUrl, {
            folder: `${env.cloudinary.folder}/${folder}`,
            transformation: [{ quality: 'auto', fetch_format: 'auto' }]
          });
          return {
            url: result.secure_url,
            publicId: result.public_id,
            format: result.format,
            bytes: result.bytes,
            provider: 'cloudinary'
          };
        } catch {
          return { url: dataUriOrUrl, provider: 'fallback' };
        }
      }
      return { url: dataUriOrUrl, provider: 'fallback' };
    }

    if (isCloudinaryConfigured && dataUriOrUrl.startsWith('data:')) {
      try {
        const result = await cloudinary.uploader.upload(dataUriOrUrl, {
          folder: `${env.cloudinary.folder}/${folder}`,
          transformation: [{ quality: 'auto', fetch_format: 'auto' }]
        });
        return {
          url: result.secure_url,
          publicId: result.public_id,
          format: result.format,
          bytes: result.bytes,
          provider: 'cloudinary'
        };
      } catch (err: any) {
        console.error('Cloudinary base64 upload failed, using fallback URI:', err.message);
      }
    }

    return {
      url: dataUriOrUrl,
      provider: 'fallback'
    };
  }

  /**
   * Delete an asset from Cloud Storage
   */
  async deleteFile(publicId: string): Promise<boolean> {
    if (!isCloudinaryConfigured || !publicId) return false;
    try {
      const result = await cloudinary.uploader.destroy(publicId);
      return result.result === 'ok';
    } catch (err) {
      console.error('Failed to delete asset from Cloudinary:', err);
      return false;
    }
  }
}

export const storageService = new StorageService();
