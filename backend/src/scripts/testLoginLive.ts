import https from 'https';

const postData = JSON.stringify({
  email: 'student@campus.edu',
  password: 'Password@123'
});

const req = https.request(
  {
    hostname: 'hostelhub-yp73.onrender.com',
    port: 443,
    path: '/api/auth/login',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(postData),
      'Accept': 'application/json'
    }
  },
  (res) => {
    let data = '';
    res.on('data', (chunk) => {
      data += chunk;
    });
    res.on('end', () => {
      console.log('HTTP Status from Render Cloud:', res.statusCode);
      try {
        const parsed = JSON.parse(data);
        console.log('Success:', parsed.success);
        console.log('User Role:', parsed.data?.user?.role);
        console.log('User Full Name:', parsed.data?.user?.fullName);
        console.log('Access Token issued:', Boolean(parsed.data?.tokens?.accessToken));
      } catch (err) {
        console.log('Raw response:', data);
      }
    });
  }
);

req.on('error', (e) => {
  console.error('Request failed:', e.message);
});

req.write(postData);
req.end();
