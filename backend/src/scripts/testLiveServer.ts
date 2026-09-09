async function testLiveServer() {
  const baseUrl = 'https://hostelhub-yp73.onrender.com';
  console.log(`Checking Live Server: ${baseUrl}\n`);

  const getEndpoints = [
    '/health',
    '/api/storage/status',
    '/api/health'
  ];

  for (const path of getEndpoints) {
    try {
      const res = await fetch(`${baseUrl}${path}`);
      const text = await res.text();
      console.log(`GET ${path}: ${res.status} ${res.statusText}`);
      console.log(`Body: ${text}\n`);
    } catch (e: any) {
      console.error(`Error on GET ${path}:`, e.message);
    }
  }

  const postEndpoints = [
    '/api/storage/payment-qr',
    '/api/storage/qr',
    '/api/storage/payment/qr',
    '/api/storage/upload',
    '/api/payments/config/hostel/test'
  ];

  for (const path of postEndpoints) {
    try {
      const res = await fetch(`${baseUrl}${path}`, { method: 'POST' });
      const text = await res.text();
      console.log(`POST ${path}: ${res.status} ${res.statusText}`);
      console.log(`Body: ${text.substring(0, 150)}\n`);
    } catch (e: any) {
      console.error(`Error on POST ${path}:`, e.message);
    }
  }
}

testLiveServer();
