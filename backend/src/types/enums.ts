export enum UserRole {
  ADMIN = 'ADMIN',
  HOST = 'HOST',
  STUDENT = 'STUDENT',
  STAFF = 'STAFF'
}

export enum StudentStatus {
  ACTIVE = 'ACTIVE',
  VACATED = 'VACATED',
  PENDING_APPROVAL = 'PENDING_APPROVAL'
}

export enum HostelGenderType {
  BOYS = 'BOYS',
  GIRLS = 'GIRLS',
  COED = 'COED'
}

export enum RoomType {
  SINGLE = 'SINGLE',
  DOUBLE = 'DOUBLE',
  TRIPLE = 'TRIPLE',
  DORMITORY = 'DORMITORY'
}

export enum RoomStatus {
  AVAILABLE = 'AVAILABLE',
  FULL = 'FULL',
  MAINTENANCE = 'MAINTENANCE'
}

export enum AllocationStatus {
  ACTIVE = 'ACTIVE',
  TRANSFERRED = 'TRANSFERRED',
  VACATED = 'VACATED'
}

export enum FeeTypeEnum {
  RENT = 'RENT',
  MESS = 'MESS',
  CAUTION_DEPOSIT = 'CAUTION_DEPOSIT',
  ELECTRICITY = 'ELECTRICITY',
  FINE = 'FINE',
  OTHER = 'OTHER'
}

export enum FeeStatusEnum {
  PAID = 'PAID',
  PARTIALLY_PAID = 'PARTIALLY_PAID',
  PENDING = 'PENDING',
  OVERDUE = 'OVERDUE'
}

export enum PaymentMethodEnum {
  ONLINE = 'ONLINE',
  UPI = 'UPI',
  CARD = 'CARD',
  CASH = 'CASH',
  BANK_TRANSFER = 'BANK_TRANSFER'
}

export enum PaymentStatusEnum {
  SUCCESS = 'SUCCESS',
  PENDING = 'PENDING',
  FAILED = 'FAILED'
}

export enum ComplaintCategoryEnum {
  ELECTRICAL = 'ELECTRICAL',
  PLUMBING = 'PLUMBING',
  WIFI = 'WIFI',
  CLEANING = 'CLEANING',
  FOOD = 'FOOD',
  FURNITURE = 'FURNITURE',
  SECURITY = 'SECURITY',
  OTHER = 'OTHER'
}

export enum ComplaintUrgencyEnum {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

export enum ComplaintStatusEnum {
  OPEN = 'OPEN',
  IN_PROGRESS = 'IN_PROGRESS',
  RESOLVED = 'RESOLVED',
  REJECTED = 'REJECTED'
}

export enum AttendanceStatusEnum {
  PRESENT = 'PRESENT',
  ABSENT = 'ABSENT',
  ON_LEAVE = 'ON_LEAVE',
  LATE = 'LATE'
}

export enum LeaveStatusEnum {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  CANCELLED = 'CANCELLED'
}

export enum VisitorStatusEnum {
  INSIDE = 'INSIDE',
  CHECKED_OUT = 'CHECKED_OUT',
  DENIED = 'DENIED'
}

export enum AnnouncementPriorityEnum {
  NORMAL = 'NORMAL',
  IMPORTANT = 'IMPORTANT',
  URGENT = 'URGENT'
}

export enum NotificationTypeEnum {
  PAYMENT_DUE = 'PAYMENT_DUE',
  PAYMENT_CONFIRMED = 'PAYMENT_CONFIRMED',
  COMPLAINT_UPDATE = 'COMPLAINT_UPDATE',
  ATTENDANCE_ALERT = 'ATTENDANCE_ALERT',
  ANNOUNCEMENT = 'ANNOUNCEMENT',
  LEAVE_APPROVED = 'LEAVE_APPROVED'
}
