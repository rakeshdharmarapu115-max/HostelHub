-- ============================================================================
-- Hostel Management System (HostelHub) - Seed Data
-- ============================================================================

-- 1. Roles
INSERT OR REPLACE INTO roles (role_id, role_name, description) VALUES
('role_admin', 'ADMIN', 'System Administrator with university-wide campus housing management rights'),
('role_host', 'HOST', 'Hostel Warden or Property Manager managing hostel units, rooms, and students'),
('role_student', 'STUDENT', 'Enrolled student resident residing in hostel rooms'),
('role_staff', 'STAFF', 'Maintenance, housekeeping, and security staff');

-- 2. Users
-- Passwords are stored hashed (e.g. SHA-256 / Bcrypt placeholder hashes)
INSERT OR REPLACE INTO users (user_id, email, password_hash, role, full_name, phone_number, avatar_url, is_active, created_at, updated_at) VALUES
('admin_001', 'admin@campus.edu', '$2a$12$e8xL4k3J1fFq1s7U3gTfOuXvYgJv8r0A.lKqH.wG.fW8fB1jK5d5O', 'ADMIN', 'Dean Henderson', '+1 555-0100', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb', 1, 1729000000000, 1729000000000),
('host_001', 'warden@greenvalley.edu', '$2a$12$e8xL4k3J1fFq1s7U3gTfOuXvYgJv8r0A.lKqH.wG.fW8fB1jK5d5O', 'HOST', 'Robert Vance', '+1 555-HOSTEL', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d', 1, 1729000000000, 1729000000000),
('host_002', 'warden@stjude.edu', '$2a$12$e8xL4k3J1fFq1s7U3gTfOuXvYgJv8r0A.lKqH.wG.fW8fB1jK5d5O', 'HOST', 'Sister Claire', '+1 555-STJUDE', 'https://images.unsplash.com/photo-1544005313-94ddf0286df2', 1, 1729000000000, 1729000000000),
('std_001', 'student@campus.edu', '$2a$12$e8xL4k3J1fFq1s7U3gTfOuXvYgJv8r0A.lKqH.wG.fW8fB1jK5d5O', 'STUDENT', 'Alex Mercer', '+1 555-0199', 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6', 1, 1729000000000, 1729000000000),
('std_002', 'david.miller@campus.edu', '$2a$12$e8xL4k3J1fFq1s7U3gTfOuXvYgJv8r0A.lKqH.wG.fW8fB1jK5d5O', 'STUDENT', 'David Miller', '+1 555-0188', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e', 1, 1729000000000, 1729000000000),
('std_003', 'jordan.reed@campus.edu', '$2a$12$e8xL4k3J1fFq1s7U3gTfOuXvYgJv8r0A.lKqH.wG.fW8fB1jK5d5O', 'STUDENT', 'Jordan Reed', '+1 555-0177', 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7', 1, 1729000000000, 1729000000000),
('std_004', 'marcus.brody@campus.edu', '$2a$12$e8xL4k3J1fFq1s7U3gTfOuXvYgJv8r0A.lKqH.wG.fW8fB1jK5d5O', 'STUDENT', 'Marcus Brody', '+1 555-0166', 'https://images.unsplash.com/photo-1492562080023-ab3db95bfbce', 1, 1729000000000, 1729000000000),
('std_005', 'elena.rostova@campus.edu', '$2a$12$e8xL4k3J1fFq1s7U3gTfOuXvYgJv8r0A.lKqH.wG.fW8fB1jK5d5O', 'STUDENT', 'Elena Rostova', '+1 555-0155', 'https://images.unsplash.com/photo-1517841905240-472988babdf9', 1, 1729000000000, 1729000000000),
('staff_001', 'carl.electric@campus.edu', '$2a$12$e8xL4k3J1fFq1s7U3gTfOuXvYgJv8r0A.lKqH.wG.fW8fB1jK5d5O', 'STAFF', 'Carl Johnson', '+1 555-0211', NULL, 1, 1729000000000, 1729000000000),
('staff_002', 'mario.plumb@campus.edu', '$2a$12$e8xL4k3J1fFq1s7U3gTfOuXvYgJv8r0A.lKqH.wG.fW8fB1jK5d5O', 'STAFF', 'Mario Rossi', '+1 555-0222', NULL, 1, 1729000000000, 1729000000000);

-- 3. Admins
INSERT OR REPLACE INTO admins (admin_id, user_id, full_name, association_name, designation, permissions, contact_phone, created_at) VALUES
('adm_001', 'admin_001', 'Dean Henderson', 'Campus Housing Association', 'Dean of Student Welfare', '["ALL","APPROVE_HOSTEL","MANAGE_FINANCES","BROADCAST_ALL"]', '+1 555-0100', 1729000000000);

-- 4. Hosts
INSERT OR REPLACE INTO hosts (host_id, user_id, full_name, business_name, contact_phone, contact_email, verified_status, created_at, updated_at) VALUES
('host_001', 'host_001', 'Robert Vance', 'Green Valley Residences Inc', '+1 555-HOSTEL', 'warden@greenvalley.edu', 1, 1729000000000, 1729000000000),
('host_002', 'host_002', 'Sister Claire', 'St. Jude Housing Trust', '+1 555-STJUDE', 'warden@stjude.edu', 1, 1729000000000, 1729000000000);

-- 5. Hostels
INSERT OR REPLACE INTO hostels (hostel_id, host_id, name, address, city, state, postal_code, latitude, longitude, description, gender_type, amenities, rules, images, total_rooms, total_beds, occupied_beds, base_monthly_rent, caution_deposit, rating, rating_count, contact_email, contact_phone, created_at, updated_at) VALUES
('hostel_001', 'host_001', 'Green Valley Residencies', '12 North Campus Road, University District', 'Academic City', 'State', '10001', 40.7128, -74.0060, 'Premium student housing with high-speed Wi-Fi, modern study pods, 24/7 security, gym, and nutritious catering.', 'COED', '["Wi-Fi","Air Conditioning","Mess Included","24/7 Power Backup","Gym","Laundry"]', '["Curfew: 10:30 PM","No smoking on premises","Quiet hours after 11:00 PM"]', '["https://images.unsplash.com/photo-1555854877-bab0e564b8d5"]', 30, 60, 52, 450.0, 200.0, 4.8, 124, 'warden@greenvalley.edu', '+1 555-HOSTEL', 1729000000000, 1729000000000),
('hostel_002', 'host_002', 'St. Jude Student Suites', '45 West Avenue, Campus Perimeter', 'Academic City', 'State', '10002', 40.7135, -74.0080, 'Cozy and affordable student dormitory close to the central library and sports pavilion.', 'BOYS', '["Wi-Fi","Mess Included","CCTV Security","Study Hall"]', '["Curfew: 10:00 PM","Guests allowed till 8 PM"]', '["https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf"]', 25, 50, 40, 380.0, 150.0, 4.5, 88, 'warden@stjude.edu', '+1 555-STJUDE', 1729000000000, 1729000000000);

-- 6. Blocks
INSERT OR REPLACE INTO blocks (block_id, hostel_id, block_name, total_floors, description, created_at) VALUES
('blk_h1_a', 'hostel_001', 'A', 3, 'Block A - AC Deluxe Wing', 1729000000000),
('blk_h1_b', 'hostel_001', 'B', 3, 'Block B - Standard Single & Double Wing', 1729000000000),
('blk_h2_a', 'hostel_002', 'Main', 2, 'Main Dormitory Building', 1729000000000);

-- 7. Floors
INSERT OR REPLACE INTO floors (floor_id, block_id, hostel_id, floor_number, total_rooms, created_at) VALUES
('flr_h1_a_1', 'blk_h1_a', 'hostel_001', 1, 10, 1729000000000),
('flr_h1_a_2', 'blk_h1_a', 'hostel_001', 2, 10, 1729000000000),
('flr_h1_b_1', 'blk_h1_b', 'hostel_001', 1, 10, 1729000000000),
('flr_h2_a_1', 'blk_h2_a', 'hostel_002', 1, 12, 1729000000000),
('flr_h2_a_2', 'blk_h2_a', 'hostel_002', 2, 13, 1729000000000);

-- 8. Rooms
INSERT OR REPLACE INTO rooms (room_id, hostel_id, block_id, floor_id, room_number, floor, block, room_type, total_capacity, occupied_count, monthly_rent, amenities, status, created_at, updated_at) VALUES
('room_204', 'hostel_001', 'blk_h1_a', 'flr_h1_a_2', 'A-204', 2, 'A', 'DOUBLE', 2, 2, 450.0, '["AC","Attached Bath","Study Table","Balcony"]', 'FULL', 1729000000000, 1729000000000),
('room_205', 'hostel_001', 'blk_h1_a', 'flr_h1_a_2', 'A-205', 2, 'A', 'DOUBLE', 2, 1, 450.0, '["AC","Attached Bath","Study Table"]', 'AVAILABLE', 1729000000000, 1729000000000),
('room_101', 'hostel_001', 'blk_h1_b', 'flr_h1_b_1', 'B-101', 1, 'B', 'SINGLE', 1, 1, 600.0, '["AC","Attached Bath","Fridge","Study Table"]', 'FULL', 1729000000000, 1729000000000),
('room_102', 'hostel_001', 'blk_h1_b', 'flr_h1_b_1', 'B-102', 1, 'B', 'TRIPLE', 3, 0, 350.0, '["Attached Bath","Geyser","Study Desks"]', 'AVAILABLE', 1729000000000, 1729000000000),
('room_j101', 'hostel_002', 'blk_h2_a', 'flr_h2_a_1', 'M-101', 1, 'Main', 'DOUBLE', 2, 1, 380.0, '["Ceiling Fan","Shared Bath","Study Table"]', 'AVAILABLE', 1729000000000, 1729000000000);

-- 9. Beds
INSERT OR REPLACE INTO beds (bed_id, room_id, bed_number, is_occupied, created_at) VALUES
('bed_1', 'room_204', 'Bed-A', 1, 1729000000000),
('bed_2', 'room_204', 'Bed-B', 1, 1729000000000),
('bed_3', 'room_205', 'Bed-A', 1, 1729000000000),
('bed_4', 'room_205', 'Bed-B', 0, 1729000000000),
('bed_5', 'room_101', 'Bed-A', 1, 1729000000000),
('bed_6', 'room_102', 'Bed-A', 0, 1729000000000),
('bed_7', 'room_102', 'Bed-B', 0, 1729000000000),
('bed_8', 'room_102', 'Bed-C', 0, 1729000000000),
('bed_j1', 'room_j101', 'Bed-A', 1, 1729000000000),
('bed_j2', 'room_j101', 'Bed-B', 0, 1729000000000);

-- 10. Students
INSERT OR REPLACE INTO students (student_id, user_id, full_name, roll_number, college_name, course, year_of_study, gender, permanent_address, emergency_contact_name, emergency_contact_phone, hostel_id, hostel_name, room_id, room_number, bed_number, admission_date, status, created_at, updated_at) VALUES
('std_001', 'std_001', 'Alex Mercer', 'STD-2024-0042', 'College of Engineering', 'B.Tech Computer Science', '3', 'male', '42 Silicon Avenue, Metro City', 'Sarah Mercer (Mother)', '+1 555-0144', 'hostel_001', 'Green Valley Residencies', 'room_204', 'A-204', 'Bed-A', 1724000000000, 'ACTIVE', 1729000000000, 1729000000000),
('std_002', 'std_002', 'David Miller', 'STD-2024-0043', 'School of Management', 'BBA', '2', 'male', '88 Wall Street, Metro City', 'James Miller (Father)', '+1 555-0145', 'hostel_001', 'Green Valley Residencies', 'room_204', 'A-204', 'Bed-B', 1724000000000, 'ACTIVE', 1729000000000, 1729000000000),
('std_003', 'std_003', 'Jordan Reed', 'STD-2024-0088', 'Faculty of Arts & Sciences', 'B.Sc Physics', '1', 'male', '15 Newton Drive, Cambridge City', 'Arthur Reed (Father)', '+1 555-0146', 'hostel_001', 'Green Valley Residencies', 'room_101', 'B-101', 'Bed-A', 1724000000000, 'ACTIVE', 1729000000000, 1729000000000),
('std_004', 'std_004', 'Marcus Brody', 'STD-2024-0092', 'College of Engineering', 'B.Tech Mechanical', '2', 'male', '74 Industrial Way, Northfield', 'Helen Brody (Mother)', '+1 555-0147', 'hostel_001', 'Green Valley Residencies', 'room_205', 'A-205', 'Bed-A', 1724000000000, 'ACTIVE', 1729000000000, 1729000000000),
('std_005', 'std_005', 'Elena Rostova', 'STD-2024-0105', 'Faculty of Medicine', 'MBBS', '1', 'female', '91 Health Boulevard, Riverdale', 'Dr. Ivan Rostov (Father)', '+1 555-0148', NULL, NULL, NULL, NULL, NULL, 1724000000000, 'PENDING_APPROVAL', 1729000000000, 1729000000000);

-- 11. Room Allocations
INSERT OR REPLACE INTO room_allocations (allocation_id, bed_id, room_id, hostel_id, student_id, allocation_date, check_in_date, check_out_date, status, allocated_by, remarks, created_at, updated_at) VALUES
('alloc_001', 'bed_1', 'room_204', 'hostel_001', 'std_001', 1724000000000, 1724086400000, NULL, 'ACTIVE', 'host_001', 'Regular term allotment', 1729000000000, 1729000000000),
('alloc_002', 'bed_2', 'room_204', 'hostel_001', 'std_002', 1724000000000, 1724086400000, NULL, 'ACTIVE', 'host_001', 'Roommate preference matched', 1729000000000, 1729000000000),
('alloc_003', 'bed_3', 'room_205', 'hostel_001', 'std_004', 1724000000000, 1724086400000, NULL, 'ACTIVE', 'host_001', 'Allotted AC Double room', 1729000000000, 1729000000000),
('alloc_004', 'bed_5', 'room_101', 'hostel_001', 'std_003', 1724000000000, 1724086400000, NULL, 'ACTIVE', 'host_001', 'Single room premium requested', 1729000000000, 1729000000000);

-- 12. Staff
INSERT OR REPLACE INTO staff (staff_id, user_id, hostel_id, full_name, role_title, phone, email, is_available, created_at) VALUES
('staff_001', 'staff_001', 'hostel_001', 'Carl Johnson', 'Certified Electrician', '+1 555-0211', 'carl.electric@campus.edu', 1, 1729000000000),
('staff_002', 'staff_002', 'hostel_001', 'Mario Rossi', 'Senior Plumber', '+1 555-0222', 'mario.plumb@campus.edu', 1, 1729000000000);

-- 13. Fee Types
INSERT OR REPLACE INTO fee_types (fee_type_id, hostel_id, name, default_amount, billing_cycle, created_at) VALUES
('ft_001', 'hostel_001', 'RENT', 450.0, 'MONTHLY', 1729000000000),
('ft_002', 'hostel_001', 'MESS', 120.0, 'MONTHLY', 1729000000000),
('ft_003', 'hostel_001', 'CAUTION_DEPOSIT', 200.0, 'ONE_TIME', 1729000000000),
('ft_004', 'hostel_001', 'ELECTRICITY', 30.0, 'MONTHLY', 1729000000000);

-- 14. Fees
INSERT OR REPLACE INTO fees (fee_id, hostel_id, student_id, room_id, title, fee_type, amount, amount_paid, due_date, billing_month, billing_year, status, created_at, updated_at) VALUES
('fee_001', 'hostel_001', 'std_001', 'room_204', 'October 2026 Accommodation & Mess', 'RENT', 450.0, 450.0, 1728500000000, 10, 2026, 'PAID', 1727740800000, 1729000000000),
('fee_002', 'hostel_001', 'std_001', 'room_204', 'November 2026 Accommodation & Mess', 'RENT', 450.0, 0.0, 1731196800000, 11, 2026, 'PENDING', 1730419200000, 1730419200000),
('fee_003', 'hostel_001', 'std_002', 'room_204', 'November 2026 Accommodation & Mess', 'RENT', 450.0, 450.0, 1731196800000, 11, 2026, 'PAID', 1730419200000, 1730500000000),
('fee_004', 'hostel_001', 'std_003', 'room_101', 'November 2026 Single Room Rent', 'RENT', 600.0, 0.0, 1731196800000, 11, 2026, 'PENDING', 1730419200000, 1730419200000);

-- 15. Payments
INSERT OR REPLACE INTO payments (payment_id, fee_id, student_id, hostel_id, amount_paid, payment_method, transaction_reference, payment_date, receipt_url, status, verified_by_host_id, remarks, created_at) VALUES
('pay_101', 'fee_001', 'std_001', 'hostel_001', 450.0, 'UPI', 'TXN-98421049-OCT', 1728400000000, 'https://receipts.campus.edu/pay_101.pdf', 'SUCCESS', 'host_001', 'Cleared via GPay UPI', 1728400000000),
('pay_102', 'fee_003', 'std_002', 'hostel_001', 450.0, 'CARD', 'TXN-98421050-NOV', 1730500000000, 'https://receipts.campus.edu/pay_102.pdf', 'SUCCESS', 'host_001', 'Cleared via Visa Debit', 1730500000000);

-- 16. Complaints
INSERT OR REPLACE INTO complaints (complaint_id, hostel_id, student_id, student_name, room_number, category, title, description, attachments, urgency, status, assigned_staff_name, host_notes, resolution_summary, created_at, resolved_at, updated_at) VALUES
('comp_001', 'hostel_001', 'std_001', 'Alex Mercer', 'A-204', 'ELECTRICAL', 'Study lamp socket sparking', 'The main wall power outlet near desk 1 has intermittent sparks when plugging in laptops.', '["https://images.unsplash.com/photo-1581092160607-ee22621dd758"]', 'HIGH', 'IN_PROGRESS', 'Carl Johnson (Electrician)', 'Technician dispatched for morning inspection.', NULL, 1729000000000, NULL, 1729086400000),
('comp_002', 'hostel_001', 'std_001', 'Alex Mercer', 'A-204', 'PLUMBING', 'Bathroom faucet low pressure', 'Water flow is very low in the morning hours.', '[]', 'LOW', 'RESOLVED', 'Mario Rossi', 'Assigned plumber', 'Aerator cleaned and valve adjusted.', 1728000000000, 1728100000000, 1728100000000),
('comp_003', 'hostel_001', 'std_003', 'Jordan Reed', 'B-101', 'WIFI', 'Wi-Fi signal weak in corner room', 'Speed drops below 1 Mbps in evening hours.', '[]', 'MEDIUM', 'OPEN', NULL, NULL, NULL, 1729100000000, NULL, 1729100000000);

-- 17. Maintenance Logs
INSERT OR REPLACE INTO maintenance_logs (maintenance_id, complaint_id, hostel_id, room_id, performed_by_staff_id, issue_type, action_taken, cost, maintenance_date, created_at) VALUES
('maint_001', 'comp_002', 'hostel_001', 'room_204', 'staff_002', 'PLUMBING', 'Replaced ceramic cartridge and cleaned aerator nozzle', 15.0, 1728100000000, 1728100000000);

-- 18. Leave Requests
INSERT OR REPLACE INTO leave_requests (leave_id, student_id, hostel_id, start_date, end_date, reason, emergency_contact_phone, status, approved_by, rejection_reason, remarks, created_at, updated_at) VALUES
('leave_001', 'std_003', 'hostel_001', '2026-10-22', '2026-10-25', 'Family wedding celebration in home city', '+1 555-0146', 'APPROVED', 'host_001', NULL, 'Parent consent verified via phone', 1729000000000, 1729050000000),
('leave_002', 'std_001', 'hostel_001', '2026-11-12', '2026-11-15', 'Attending National Hackathon Finals at MIT', '+1 555-0144', 'PENDING', NULL, NULL, NULL, 1730000000000, 1730000000000);

-- 19. Attendance Records
INSERT OR REPLACE INTO attendance_records (attendance_id, hostel_id, student_id, student_name, room_number, date, status, check_in_time, remarks, marked_by, leave_request_id, created_at) VALUES
('att_std1_20261018', 'hostel_001', 'std_001', 'Alex Mercer', 'A-204', '2026-10-18', 'PRESENT', 1729267200000, 'Regular roll-call', 'STUDENT_SELF', NULL, 1729267200000),
('att_std1_20261019', 'hostel_001', 'std_001', 'Alex Mercer', 'A-204', '2026-10-19', 'PRESENT', 1729353600000, 'Regular roll-call', 'STUDENT_SELF', NULL, 1729353600000),
('att_std1_20261020', 'hostel_001', 'std_001', 'Alex Mercer', 'A-204', '2026-10-20', 'PRESENT', 1729440000000, 'Regular roll-call', 'STUDENT_SELF', NULL, 1729440000000),
('att_std2_20261020', 'hostel_001', 'std_002', 'David Miller', 'A-204', '2026-10-20', 'PRESENT', 1729440000000, 'Regular roll-call', 'STUDENT_SELF', NULL, 1729440000000),
('att_std3_20261022', 'hostel_001', 'std_003', 'Jordan Reed', 'B-101', '2026-10-22', 'ON_LEAVE', NULL, 'Leave ID: leave_001', 'WARDEN', 'leave_001', 1729612800000),
('att_std4_20261020', 'hostel_001', 'std_004', 'Marcus Brody', 'A-205', '2026-10-20', 'ABSENT', NULL, 'Unexcused late absence', 'WARDEN', NULL, 1729440000000);

-- 20. Visitors
INSERT OR REPLACE INTO visitors (visitor_id, hostel_id, student_id, visitor_name, relationship, phone, id_proof_type, id_proof_number, purpose, check_in_time, check_out_time, approved_by, status, remarks, created_at) VALUES
('vis_001', 'hostel_001', 'std_001', 'Sarah Mercer', 'Mother', '+1 555-0144', 'Driving License', 'DL-NY-982138', 'Weekend visit & drop luggage', 1729240000000, 1729255000000, 'host_001', 'CHECKED_OUT', 'Common room visited', 1729240000000),
('vis_002', 'hostel_001', 'std_002', 'James Miller', 'Father', '+1 555-0145', 'Passport', 'PASS-US-48201', 'Semester fees discussion', 1729300000000, NULL, 'host_001', 'INSIDE', 'Visiting campus office', 1729300000000);

-- 21. Food Menus
INSERT OR REPLACE INTO food_menus (menu_id, hostel_id, week_start_date, schedule_json, special_notice, is_published, updated_at, created_at) VALUES
('menu_h1_current', 'hostel_001', '2026-10-19', '{
  "monday": {
    "breakfast": ["Poha", "Boiled Eggs / Sprouts", "Tea & Coffee"],
    "lunch": ["Steamed Rice", "Yellow Dal Tadka", "Paneer Butter Masala", "Curd"],
    "snacks": ["Vegetable Samosa", "Masala Chai"],
    "dinner": ["Butter Roti", "Mixed Vegetable Curry", "Jeera Rice", "Gulab Jamun"]
  },
  "tuesday": {
    "breakfast": ["Idli & Vada", "Coconut Chutney", "Sambar", "Filter Coffee"],
    "lunch": ["Jeera Rice", "Rajma Masala", "Aloo Gobi", "Salad"],
    "snacks": ["Cookies & Biscuits", "Tea"],
    "dinner": ["Phulka Roti", "Chicken Curry / Paneer Kadhai", "Dal Fry", "Ice Cream"]
  },
  "wednesday": {
    "breakfast": ["Aloo Paratha", "Curd & Pickle", "Tea"],
    "lunch": ["Veg Biryani", "Mirchi Ka Salan", "Raita", "Papad"],
    "snacks": ["Sandwich", "Coffee"],
    "dinner": ["Roti", "Dal Makhani", "Bhindi Masala", "Kheer"]
  },
  "thursday": {
    "breakfast": ["Upma", "Sambar", "Boiled Eggs", "Tea"],
    "lunch": ["Rice", "Chole Masala", "Bhature", "Salad"],
    "snacks": ["Puffs", "Tea"],
    "dinner": ["Roti", "Egg Curry / Malai Kofta", "Rice", "Custard"]
  },
  "friday": {
    "breakfast": ["Masala Dosa", "Sambar", "Chutney", "Coffee"],
    "lunch": ["Fried Rice", "Chilli Paneer / Manchurian", "Soup"],
    "snacks": ["Pakora", "Tea"],
    "dinner": ["Roti", "Dal Tadka", "Dum Aloo", "Jalebi"]
  },
  "saturday": {
    "breakfast": ["Puri Bhaji", "Halwa", "Tea"],
    "lunch": ["Curd Rice", "Lemon Rice", "Potato Fry", "Papad"],
    "snacks": ["Bhel Puri", "Juice"],
    "dinner": ["Naan", "Paneer Tikka Masala", "Pulao", "Rasgulla"]
  },
  "sunday": {
    "breakfast": ["Chole Bhature", "Lassi / Sweet Tea"],
    "lunch": ["Special Chicken Biryani / Hyderabadi Veg Biryani", "Raita", "Sweet"],
    "snacks": ["Pastry", "Cold Coffee"],
    "dinner": ["Light Khichdi", "Kadhi", "Papad", "Fruit Salad"]
  }
}', 'Sunday Special Feast will be served between 12:30 PM and 3:00 PM.', 1, 1729000000000, 1729000000000);

-- 22. Announcements
INSERT OR REPLACE INTO announcements (announcement_id, hostel_id, sender_id, sender_role, sender_name, title, message, priority, target_audience, attachment_urls, created_at, expires_at) VALUES
('anc_1', 'hostel_001', 'host_001', 'HOST', 'Hostel Warden', 'Monthly Wi-Fi Maintenance on Saturday', 'High-speed network maintenance will occur between 2:00 AM and 5:00 AM on Saturday.', 'NORMAL', 'ALL', '[]', 1729000000000, 1729500000000),
('anc_2', 'GLOBAL_CAMPUS', 'admin_001', 'ADMIN', 'Campus Housing Association', 'Winter Break Hostel Guidelines', 'All residents planning to stay during the winter term must submit vacation permission slips by Nov 25th.', 'IMPORTANT', 'ALL', '[]', 1729000000000, 1732500000000);

-- 23. App Notifications
INSERT OR REPLACE INTO notifications (notification_id, recipient_user_id, title, body, type, related_entity_id, is_read, created_at) VALUES
('notif_1', 'std_001', 'Rent Due Reminder', 'Your accommodation fee for November is due in 5 days.', 'PAYMENT_DUE', 'fee_002', 0, 1730419200000),
('notif_2', 'std_001', 'Complaint Status Updated', 'Your complaint #comp_001 has been assigned to Carl Johnson (Electrician).', 'COMPLAINT_UPDATE', 'comp_001', 0, 1729086400000),
('notif_3', 'std_003', 'Leave Application Approved', 'Your leave request for Oct 22 - Oct 25 has been approved by Warden.', 'LEAVE_APPROVED', 'leave_001', 1, 1729050000000);

-- 24. Audit Logs
INSERT OR REPLACE INTO audit_logs (log_id, user_id, action, entity_type, entity_id, details, ip_address, timestamp) VALUES
('log_001', 'host_001', 'ASSIGN_BED', 'BED', 'bed_1', 'Allocated Bed-A in Room A-204 to Alex Mercer (std_001)', '192.168.1.10', 1724000000000),
('log_002', 'std_001', 'RECORD_PAYMENT', 'PAYMENT', 'pay_101', 'Paid 450.0 for fee_001 via UPI (TXN-98421049-OCT)', '10.0.0.45', 1728400000000),
('log_003', 'std_001', 'SUBMIT_COMPLAINT', 'COMPLAINT', 'comp_001', 'Submitted high-urgency complaint: Study lamp socket sparking', '10.0.0.45', 1729000000000);
