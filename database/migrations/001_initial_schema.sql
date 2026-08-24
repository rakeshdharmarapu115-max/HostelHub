-- Migration: 001_initial_schema.sql
-- Description: Create initial schema for Hostel Management System (HostelHub)

PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS roles (
    role_id VARCHAR(50) PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000)
);

CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(100) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    avatar_url TEXT,
    is_active INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1)),
    fcm_token TEXT,
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    updated_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_users_role FOREIGN KEY (role) REFERENCES roles (role_name) ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users (role);

CREATE TABLE IF NOT EXISTS hosts (
    host_id VARCHAR(100) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    business_name VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(50) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    verified_status INTEGER NOT NULL DEFAULT 0 CHECK (verified_status IN (0, 1)),
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    updated_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_hosts_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS admins (
    admin_id VARCHAR(100) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    association_name VARCHAR(255) NOT NULL,
    designation VARCHAR(100) NOT NULL,
    permissions TEXT NOT NULL DEFAULT '["ALL"]',
    contact_phone VARCHAR(50),
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_admins_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS hostels (
    hostel_id VARCHAR(100) PRIMARY KEY,
    host_id VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) DEFAULT '',
    postal_code VARCHAR(30) DEFAULT '',
    latitude REAL DEFAULT 0.0,
    longitude REAL DEFAULT 0.0,
    description TEXT,
    gender_type VARCHAR(20) NOT NULL CHECK (gender_type IN ('BOYS', 'GIRLS', 'COED')),
    amenities TEXT NOT NULL DEFAULT '[]',
    rules TEXT NOT NULL DEFAULT '[]',
    images TEXT NOT NULL DEFAULT '[]',
    total_rooms INTEGER NOT NULL DEFAULT 0 CHECK (total_rooms >= 0),
    total_beds INTEGER NOT NULL DEFAULT 0 CHECK (total_beds >= 0),
    occupied_beds INTEGER NOT NULL DEFAULT 0 CHECK (occupied_beds >= 0 AND occupied_beds <= total_beds),
    base_monthly_rent REAL NOT NULL DEFAULT 0.0 CHECK (base_monthly_rent >= 0.0),
    caution_deposit REAL NOT NULL DEFAULT 0.0 CHECK (caution_deposit >= 0.0),
    rating REAL NOT NULL DEFAULT 0.0 CHECK (rating >= 0.0 AND rating <= 5.0),
    rating_count INTEGER NOT NULL DEFAULT 0 CHECK (rating_count >= 0),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    updated_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_hostels_host FOREIGN KEY (host_id) REFERENCES hosts (host_id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_hostels_city ON hostels (city);
CREATE INDEX IF NOT EXISTS idx_hostels_gender ON hostels (gender_type);

CREATE TABLE IF NOT EXISTS blocks (
    block_id VARCHAR(100) PRIMARY KEY,
    hostel_id VARCHAR(100) NOT NULL,
    block_name VARCHAR(50) NOT NULL,
    total_floors INTEGER NOT NULL DEFAULT 1 CHECK (total_floors >= 1),
    description TEXT,
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_blocks_hostel FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE,
    CONSTRAINT uq_hostel_block UNIQUE (hostel_id, block_name)
);

CREATE TABLE IF NOT EXISTS floors (
    floor_id VARCHAR(100) PRIMARY KEY,
    block_id VARCHAR(100) NOT NULL,
    hostel_id VARCHAR(100) NOT NULL,
    floor_number INTEGER NOT NULL,
    total_rooms INTEGER NOT NULL DEFAULT 0 CHECK (total_rooms >= 0),
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_floors_block FOREIGN KEY (block_id) REFERENCES blocks (block_id) ON DELETE CASCADE,
    CONSTRAINT fk_floors_hostel FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE,
    CONSTRAINT uq_block_floor UNIQUE (block_id, floor_number)
);

CREATE TABLE IF NOT EXISTS rooms (
    room_id VARCHAR(100) PRIMARY KEY,
    hostel_id VARCHAR(100) NOT NULL,
    block_id VARCHAR(100),
    floor_id VARCHAR(100),
    room_number VARCHAR(50) NOT NULL,
    floor INTEGER NOT NULL DEFAULT 1,
    block VARCHAR(50) NOT NULL DEFAULT 'A',
    room_type VARCHAR(30) NOT NULL CHECK (room_type IN ('SINGLE', 'DOUBLE', 'TRIPLE', 'DORMITORY')),
    total_capacity INTEGER NOT NULL DEFAULT 2 CHECK (total_capacity >= 1),
    occupied_count INTEGER NOT NULL DEFAULT 0 CHECK (occupied_count >= 0 AND occupied_count <= total_capacity),
    monthly_rent REAL NOT NULL DEFAULT 0.0 CHECK (monthly_rent >= 0.0),
    amenities TEXT NOT NULL DEFAULT '[]',
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'FULL', 'MAINTENANCE')),
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    updated_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_rooms_hostel FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE,
    CONSTRAINT fk_rooms_block FOREIGN KEY (block_id) REFERENCES blocks (block_id) ON DELETE SET NULL,
    CONSTRAINT fk_rooms_floor FOREIGN KEY (floor_id) REFERENCES floors (floor_id) ON DELETE SET NULL,
    CONSTRAINT uq_hostel_room UNIQUE (hostel_id, room_number)
);

CREATE INDEX IF NOT EXISTS idx_rooms_hostel ON rooms (hostel_id);
CREATE INDEX IF NOT EXISTS idx_rooms_status ON rooms (status);

CREATE TABLE IF NOT EXISTS beds (
    bed_id VARCHAR(100) PRIMARY KEY,
    room_id VARCHAR(100) NOT NULL,
    bed_number VARCHAR(50) NOT NULL,
    is_occupied INTEGER NOT NULL DEFAULT 0 CHECK (is_occupied IN (0, 1)),
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_beds_room FOREIGN KEY (room_id) REFERENCES rooms (room_id) ON DELETE CASCADE,
    CONSTRAINT uq_room_bed UNIQUE (room_id, bed_number)
);

CREATE INDEX IF NOT EXISTS idx_beds_room ON beds (room_id);

CREATE TABLE IF NOT EXISTS students (
    student_id VARCHAR(100) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    roll_number VARCHAR(100) NOT NULL UNIQUE,
    college_name VARCHAR(255) NOT NULL,
    course VARCHAR(150) NOT NULL,
    year_of_study VARCHAR(20) NOT NULL DEFAULT '1',
    gender VARCHAR(20) NOT NULL CHECK (gender IN ('male', 'female', 'other')),
    permanent_address TEXT NOT NULL,
    emergency_contact_name VARCHAR(255) NOT NULL,
    emergency_contact_phone VARCHAR(50) NOT NULL,
    hostel_id VARCHAR(100),
    hostel_name VARCHAR(255),
    room_id VARCHAR(100),
    room_number VARCHAR(50),
    bed_number VARCHAR(50),
    admission_date BIGINT,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'VACATED', 'PENDING_APPROVAL')),
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    updated_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_students_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_students_hostel FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE SET NULL,
    CONSTRAINT fk_students_room FOREIGN KEY (room_id) REFERENCES rooms (room_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_students_hostel ON students (hostel_id);
CREATE INDEX IF NOT EXISTS idx_students_roll ON students (roll_number);

CREATE TABLE IF NOT EXISTS room_allocations (
    allocation_id VARCHAR(100) PRIMARY KEY,
    bed_id VARCHAR(100) NOT NULL,
    room_id VARCHAR(100) NOT NULL,
    hostel_id VARCHAR(100) NOT NULL,
    student_id VARCHAR(100) NOT NULL,
    allocation_date BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    check_in_date BIGINT,
    check_out_date BIGINT,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'TRANSFERRED', 'VACATED')),
    allocated_by VARCHAR(100),
    remarks TEXT,
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    updated_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_allocations_bed FOREIGN KEY (bed_id) REFERENCES beds (bed_id) ON DELETE RESTRICT,
    CONSTRAINT fk_allocations_room FOREIGN KEY (room_id) REFERENCES rooms (room_id) ON DELETE RESTRICT,
    CONSTRAINT fk_allocations_hostel FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE RESTRICT,
    CONSTRAINT fk_allocations_student FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
    CONSTRAINT fk_allocations_admin FOREIGN KEY (allocated_by) REFERENCES users (user_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_allocations_student ON room_allocations (student_id);
CREATE INDEX IF NOT EXISTS idx_allocations_bed ON room_allocations (bed_id);
CREATE INDEX IF NOT EXISTS idx_allocations_status ON room_allocations (status);

CREATE TABLE IF NOT EXISTS staff (
    staff_id VARCHAR(100) PRIMARY KEY,
    user_id VARCHAR(100) UNIQUE,
    hostel_id VARCHAR(100) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role_title VARCHAR(100) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    is_available INTEGER NOT NULL DEFAULT 1 CHECK (is_available IN (0, 1)),
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_staff_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE SET NULL,
    CONSTRAINT fk_staff_hostel FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS fee_types (
    fee_type_id VARCHAR(100) PRIMARY KEY,
    hostel_id VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    default_amount REAL NOT NULL DEFAULT 0.0,
    billing_cycle VARCHAR(50) DEFAULT 'MONTHLY',
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_feetype_hostel FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS fees (
    fee_id VARCHAR(100) PRIMARY KEY,
    hostel_id VARCHAR(100) NOT NULL,
    student_id VARCHAR(100) NOT NULL,
    room_id VARCHAR(100),
    title VARCHAR(255) NOT NULL,
    fee_type VARCHAR(50) NOT NULL CHECK (fee_type IN ('RENT', 'MESS', 'CAUTION_DEPOSIT', 'ELECTRICITY', 'FINE', 'OTHER')),
    amount REAL NOT NULL CHECK (amount >= 0.0),
    amount_paid REAL NOT NULL DEFAULT 0.0 CHECK (amount_paid >= 0.0 AND amount_paid <= amount),
    due_date BIGINT NOT NULL,
    billing_month INTEGER NOT NULL CHECK (billing_month BETWEEN 1 AND 12),
    billing_year INTEGER NOT NULL CHECK (billing_year >= 2020),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PAID', 'PARTIALLY_PAID', 'PENDING', 'OVERDUE')),
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    updated_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_fees_hostel FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE,
    CONSTRAINT fk_fees_student FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
    CONSTRAINT fk_fees_room FOREIGN KEY (room_id) REFERENCES rooms (room_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_fees_student ON fees (student_id);
CREATE INDEX IF NOT EXISTS idx_fees_hostel ON fees (hostel_id);
CREATE INDEX IF NOT EXISTS idx_fees_status ON fees (status);

CREATE TABLE IF NOT EXISTS payments (
    payment_id VARCHAR(100) PRIMARY KEY,
    fee_id VARCHAR(100) NOT NULL,
    student_id VARCHAR(100) NOT NULL,
    hostel_id VARCHAR(100) NOT NULL,
    amount_paid REAL NOT NULL CHECK (amount_paid > 0.0),
    payment_method VARCHAR(50) NOT NULL CHECK (payment_method IN ('ONLINE', 'UPI', 'CARD', 'CASH', 'BANK_TRANSFER')),
    transaction_reference VARCHAR(150) NOT NULL UNIQUE,
    payment_date BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    receipt_url TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'SUCCESS' CHECK (status IN ('SUCCESS', 'PENDING', 'FAILED')),
    verified_by_host_id VARCHAR(100),
    remarks TEXT,
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_payments_fee FOREIGN KEY (fee_id) REFERENCES fees (fee_id) ON DELETE RESTRICT,
    CONSTRAINT fk_payments_student FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE RESTRICT,
    CONSTRAINT fk_payments_hostel FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE RESTRICT,
    CONSTRAINT fk_payments_verifier FOREIGN KEY (verified_by_host_id) REFERENCES users (user_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_payments_student ON payments (student_id);
CREATE INDEX IF NOT EXISTS idx_payments_fee ON payments (fee_id);

CREATE TABLE IF NOT EXISTS complaints (
    complaint_id VARCHAR(100) PRIMARY KEY,
    hostel_id VARCHAR(100) NOT NULL,
    student_id VARCHAR(100) NOT NULL,
    student_name VARCHAR(255) NOT NULL,
    room_number VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL CHECK (category IN ('ELECTRICAL', 'PLUMBING', 'WIFI', 'CLEANING', 'FOOD', 'FURNITURE', 'SECURITY', 'OTHER')),
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    attachments TEXT NOT NULL DEFAULT '[]',
    urgency VARCHAR(30) NOT NULL DEFAULT 'MEDIUM' CHECK (urgency IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'REJECTED')),
    assigned_staff_name VARCHAR(255),
    host_notes TEXT,
    resolution_summary TEXT,
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    resolved_at BIGINT,
    updated_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_complaints_hostel FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE,
    CONSTRAINT fk_complaints_student FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_complaints_hostel ON complaints (hostel_id);
CREATE INDEX IF NOT EXISTS idx_complaints_student ON complaints (student_id);
CREATE INDEX IF NOT EXISTS idx_complaints_status ON complaints (status);

CREATE TABLE IF NOT EXISTS maintenance_logs (
    maintenance_id VARCHAR(100) PRIMARY KEY,
    complaint_id VARCHAR(100),
    hostel_id VARCHAR(100) NOT NULL,
    room_id VARCHAR(100),
    performed_by_staff_id VARCHAR(100),
    issue_type VARCHAR(100) NOT NULL,
    action_taken TEXT NOT NULL,
    cost REAL DEFAULT 0.0,
    maintenance_date BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_maint_complaint FOREIGN KEY (complaint_id) REFERENCES complaints (complaint_id) ON DELETE SET NULL,
    CONSTRAINT fk_maint_hostel FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE,
    CONSTRAINT fk_maint_room FOREIGN KEY (room_id) REFERENCES rooms (room_id) ON DELETE SET NULL,
    CONSTRAINT fk_maint_staff FOREIGN KEY (performed_by_staff_id) REFERENCES staff (staff_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS leave_requests (
    leave_id VARCHAR(100) PRIMARY KEY,
    student_id VARCHAR(100) NOT NULL,
    hostel_id VARCHAR(100) NOT NULL,
    start_date VARCHAR(30) NOT NULL,
    end_date VARCHAR(30) NOT NULL,
    reason TEXT NOT NULL,
    emergency_contact_phone VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    approved_by VARCHAR(100),
    rejection_reason TEXT,
    remarks TEXT,
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    updated_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_leave_student FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_hostel FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_approver FOREIGN KEY (approved_by) REFERENCES users (user_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_leave_student ON leave_requests (student_id);
CREATE INDEX IF NOT EXISTS idx_leave_hostel ON leave_requests (hostel_id);

CREATE TABLE IF NOT EXISTS attendance_records (
    attendance_id VARCHAR(100) PRIMARY KEY,
    hostel_id VARCHAR(100) NOT NULL,
    student_id VARCHAR(100) NOT NULL,
    student_name VARCHAR(255) NOT NULL,
    room_number VARCHAR(50) NOT NULL,
    date VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PRESENT' CHECK (status IN ('PRESENT', 'ABSENT', 'ON_LEAVE', 'LATE')),
    check_in_time BIGINT,
    remarks TEXT,
    marked_by VARCHAR(100) NOT NULL DEFAULT 'STUDENT_SELF',
    leave_request_id VARCHAR(100),
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_attendance_hostel FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_leave FOREIGN KEY (leave_request_id) REFERENCES leave_requests (leave_id) ON DELETE SET NULL,
    CONSTRAINT uq_student_date UNIQUE (student_id, date)
);

CREATE INDEX IF NOT EXISTS idx_attendance_student_date ON attendance_records (student_id, date);
CREATE INDEX IF NOT EXISTS idx_attendance_hostel_date ON attendance_records (hostel_id, date);

CREATE TABLE IF NOT EXISTS visitors (
    visitor_id VARCHAR(100) PRIMARY KEY,
    hostel_id VARCHAR(100) NOT NULL,
    student_id VARCHAR(100) NOT NULL,
    visitor_name VARCHAR(255) NOT NULL,
    relationship VARCHAR(100) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    id_proof_type VARCHAR(50),
    id_proof_number VARCHAR(100),
    purpose TEXT NOT NULL,
    check_in_time BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    check_out_time BIGINT,
    approved_by VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'INSIDE' CHECK (status IN ('INSIDE', 'CHECKED_OUT', 'DENIED')),
    remarks TEXT,
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_visitors_hostel FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE,
    CONSTRAINT fk_visitors_student FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
    CONSTRAINT fk_visitors_approver FOREIGN KEY (approved_by) REFERENCES users (user_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_visitors_hostel ON visitors (hostel_id);

CREATE TABLE IF NOT EXISTS food_menus (
    menu_id VARCHAR(100) PRIMARY KEY,
    hostel_id VARCHAR(100) NOT NULL,
    week_start_date VARCHAR(20) NOT NULL,
    schedule_json TEXT NOT NULL,
    special_notice TEXT,
    is_published INTEGER NOT NULL DEFAULT 1 CHECK (is_published IN (0, 1)),
    updated_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_foodmenu_hostel FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE,
    CONSTRAINT uq_hostel_week_menu UNIQUE (hostel_id, week_start_date)
);

CREATE TABLE IF NOT EXISTS announcements (
    announcement_id VARCHAR(100) PRIMARY KEY,
    hostel_id VARCHAR(100) NOT NULL DEFAULT 'GLOBAL_CAMPUS',
    sender_id VARCHAR(100) NOT NULL,
    sender_role VARCHAR(50) NOT NULL CHECK (sender_role IN ('ADMIN', 'HOST', 'STUDENT', 'STAFF')),
    sender_name VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    priority VARCHAR(30) NOT NULL DEFAULT 'NORMAL' CHECK (priority IN ('NORMAL', 'IMPORTANT', 'URGENT')),
    target_audience VARCHAR(50) NOT NULL DEFAULT 'ALL',
    attachment_urls TEXT NOT NULL DEFAULT '[]',
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    expires_at BIGINT,
    CONSTRAINT fk_announcements_sender FOREIGN KEY (sender_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_announcements_hostel ON announcements (hostel_id);

CREATE TABLE IF NOT EXISTS notifications (
    notification_id VARCHAR(100) PRIMARY KEY,
    recipient_user_id VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('PAYMENT_DUE', 'PAYMENT_CONFIRMED', 'COMPLAINT_UPDATE', 'ATTENDANCE_ALERT', 'ANNOUNCEMENT', 'LEAVE_APPROVED')),
    related_entity_id VARCHAR(100),
    is_read INTEGER NOT NULL DEFAULT 0 CHECK (is_read IN (0, 1)),
    created_at BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notifications_recipient ON notifications (recipient_user_id, is_read);

CREATE TABLE IF NOT EXISTS audit_logs (
    log_id VARCHAR(100) PRIMARY KEY,
    user_id VARCHAR(100),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    details TEXT,
    ip_address VARCHAR(50),
    timestamp BIGINT NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_logs (timestamp);
