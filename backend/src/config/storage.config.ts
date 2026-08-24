import { v2 as cloudinary } from 'cloudinary';
import { env } from './env';

const isCloudinaryConfigured = Boolean(
  env.cloudinary.cloudName &&
  env.cloudinary.apiKey &&
  env.cloudinary.apiSecret
);

if (isCloudinaryConfigured) {
  cloudinary.config({
    cloud_name: env.cloudinary.cloudName,
    api_key: env.cloudinary.apiKey,
    api_secret: env.cloudinary.apiSecret,
    secure: true
  });
  console.log('☁️ Cloudinary Cloud Storage initialized');
} else {
  console.log('ℹ️ Cloudinary credentials not configured — storage fallback enabled');
}

export { cloudinary, isCloudinaryConfigured };
