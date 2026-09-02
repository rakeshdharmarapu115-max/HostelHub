import http from 'http';

const postData = JSON.stringify({
  email: 'student@campus.edu',
  password: 'Password@123'
});

const req = http.request(
  {
    hostname: 'localhost',
    port: 5000,
    path: '/api/auth/login',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(postData)
    }
  },
  (res) => {
    let data = '';
    res.on('data', (chunk) => {
      data += chunk;
    });
    res.on('end', () => {
      console.log('HTTP Status:', res.statusCode);
      const parsed = JSON.parse(data);
      console.log('Login Response Success:', parsed.success);
      console.log('User Role:', parsed.data?.user?.role);
      console.log('User Name:', parsed.data?.user?.fullName);
      console.log('Token Received:', Boolean(parsed.data?.accessToken));
    });
  }
);

req.on('error', (e) => {
  console.error('Request failed:', e.message);
});

req.write(postData);
req.end();
