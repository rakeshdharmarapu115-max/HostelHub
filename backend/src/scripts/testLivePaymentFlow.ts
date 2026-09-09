async function testLivePaymentFlow() {
  const baseUrl = 'https://hostelhub-yp73.onrender.com';
  console.log(`\n=======================================================`);
  console.log(`🧪 Testing Live Deployment: ${baseUrl}`);
  console.log(`=======================================================\n`);

  // 1. Check Health & Build Fingerprint
  try {
    const healthRes = await fetch(`${baseUrl}/health`);
    const healthData: any = await healthRes.json();
    console.log(`[STEP 1] GET /health -> Status: ${healthRes.status}`);
    console.log(`         Version: ${healthData.version || 'N/A'}`);
    console.log(`         qrUploadRouteVersion: ${healthData.qrUploadRouteVersion || 'N/A'}`);
    console.log(`         Database: ${healthData.database?.status || 'N/A'} (${healthData.database?.latencyMs}ms)\n`);
  } catch (e: any) {
    console.error(`[STEP 1 FAILED] Could not fetch /health:`, e.message);
  }

  // 2. Check Storage Status
  try {
    const statusRes = await fetch(`${baseUrl}/api/storage/status`);
    const statusData: any = await statusRes.json();
    console.log(`[STEP 2] GET /api/storage/status -> Status: ${statusRes.status}`);
    console.log(`         Data:`, statusData.data, `\n`);
  } catch (e: any) {
    console.error(`[STEP 2 FAILED] Could not fetch /api/storage/status:`, e.message);
  }

  // 3. Test POST /api/storage/payment-qr response without token
  try {
    const unauthRes = await fetch(`${baseUrl}/api/storage/payment-qr`, { method: 'POST' });
    const unauthText = await unauthRes.text();
    console.log(`[STEP 3] POST /api/storage/payment-qr (unauthenticated) -> HTTP ${unauthRes.status}`);
    console.log(`         Response: ${unauthText.substring(0, 120)}\n`);
  } catch (e: any) {
    console.error(`[STEP 3 FAILED] Error calling POST /api/storage/payment-qr:`, e.message);
  }

  // 4. Test Owner Login
  let ownerToken = '';
  let ownerHostelId = '';
  try {
    const loginRes = await fetch(`${baseUrl}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: 'warden@greenvalley.edu',
        password: 'Password@123'
      })
    });
    const loginData: any = await loginRes.json();
    if (loginRes.ok && (loginData.data?.tokens?.accessToken || loginData.data?.token)) {
      ownerToken = loginData.data?.tokens?.accessToken || loginData.data?.token;
      ownerHostelId = loginData.data?.user?.hostelId || '';
      console.log(`[STEP 4] Owner Login SUCCESS -> User: ${loginData.data.user.email} (Role: ${loginData.data.user.role}, HostelId: ${ownerHostelId})\n`);
    } else {
      console.log(`[STEP 4] Owner Login response:`, loginData);
    }
  } catch (e: any) {
    console.error(`[STEP 4 FAILED] Login request error:`, e.message);
  }

  // 5. Test Authenticated QR Upload
  let uploadedQrUrl = '';
  if (ownerToken) {
    try {
      const pngBuffer = Buffer.from(
        'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==',
        'base64'
      );
      const formData = new FormData();
      const blob = new Blob([pngBuffer], { type: 'image/png' });
      formData.append('file', blob, 'owner_official_qr.png');

      const uploadRes = await fetch(`${baseUrl}/api/storage/payment-qr`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${ownerToken}`
        },
        body: formData
      });

      const uploadData: any = await uploadRes.json();
      console.log(`[STEP 5] Authenticated POST /api/storage/payment-qr -> HTTP ${uploadRes.status}`);
      console.log(`         Upload response:`, uploadData);
      if (uploadRes.ok && uploadData.data?.url) {
        uploadedQrUrl = uploadData.data.url;
        console.log(`         ✓ QR Uploaded successfully! Server URL: ${uploadedQrUrl}\n`);
      }
    } catch (e: any) {
      console.error(`[STEP 5 FAILED] QR upload error:`, e.message);
    }
  }

  // 6. Update Owner Payment Config
  if (ownerToken && uploadedQrUrl) {
    try {
      const updateRes = await fetch(`${baseUrl}/api/payments/config/hostel/${ownerHostelId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${ownerToken}`
        },
        body: JSON.stringify({
          paymentQrUrl: uploadedQrUrl,
          upiId: 'greenvalley.owner@okaxis',
          merchantName: 'Green Valley Residences',
          qrPaymentEnabled: true
        })
      });
      const updateData: any = await updateRes.json();
      console.log(`[STEP 6] PUT /api/payments/config/hostel/${ownerHostelId} -> HTTP ${updateRes.status}`);
      console.log(`         Payment Config Update response:`, updateData, `\n`);
    } catch (e: any) {
      console.error(`[STEP 6 FAILED] Error updating payment config:`, e.message);
    }
  }

  // 7. Verify Owner Fetches Updated Payment Config
  if (ownerToken) {
    try {
      const getRes = await fetch(`${baseUrl}/api/payments/config/hostel/${ownerHostelId}`, {
        headers: {
          'Authorization': `Bearer ${ownerToken}`
        }
      });
      const getData: any = await getRes.json();
      console.log(`[STEP 7] GET /api/payments/config/hostel/${ownerHostelId} -> HTTP ${getRes.status}`);
      console.log(`         Saved Config:`, getData.data, `\n`);
    } catch (e: any) {
      console.error(`[STEP 7 FAILED] Error fetching payment config:`, e.message);
    }
  }

  // 8. Student Login & View Fee Payment Details
  try {
    const studentLoginRes = await fetch(`${baseUrl}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: 'student@campus.edu',
        password: 'Password@123'
      })
    });
    const studentLoginData: any = await studentLoginRes.json();
    const studentToken = studentLoginData.data?.tokens?.accessToken || studentLoginData.data?.token;

    if (studentToken) {
      console.log(`[STEP 8] Student Login SUCCESS -> ${studentLoginData.data.user.email}`);
      const studentConfigRes = await fetch(`${baseUrl}/api/payments/config/my-hostel`, {
        headers: {
          'Authorization': `Bearer ${studentToken}`
        }
      });
      const studentConfigData: any = await studentConfigRes.json();
      console.log(`[STEP 9] GET /api/payments/config/my-hostel (as Student) -> HTTP ${studentConfigRes.status}`);
      console.log(`         Student Received Payment Details:`);
      console.log(`         - Hostel Name: ${studentConfigData.data?.hostelName}`);
      console.log(`         - Owner UPI ID: ${studentConfigData.data?.upiId}`);
      console.log(`         - QR Payment URL: ${studentConfigData.data?.paymentQrUrl}`);
      console.log(`         - QR Payment Enabled: ${studentConfigData.data?.qrPaymentEnabled}\n`);
    } else {
      console.log(`[STEP 8] Student login response:`, studentLoginData);
    }
  } catch (e: any) {
    console.error(`[STEP 8/9 FAILED] Student flow error:`, e.message);
  }
}

testLivePaymentFlow();
