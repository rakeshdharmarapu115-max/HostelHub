import { prisma } from '../config/prisma';
import { env } from '../config/env';
import { isCloudinaryConfigured } from '../config/storage.config';
import { firebaseConfig } from '../config/firebase.config';

async function diagnose() {
  console.log('====================================================');
  console.log('🔍 HOSTELHUB CLOUD & DATABASE CONNECTIVITY DIAGNOSTIC');
  console.log('====================================================\n');

  // 1. Check Database URL
  console.log('[1/4] Checking PostgreSQL Cloud Database Configuration...');
  const dbUrl = env.databaseUrl;
  const isCloudDb = dbUrl.includes('render.com') || dbUrl.includes('neon.tech') || dbUrl.includes('supabase.co') || dbUrl.includes('rds.amazonaws.com');
  console.log(`  - Target: ${isCloudDb ? '☁️ Cloud Hosted PostgreSQL' : '💻 Local Database'}`);
  console.log(`  - Host: ${dbUrl.replace(/:[^:@]+@/, ':****@')}`);

  try {
    const start = Date.now();
    await prisma.$connect();
    const result = await prisma.$queryRaw`SELECT NOW() as current_time, version() as pg_version`;
    const latency = Date.now() - start;
    console.log(`  ✅ Database Connected successfully! Latency: ${latency}ms`);
    console.log(`  - Server Info:`, result);

    const userCount = await prisma.user.count();
    const hostelCount = await prisma.hostel.count();
    const roomCount = await prisma.room.count();
    const studentCount = await prisma.student.count();

    console.log(`\n  📊 Database Summary:`);
    console.log(`    - Users: ${userCount}`);
    console.log(`    - Hostels: ${hostelCount}`);
    console.log(`    - Rooms: ${roomCount}`);
    console.log(`    - Students: ${studentCount}`);

    if (userCount > 0) {
      const users = await prisma.user.findMany({ select: { email: true, role: true, fullName: true } });
      console.log('\n  👥 Available Accounts:');
      users.forEach(u => console.log(`    * [${u.role}] ${u.fullName} (${u.email})`));
    }
  } catch (err: any) {
    console.error(`  ❌ Database Connection Failed:`, err.message);
  }

  // 2. Check Storage
  console.log('\n[2/4] Checking Cloud Storage (Cloudinary)...');
  if (isCloudinaryConfigured) {
    console.log('  ✅ Cloudinary is configured & active for cloud asset delivery');
  } else {
    console.log('  ℹ️ Cloudinary credentials not set -> Fast fallback data-URI storage active (offline/dev ready)');
  }

  // 3. Check Push Notifications
  console.log('\n[3/4] Checking Push Notifications (Firebase)...');
  if (firebaseConfig.isConfigured) {
    console.log('  ✅ Firebase Cloud Messaging is configured & active');
  } else {
    console.log('  ℹ️ Firebase credentials not set -> Dev simulation mode active (safe, no crashes)');
  }

  // 4. Server Ports & Endpoints
  console.log('\n[4/4] Checking Server Configuration...');
  console.log(`  - Port: ${env.port}`);
  console.log(`  - CORS Origin: ${env.corsOrigin}`);
  console.log(`  - Node Environment: ${env.nodeEnv}`);

  console.log('\n====================================================');
  console.log('🎉 Diagnostic Complete!');
  console.log('====================================================');
  await prisma.$disconnect();
}

diagnose().catch(err => {
  console.error('Diagnostic error:', err);
  process.exit(1);
});
