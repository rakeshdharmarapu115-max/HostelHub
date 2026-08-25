export const swaggerDocument = {
  openapi: '3.0.0',
  info: {
    title: 'HostelHub REST API',
    version: '1.0.0',
    description: 'Complete production-grade REST API backend for the HostelHub Hostel Management System'
  },
  servers: [
  {
    url: 'https://hostelhub-yp73.onrender.com/api',
    description: 'Cloud Production Server (Render)'
  }
],
  components: {
    securitySchemes: {
      bearerAuth: {
        type: 'http',
        scheme: 'bearer',
        bearerFormat: 'JWT'
      }
    }
  },
  security: [
    {
      bearerAuth: []
    }
  ],
  paths: {
    '/auth/register/student': {
      post: {
        tags: ['Authentication'],
        summary: 'Register a new student account and profile',
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: {
                type: 'object',
                required: ['email', 'password', 'fullName', 'rollNumber', 'collegeName', 'course', 'permanentAddress', 'emergencyContactName', 'emergencyContactPhone'],
                properties: {
                  email: { type: 'string', example: 'newstudent@campus.edu' },
                  password: { type: 'string', example: 'Password@123' },
                  fullName: { type: 'string', example: 'John Doe' },
                  rollNumber: { type: 'string', example: 'STD-2026-9999' },
                  collegeName: { type: 'string', example: 'Engineering College' },
                  course: { type: 'string', example: 'B.Tech IT' },
                  yearOfStudy: { type: 'string', example: '1' },
                  gender: { type: 'string', example: 'male' },
                  permanentAddress: { type: 'string', example: '100 University Blvd' },
                  emergencyContactName: { type: 'string', example: 'Jane Doe (Mother)' },
                  emergencyContactPhone: { type: 'string', example: '+1 555-0999' }
                }
              }
            }
          }
        },
        responses: {
          201: { description: 'Student registered successfully' },
          409: { description: 'Email or Roll number already exists' }
        }
      }
    },
    '/auth/login': {
      post: {
        tags: ['Authentication'],
        summary: 'Authenticate and receive JWT token pair',
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: {
                type: 'object',
                required: ['email', 'password'],
                properties: {
                  email: { type: 'string', example: 'student@campus.edu' },
                  password: { type: 'string', example: 'Password@123' }
                }
              }
            }
          }
        },
        responses: {
          200: { description: 'Login successful' },
          401: { description: 'Invalid email or password' }
        }
      }
    },
    '/auth/me': {
      get: {
        tags: ['Authentication'],
        summary: 'Get current authenticated user profile',
        responses: {
          200: { description: 'Profile returned' },
          401: { description: 'Unauthorized' }
        }
      }
    },
    '/hostels': {
      get: {
        tags: ['Hostels'],
        summary: 'List and filter hostels',
        parameters: [
          { name: 'city', in: 'query', schema: { type: 'string' } },
          { name: 'gender', in: 'query', schema: { type: 'string' } },
          { name: 'minRent', in: 'query', schema: { type: 'number' } },
          { name: 'maxRent', in: 'query', schema: { type: 'number' } }
        ],
        responses: {
          200: { description: 'Hostels list' }
        }
      }
    },
    '/allocations': {
      post: {
        tags: ['Allocations'],
        summary: 'Atomic room bed allocation transaction',
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: {
                type: 'object',
                required: ['bedId', 'roomId', 'studentId'],
                properties: {
                  bedId: { type: 'string' },
                  roomId: { type: 'string' },
                  studentId: { type: 'string' },
                  remarks: { type: 'string' }
                }
              }
            }
          }
        },
        responses: {
          201: { description: 'Bed allocated atomically' },
          409: { description: 'Bed already occupied or conflict' }
        }
      }
    },
    '/dashboard/student': {
      get: {
        tags: ['Dashboard'],
        summary: 'Get aggregated student metrics',
        responses: {
          200: { description: 'Student metrics' }
        }
      }
    },
    '/dashboard/host': {
      get: {
        tags: ['Dashboard'],
        summary: 'Get aggregated host metrics',
        responses: {
          200: { description: 'Host metrics' }
        }
      }
    },
    '/dashboard/admin': {
      get: {
        tags: ['Dashboard'],
        summary: 'Get aggregated admin housing overview',
        responses: {
          200: { description: 'Admin statistics' }
        }
      }
    }
  }
};
