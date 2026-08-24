import { z } from 'zod';

export const registerStudentSchema = z.object({
  body: z.object({
    email: z.string().email('Invalid email address'),
    password: z.string().min(6, 'Password must be at least 6 characters'),
    fullName: z.string().min(2, 'Full name is required'),
    rollNumber: z.string().min(2, 'Roll number is required'),
    collegeName: z.string().min(2, 'College name is required'),
    course: z.string().min(2, 'Course name is required'),
    yearOfStudy: z.string().default('1'),
    gender: z.string().default('male'),
    permanentAddress: z.string().min(5, 'Address is required'),
    emergencyContactName: z.string().min(2, 'Emergency contact name is required'),
    emergencyContactPhone: z.string().min(5, 'Emergency contact phone is required'),
    hostelId: z.string().optional().nullable(),
    roomId: z.string().optional().nullable(),
    bedNumber: z.string().optional().nullable()
  })
});

export const registerHostSchema = z.object({
  body: z.object({
    email: z.string().email('Invalid email address'),
    password: z.string().min(6, 'Password must be at least 6 characters'),
    fullName: z.string().min(2, 'Full name is required'),
    businessName: z.string().min(2, 'Business name is required'),
    contactPhone: z.string().min(5, 'Contact phone is required'),
    contactEmail: z.string().email('Invalid contact email')
  })
});

export const registerAdminSchema = z.object({
  body: z.object({
    email: z.string().email('Invalid email address'),
    password: z.string().min(6, 'Password must be at least 6 characters'),
    fullName: z.string().min(2, 'Full name is required'),
    associationName: z.string().min(2, 'Association or council name is required'),
    designation: z.string().default('Association Head'),
    contactPhone: z.string().optional().nullable()
  })
});

export const loginSchema = z.object({
  body: z.object({
    email: z.string().email('Invalid email address'),
    password: z.string().min(1, 'Password is required')
  })
});

export const refreshTokenSchema = z.object({
  body: z.object({
    refreshToken: z.string().min(10, 'Valid refresh token is required')
  })
});
