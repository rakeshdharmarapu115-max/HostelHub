import { env } from './env';

export interface FirebaseAdminConfig {
  isConfigured: boolean;
  projectId?: string;
  clientEmail?: string;
}

const isConfigured = Boolean(
  (env.firebase.projectId && env.firebase.clientEmail && env.firebase.privateKey) ||
  env.firebase.serviceAccountPath
);

if (isConfigured) {
  console.log(`🔥 Firebase Cloud Messaging initialized for project: ${env.firebase.projectId}`);
} else {
  console.log('ℹ️ Firebase credentials not provided — Cloud push notifications running in dev simulation mode');
}

export const firebaseConfig: FirebaseAdminConfig = {
  isConfigured,
  projectId: env.firebase.projectId,
  clientEmail: env.firebase.clientEmail
};
