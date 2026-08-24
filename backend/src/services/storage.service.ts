import { cloudinary, isCloudinaryConfigured } from '../config/storage.config';
import { env } from '../config/env';

export interface UploadResult {
  url: string;
  publicId?: string;
  format?: string;
  width?: number;
  height?: number;
  bytes?: number;
  provider: 'cloudinary' | 'fallback';
}

export class StorageService {
  /**
   * Uploads a file buffer directly to Cloudinary (or returns optimized data URI if offline/not configured)
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
    const folder = `${env.cloudinary.folder}/${options.folder || 'uploads'}`;

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
        console.error('Cloudinary upload error, using fallback:', err.message);
      }
    }

    // Fallback mode: inline base64 data URI for instant development & testing
    const mime = options.mimeType || 'image/jpeg';
    const base64Data = `data:${mime};base64,${fileBuffer.toString('base64')}`;

    return {
      url: base64Data,
      bytes: fileBuffer.length,
      provider: 'fallback'
    };
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
