package com.hostelhub.app.data.local.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HostelDatabaseHelper @Inject constructor(
    @ApplicationContext private val context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "hostelhub.db"
        const val DATABASE_VERSION = 1
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        createTables(db)
        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migrations handle schema upgrades
    }

    private fun createTables(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS roles (
                role_id TEXT PRIMARY KEY,
                role_name TEXT NOT NULL UNIQUE,
                description TEXT,
                created_at INTEGER NOT NULL
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS users (
                user_id TEXT PRIMARY KEY,
                email TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                role TEXT NOT NULL,
                full_name TEXT NOT NULL,
                phone_number TEXT,
                avatar_url TEXT,
                is_active INTEGER NOT NULL DEFAULT 1,
                fcm_token TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY (role) REFERENCES roles (role_name) ON UPDATE CASCADE
            );
        """.trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_users_role ON users (role);")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS hosts (
                host_id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL UNIQUE,
                full_name TEXT NOT NULL,
                business_name TEXT NOT NULL,
                contact_phone TEXT NOT NULL,
                contact_email TEXT NOT NULL,
                verified_status INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS admins (
                admin_id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL UNIQUE,
                full_name TEXT NOT NULL,
                association_name TEXT NOT NULL,
                designation TEXT NOT NULL,
                permissions TEXT NOT NULL DEFAULT '["ALL"]',
                contact_phone TEXT,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS hostels (
                hostel_id TEXT PRIMARY KEY,
                host_id TEXT NOT NULL,
                name TEXT NOT NULL,
                address TEXT NOT NULL,
                city TEXT NOT NULL,
                state TEXT DEFAULT '',
                postal_code TEXT DEFAULT '',
                latitude REAL DEFAULT 0.0,
                longitude REAL DEFAULT 0.0,
                description TEXT,
                gender_type TEXT NOT NULL,
                amenities TEXT NOT NULL DEFAULT '[]',
                rules TEXT NOT NULL DEFAULT '[]',
                images TEXT NOT NULL DEFAULT '[]',
                total_rooms INTEGER NOT NULL DEFAULT 0,
                total_beds INTEGER NOT NULL DEFAULT 0,
                occupied_beds INTEGER NOT NULL DEFAULT 0,
                base_monthly_rent REAL NOT NULL DEFAULT 0.0,
                caution_deposit REAL NOT NULL DEFAULT 0.0,
                rating REAL NOT NULL DEFAULT 0.0,
                rating_count INTEGER NOT NULL DEFAULT 0,
                contact_email TEXT,
                contact_phone TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY (host_id) REFERENCES hosts (host_id) ON DELETE RESTRICT
            );
        """.trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_hostels_city ON hostels (city);")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS blocks (
                block_id TEXT PRIMARY KEY,
                hostel_id TEXT NOT NULL,
                block_name TEXT NOT NULL,
                total_floors INTEGER NOT NULL DEFAULT 1,
                description TEXT,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE,
                UNIQUE (hostel_id, block_name)
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS floors (
                floor_id TEXT PRIMARY KEY,
                block_id TEXT NOT NULL,
                hostel_id TEXT NOT NULL,
                floor_number INTEGER NOT NULL,
                total_rooms INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (block_id) REFERENCES blocks (block_id) ON DELETE CASCADE,
                FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE,
                UNIQUE (block_id, floor_number)
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS rooms (
                room_id TEXT PRIMARY KEY,
                hostel_id TEXT NOT NULL,
                block_id TEXT,
                floor_id TEXT,
                room_number TEXT NOT NULL,
                floor INTEGER NOT NULL DEFAULT 1,
                block TEXT NOT NULL DEFAULT 'A',
                room_type TEXT NOT NULL,
                total_capacity INTEGER NOT NULL DEFAULT 2,
                occupied_count INTEGER NOT NULL DEFAULT 0,
                monthly_rent REAL NOT NULL DEFAULT 0.0,
                amenities TEXT NOT NULL DEFAULT '[]',
                status TEXT NOT NULL DEFAULT 'AVAILABLE',
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE,
                FOREIGN KEY (block_id) REFERENCES blocks (block_id) ON DELETE SET NULL,
                FOREIGN KEY (floor_id) REFERENCES floors (floor_id) ON DELETE SET NULL,
                UNIQUE (hostel_id, room_number)
            );
        """.trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_rooms_hostel ON rooms (hostel_id);")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS beds (
                bed_id TEXT PRIMARY KEY,
                room_id TEXT NOT NULL,
                bed_number TEXT NOT NULL,
                is_occupied INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (room_id) REFERENCES rooms (room_id) ON DELETE CASCADE,
                UNIQUE (room_id, bed_number)
            );
        """.trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_beds_room ON beds (room_id);")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS students (
                student_id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL UNIQUE,
                full_name TEXT NOT NULL,
                roll_number TEXT NOT NULL UNIQUE,
                college_name TEXT NOT NULL,
                course TEXT NOT NULL,
                year_of_study TEXT NOT NULL DEFAULT '1',
                gender TEXT NOT NULL,
                permanent_address TEXT NOT NULL,
                emergency_contact_name TEXT NOT NULL,
                emergency_contact_phone TEXT NOT NULL,
                hostel_id TEXT,
                hostel_name TEXT,
                room_id TEXT,
                room_number TEXT,
                bed_number TEXT,
                admission_date INTEGER,
                status TEXT NOT NULL DEFAULT 'ACTIVE',
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
                FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE SET NULL,
                FOREIGN KEY (room_id) REFERENCES rooms (room_id) ON DELETE SET NULL
            );
        """.trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_students_hostel ON students (hostel_id);")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS room_allocations (
                allocation_id TEXT PRIMARY KEY,
                bed_id TEXT NOT NULL,
                room_id TEXT NOT NULL,
                hostel_id TEXT NOT NULL,
                student_id TEXT NOT NULL,
                allocation_date INTEGER NOT NULL,
                check_in_date INTEGER,
                check_out_date INTEGER,
                status TEXT NOT NULL DEFAULT 'ACTIVE',
                allocated_by TEXT,
                remarks TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY (bed_id) REFERENCES beds (bed_id) ON DELETE RESTRICT,
                FOREIGN KEY (room_id) REFERENCES rooms (room_id) ON DELETE RESTRICT,
                FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE RESTRICT,
                FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
                FOREIGN KEY (allocated_by) REFERENCES users (user_id) ON DELETE SET NULL
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS staff (
                staff_id TEXT PRIMARY KEY,
                user_id TEXT UNIQUE,
                hostel_id TEXT NOT NULL,
                full_name TEXT NOT NULL,
                role_title TEXT NOT NULL,
                phone TEXT NOT NULL,
                email TEXT,
                is_available INTEGER NOT NULL DEFAULT 1,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE SET NULL,
                FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS fee_types (
                fee_type_id TEXT PRIMARY KEY,
                hostel_id TEXT NOT NULL,
                name TEXT NOT NULL,
                default_amount REAL NOT NULL DEFAULT 0.0,
                billing_cycle TEXT DEFAULT 'MONTHLY',
                created_at INTEGER NOT NULL,
                FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS fees (
                fee_id TEXT PRIMARY KEY,
                hostel_id TEXT NOT NULL,
                student_id TEXT NOT NULL,
                room_id TEXT,
                title TEXT NOT NULL,
                fee_type TEXT NOT NULL,
                amount REAL NOT NULL,
                amount_paid REAL NOT NULL DEFAULT 0.0,
                due_date INTEGER NOT NULL,
                billing_month INTEGER NOT NULL,
                billing_year INTEGER NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING',
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE,
                FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
                FOREIGN KEY (room_id) REFERENCES rooms (room_id) ON DELETE SET NULL
            );
        """.trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_fees_student ON fees (student_id);")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS payments (
                payment_id TEXT PRIMARY KEY,
                fee_id TEXT NOT NULL,
                student_id TEXT NOT NULL,
                hostel_id TEXT NOT NULL,
                amount_paid REAL NOT NULL,
                payment_method TEXT NOT NULL,
                transaction_reference TEXT NOT NULL UNIQUE,
                payment_date INTEGER NOT NULL,
                receipt_url TEXT,
                status TEXT NOT NULL DEFAULT 'SUCCESS',
                verified_by_host_id TEXT,
                remarks TEXT,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (fee_id) REFERENCES fees (fee_id) ON DELETE RESTRICT,
                FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE RESTRICT,
                FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE RESTRICT,
                FOREIGN KEY (verified_by_host_id) REFERENCES users (user_id) ON DELETE SET NULL
            );
        """.trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_payments_student ON payments (student_id);")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS complaints (
                complaint_id TEXT PRIMARY KEY,
                hostel_id TEXT NOT NULL,
                student_id TEXT NOT NULL,
                student_name TEXT NOT NULL,
                room_number TEXT NOT NULL,
                category TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                attachments TEXT NOT NULL DEFAULT '[]',
                urgency TEXT NOT NULL DEFAULT 'MEDIUM',
                status TEXT NOT NULL DEFAULT 'OPEN',
                assigned_staff_name TEXT,
                host_notes TEXT,
                resolution_summary TEXT,
                created_at INTEGER NOT NULL,
                resolved_at INTEGER,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE,
                FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE
            );
        """.trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_complaints_hostel ON complaints (hostel_id);")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_complaints_student ON complaints (student_id);")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS maintenance_logs (
                maintenance_id TEXT PRIMARY KEY,
                complaint_id TEXT,
                hostel_id TEXT NOT NULL,
                room_id TEXT,
                performed_by_staff_id TEXT,
                issue_type TEXT NOT NULL,
                action_taken TEXT NOT NULL,
                cost REAL DEFAULT 0.0,
                maintenance_date INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (complaint_id) REFERENCES complaints (complaint_id) ON DELETE SET NULL,
                FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE,
                FOREIGN KEY (room_id) REFERENCES rooms (room_id) ON DELETE SET NULL,
                FOREIGN KEY (performed_by_staff_id) REFERENCES staff (staff_id) ON DELETE SET NULL
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS leave_requests (
                leave_id TEXT PRIMARY KEY,
                student_id TEXT NOT NULL,
                hostel_id TEXT NOT NULL,
                start_date TEXT NOT NULL,
                end_date TEXT NOT NULL,
                reason TEXT NOT NULL,
                emergency_contact_phone TEXT,
                status TEXT NOT NULL DEFAULT 'PENDING',
                approved_by TEXT,
                rejection_reason TEXT,
                remarks TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
                FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE,
                FOREIGN KEY (approved_by) REFERENCES users (user_id) ON DELETE SET NULL
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS attendance_records (
                attendance_id TEXT PRIMARY KEY,
                hostel_id TEXT NOT NULL,
                student_id TEXT NOT NULL,
                student_name TEXT NOT NULL,
                room_number TEXT NOT NULL,
                date TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'PRESENT',
                check_in_time INTEGER,
                remarks TEXT,
                marked_by TEXT NOT NULL DEFAULT 'STUDENT_SELF',
                leave_request_id TEXT,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE,
                FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
                FOREIGN KEY (leave_request_id) REFERENCES leave_requests (leave_id) ON DELETE SET NULL,
                UNIQUE (student_id, date)
            );
        """.trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_attendance_student_date ON attendance_records (student_id, date);")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS visitors (
                visitor_id TEXT PRIMARY KEY,
                hostel_id TEXT NOT NULL,
                student_id TEXT NOT NULL,
                visitor_name TEXT NOT NULL,
                relationship TEXT NOT NULL,
                phone TEXT NOT NULL,
                id_proof_type TEXT,
                id_proof_number TEXT,
                purpose TEXT NOT NULL,
                check_in_time INTEGER NOT NULL,
                check_out_time INTEGER,
                approved_by TEXT,
                status TEXT NOT NULL DEFAULT 'INSIDE',
                remarks TEXT,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE,
                FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
                FOREIGN KEY (approved_by) REFERENCES users (user_id) ON DELETE SET NULL
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS food_menus (
                menu_id TEXT PRIMARY KEY,
                hostel_id TEXT NOT NULL,
                week_start_date TEXT NOT NULL,
                schedule_json TEXT NOT NULL,
                special_notice TEXT,
                is_published INTEGER NOT NULL DEFAULT 1,
                updated_at INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (hostel_id) REFERENCES hostels (hostel_id) ON DELETE CASCADE,
                UNIQUE (hostel_id, week_start_date)
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS announcements (
                announcement_id TEXT PRIMARY KEY,
                hostel_id TEXT NOT NULL DEFAULT 'GLOBAL_CAMPUS',
                sender_id TEXT NOT NULL,
                sender_role TEXT NOT NULL,
                sender_name TEXT NOT NULL,
                title TEXT NOT NULL,
                message TEXT NOT NULL,
                priority TEXT NOT NULL DEFAULT 'NORMAL',
                target_audience TEXT NOT NULL DEFAULT 'ALL',
                attachment_urls TEXT NOT NULL DEFAULT '[]',
                created_at INTEGER NOT NULL,
                expires_at INTEGER,
                FOREIGN KEY (sender_id) REFERENCES users (user_id) ON DELETE CASCADE
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS notifications (
                notification_id TEXT PRIMARY KEY,
                recipient_user_id TEXT NOT NULL,
                title TEXT NOT NULL,
                body TEXT NOT NULL,
                type TEXT NOT NULL,
                related_entity_id TEXT,
                is_read INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (recipient_user_id) REFERENCES users (user_id) ON DELETE CASCADE
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS audit_logs (
                log_id TEXT PRIMARY KEY,
                user_id TEXT,
                action TEXT NOT NULL,
                entity_type TEXT NOT NULL,
                entity_id TEXT,
                details TEXT,
                ip_address TEXT,
                timestamp INTEGER NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE SET NULL
            );
        """.trimIndent())
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        val now = System.currentTimeMillis()

        // Roles
        db.execSQL("INSERT OR REPLACE INTO roles VALUES ('role_admin', 'ADMIN', 'System Administrator', $now);")
        db.execSQL("INSERT OR REPLACE INTO roles VALUES ('role_host', 'HOST', 'Hostel Warden / Manager', $now);")
        db.execSQL("INSERT OR REPLACE INTO roles VALUES ('role_student', 'STUDENT', 'Hostel Student Resident', $now);")
        db.execSQL("INSERT OR REPLACE INTO roles VALUES ('role_staff', 'STAFF', 'Maintenance & Service Staff', $now);")

        // Users
        db.execSQL("INSERT OR REPLACE INTO users VALUES ('admin_001', 'admin@campus.edu', 'hash_admin', 'ADMIN', 'Dean Henderson', '+1 555-0100', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb', 1, NULL, $now, $now);")
        db.execSQL("INSERT OR REPLACE INTO users VALUES ('host_001', 'warden@greenvalley.edu', 'hash_host', 'HOST', 'Robert Vance', '+1 555-HOSTEL', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d', 1, NULL, $now, $now);")
        db.execSQL("INSERT OR REPLACE INTO users VALUES ('host_002', 'warden@stjude.edu', 'hash_host2', 'HOST', 'Sister Claire', '+1 555-STJUDE', 'https://images.unsplash.com/photo-1544005313-94ddf0286df2', 1, NULL, $now, $now);")
        db.execSQL("INSERT OR REPLACE INTO users VALUES ('std_001', 'student@campus.edu', 'hash_std1', 'STUDENT', 'Alex Mercer', '+1 555-0199', 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6', 1, NULL, $now, $now);")
        db.execSQL("INSERT OR REPLACE INTO users VALUES ('std_002', 'david.miller@campus.edu', 'hash_std2', 'STUDENT', 'David Miller', '+1 555-0188', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e', 1, NULL, $now, $now);")
        db.execSQL("INSERT OR REPLACE INTO users VALUES ('std_003', 'jordan.reed@campus.edu', 'hash_std3', 'STUDENT', 'Jordan Reed', '+1 555-0177', 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7', 1, NULL, $now, $now);")
        db.execSQL("INSERT OR REPLACE INTO users VALUES ('std_004', 'marcus.brody@campus.edu', 'hash_std4', 'STUDENT', 'Marcus Brody', '+1 555-0166', 'https://images.unsplash.com/photo-1492562080023-ab3db95bfbce', 1, NULL, $now, $now);")
        db.execSQL("INSERT OR REPLACE INTO users VALUES ('std_005', 'elena.rostova@campus.edu', 'hash_std5', 'STUDENT', 'Elena Rostova', '+1 555-0155', 'https://images.unsplash.com/photo-1517841905240-472988babdf9', 1, NULL, $now, $now);")
        db.execSQL("INSERT OR REPLACE INTO users VALUES ('staff_001', 'carl.electric@campus.edu', 'hash_staff1', 'STAFF', 'Carl Johnson', '+1 555-0211', NULL, 1, NULL, $now, $now);")

        // Admins & Hosts
        db.execSQL("INSERT OR REPLACE INTO admins VALUES ('adm_001', 'admin_001', 'Dean Henderson', 'Campus Housing Association', 'Dean of Student Welfare', '[\"ALL\"]', '+1 555-0100', $now);")
        db.execSQL("INSERT OR REPLACE INTO hosts VALUES ('host_001', 'host_001', 'Robert Vance', 'Green Valley Residences Inc', '+1 555-HOSTEL', 'warden@greenvalley.edu', 1, $now, $now);")
        db.execSQL("INSERT OR REPLACE INTO hosts VALUES ('host_002', 'host_002', 'Sister Claire', 'St. Jude Housing Trust', '+1 555-STJUDE', 'warden@stjude.edu', 1, $now, $now);")

        // Hostels
        db.execSQL("""
            INSERT OR REPLACE INTO hostels VALUES (
                'hostel_001', 'host_001', 'Green Valley Residencies', '12 North Campus Road, University District', 'Academic City', 'State', '10001', 40.7128, -74.0060,
                'Premium student housing with high-speed Wi-Fi, modern study pods, 24/7 security, gym, and nutritious catering.',
                'COED', '["Wi-Fi","Air Conditioning","Mess Included","24/7 Power Backup","Gym","Laundry"]', '["Curfew: 10:30 PM","No smoking on premises","Quiet hours after 11:00 PM"]', '["https://images.unsplash.com/photo-1555854877-bab0e564b8d5"]',
                30, 60, 52, 450.0, 200.0, 4.8, 124, 'warden@greenvalley.edu', '+1 555-HOSTEL', $now, $now
            );
        """.trimIndent())
        db.execSQL("""
            INSERT OR REPLACE INTO hostels VALUES (
                'hostel_002', 'host_002', 'St. Jude Student Suites', '45 West Avenue, Campus Perimeter', 'Academic City', 'State', '10002', 40.7135, -74.0080,
                'Cozy and affordable student dormitory close to the central library and sports pavilion.',
                'BOYS', '["Wi-Fi","Mess Included","CCTV Security","Study Hall"]', '["Curfew: 10:00 PM","Guests allowed till 8 PM"]', '["https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf"]',
                25, 50, 40, 380.0, 150.0, 4.5, 88, 'warden@stjude.edu', '+1 555-STJUDE', $now, $now
            );
        """.trimIndent())

        // Blocks & Floors
        db.execSQL("INSERT OR REPLACE INTO blocks VALUES ('blk_h1_a', 'hostel_001', 'A', 3, 'Block A - AC Deluxe Wing', $now);")
        db.execSQL("INSERT OR REPLACE INTO blocks VALUES ('blk_h1_b', 'hostel_001', 'B', 3, 'Block B - Standard Single Wing', $now);")
        db.execSQL("INSERT OR REPLACE INTO floors VALUES ('flr_h1_a_1', 'blk_h1_a', 'hostel_001', 1, 10, $now);")
        db.execSQL("INSERT OR REPLACE INTO floors VALUES ('flr_h1_a_2', 'blk_h1_a', 'hostel_001', 2, 10, $now);")
        db.execSQL("INSERT OR REPLACE INTO floors VALUES ('flr_h1_b_1', 'blk_h1_b', 'hostel_001', 1, 10, $now);")

        // Rooms
        db.execSQL("""
            INSERT OR REPLACE INTO rooms VALUES (
                'room_204', 'hostel_001', 'blk_h1_a', 'flr_h1_a_2', 'A-204', 2, 'A', 'DOUBLE', 2, 2, 450.0,
                '["AC","Attached Bath","Study Table","Balcony"]', 'FULL', $now, $now
            );
        """.trimIndent())
        db.execSQL("""
            INSERT OR REPLACE INTO rooms VALUES (
                'room_205', 'hostel_001', 'blk_h1_a', 'flr_h1_a_2', 'A-205', 2, 'A', 'DOUBLE', 2, 1, 450.0,
                '["AC","Attached Bath","Study Table"]', 'AVAILABLE', $now, $now
            );
        """.trimIndent())
        db.execSQL("""
            INSERT OR REPLACE INTO rooms VALUES (
                'room_101', 'hostel_001', 'blk_h1_b', 'flr_h1_b_1', 'B-101', 1, 'B', 'SINGLE', 1, 1, 600.0,
                '["AC","Attached Bath","Fridge","Study Table"]', 'FULL', $now, $now
            );
        """.trimIndent())

        // Beds
        db.execSQL("INSERT OR REPLACE INTO beds VALUES ('bed_1', 'room_204', 'Bed-A', 1, $now);")
        db.execSQL("INSERT OR REPLACE INTO beds VALUES ('bed_2', 'room_204', 'Bed-B', 1, $now);")
        db.execSQL("INSERT OR REPLACE INTO beds VALUES ('bed_3', 'room_205', 'Bed-A', 1, $now);")
        db.execSQL("INSERT OR REPLACE INTO beds VALUES ('bed_4', 'room_205', 'Bed-B', 0, $now);")
        db.execSQL("INSERT OR REPLACE INTO beds VALUES ('bed_5', 'room_101', 'Bed-A', 1, $now);")

        // Students
        db.execSQL("""
            INSERT OR REPLACE INTO students VALUES (
                'std_001', 'std_001', 'Alex Mercer', 'STD-2024-0042', 'College of Engineering', 'B.Tech Computer Science', '3', 'male',
                '42 Silicon Avenue, Metro City', 'Sarah Mercer (Mother)', '+1 555-0144', 'hostel_001', 'Green Valley Residencies',
                'room_204', 'A-204', 'Bed-A', $now, 'ACTIVE', $now, $now
            );
        """.trimIndent())
        db.execSQL("""
            INSERT OR REPLACE INTO students VALUES (
                'std_002', 'std_002', 'David Miller', 'STD-2024-0043', 'School of Management', 'BBA', '2', 'male',
                '88 Wall Street, Metro City', 'James Miller (Father)', '+1 555-0145', 'hostel_001', 'Green Valley Residencies',
                'room_204', 'A-204', 'Bed-B', $now, 'ACTIVE', $now, $now
            );
        """.trimIndent())
        db.execSQL("""
            INSERT OR REPLACE INTO students VALUES (
                'std_003', 'std_003', 'Jordan Reed', 'STD-2024-0088', 'Faculty of Arts & Sciences', 'B.Sc Physics', '1', 'male',
                '15 Newton Drive, Cambridge City', 'Arthur Reed (Father)', '+1 555-0146', 'hostel_001', 'Green Valley Residencies',
                'room_101', 'B-101', 'Bed-A', $now, 'ACTIVE', $now, $now
            );
        """.trimIndent())

        // Allocations
        db.execSQL("INSERT OR REPLACE INTO room_allocations VALUES ('alloc_001', 'bed_1', 'room_204', 'hostel_001', 'std_001', $now, $now, NULL, 'ACTIVE', 'host_001', 'Term allocation', $now, $now);")
        db.execSQL("INSERT OR REPLACE INTO room_allocations VALUES ('alloc_002', 'bed_2', 'room_204', 'hostel_001', 'std_002', $now, $now, NULL, 'ACTIVE', 'host_001', 'Term allocation', $now, $now);")
        db.execSQL("INSERT OR REPLACE INTO room_allocations VALUES ('alloc_003', 'bed_5', 'room_101', 'hostel_001', 'std_003', $now, $now, NULL, 'ACTIVE', 'host_001', 'Single room allocation', $now, $now);")

        // Fees & Payments
        db.execSQL("INSERT OR REPLACE INTO fees VALUES ('fee_001', 'hostel_001', 'std_001', 'room_204', 'October 2026 Accommodation & Mess', 'RENT', 450.0, 450.0, $now, 10, 2026, 'PAID', $now, $now);")
        db.execSQL("INSERT OR REPLACE INTO fees VALUES ('fee_002', 'hostel_001', 'std_001', 'room_204', 'November 2026 Accommodation & Mess', 'RENT', 450.0, 0.0, $now + 864000000, 11, 2026, 'PENDING', $now, $now);")
        db.execSQL("INSERT OR REPLACE INTO fees VALUES ('fee_003', 'hostel_001', 'std_002', 'room_204', 'November 2026 Accommodation & Mess', 'RENT', 450.0, 450.0, $now + 864000000, 11, 2026, 'PAID', $now, $now);")
        db.execSQL("INSERT OR REPLACE INTO payments VALUES ('pay_101', 'fee_001', 'std_001', 'hostel_001', 450.0, 'UPI', 'TXN-98421049-OCT', $now - 864000000, 'https://receipts.campus.edu/pay_101.pdf', 'SUCCESS', 'host_001', 'Paid via UPI', $now);")

        // Complaints
        db.execSQL("""
            INSERT OR REPLACE INTO complaints VALUES (
                'comp_001', 'hostel_001', 'std_001', 'Alex Mercer', 'A-204', 'ELECTRICAL',
                'Study lamp socket sparking', 'The main wall power outlet near desk 1 has intermittent sparks when plugging in laptops.',
                '[]', 'HIGH', 'IN_PROGRESS', 'Carl Johnson (Electrician)', 'Technician dispatched for morning inspection.', NULL,
                $now, NULL, $now
            );
        """.trimIndent())
        db.execSQL("""
            INSERT OR REPLACE INTO complaints VALUES (
                'comp_002', 'hostel_001', 'std_001', 'Alex Mercer', 'A-204', 'PLUMBING',
                'Bathroom faucet low pressure', 'Water flow is very low in the morning hours.',
                '[]', 'LOW', 'RESOLVED', 'Mario Rossi', 'Assigned plumber', 'Aerator cleaned and valve adjusted.',
                $now - 864000000, $now - 432000000, $now
            );
        """.trimIndent())
        db.execSQL("""
            INSERT OR REPLACE INTO complaints VALUES (
                'comp_003', 'hostel_001', 'std_003', 'Jordan Reed', 'B-101', 'WIFI',
                'Wi-Fi signal weak in corner room', 'Speed drops below 1 Mbps in evening.',
                '[]', 'MEDIUM', 'OPEN', NULL, NULL, NULL,
                $now, NULL, $now
            );
        """.trimIndent())

        // Leave Requests
        db.execSQL("INSERT OR REPLACE INTO leave_requests VALUES ('leave_001', 'std_003', 'hostel_001', '2026-10-22', '2026-10-25', 'Family wedding celebration in home city', '+1 555-0146', 'APPROVED', 'host_001', NULL, 'Parent verified', $now, $now);")

        // Attendance
        for (day in 1..20) {
            val dateStr = "2026-10-%02d".format(day)
            val statusStr = if (day % 10 == 0) "ABSENT" else if (day % 7 == 0) "ON_LEAVE" else "PRESENT"
            val leaveIdStr = if (day % 7 == 0) "'leave_001'" else "NULL"
            db.execSQL("INSERT OR REPLACE INTO attendance_records VALUES ('att_std1_$day', 'hostel_001', 'std_001', 'Alex Mercer', 'A-204', '$dateStr', '$statusStr', $now, 'Roll-call', 'STUDENT_SELF', $leaveIdStr, $now);")
        }
        db.execSQL("INSERT OR REPLACE INTO attendance_records VALUES ('att_std2_20', 'hostel_001', 'std_002', 'David Miller', 'A-204', '2026-10-20', 'PRESENT', $now, 'Roll-call', 'STUDENT_SELF', NULL, $now);")
        db.execSQL("INSERT OR REPLACE INTO attendance_records VALUES ('att_std3_20', 'hostel_001', 'std_003', 'Jordan Reed', 'B-101', '2026-10-20', 'ON_LEAVE', NULL, 'Leave approved', 'WARDEN', 'leave_001', $now);")

        // Visitors
        db.execSQL("INSERT OR REPLACE INTO visitors VALUES ('vis_001', 'hostel_001', 'std_001', 'Sarah Mercer', 'Mother', '+1 555-0144', 'DL', 'DL-982138', 'Drop luggage', $now - 3600000, $now, 'host_001', 'CHECKED_OUT', 'Common lounge', $now);")
        db.execSQL("INSERT OR REPLACE INTO visitors VALUES ('vis_002', 'hostel_001', 'std_002', 'James Miller', 'Father', '+1 555-0145', 'Passport', 'PASS-48201', 'Semester fees discussion', $now, NULL, 'host_001', 'INSIDE', 'Office room', $now);")

        // Food Menu
        val menuJson = """
        {
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
        }
        """.trimIndent().replace("'", "''")

        db.execSQL("INSERT OR REPLACE INTO food_menus VALUES ('menu_current', 'hostel_001', '2026-10-19', '$menuJson', 'Sunday Special Feast will be served between 12:30 PM and 3:00 PM.', 1, $now, $now);")

        // Announcements
        db.execSQL("INSERT OR REPLACE INTO announcements VALUES ('anc_1', 'hostel_001', 'host_001', 'HOST', 'Hostel Warden', 'Monthly Wi-Fi Maintenance on Saturday', 'High-speed network maintenance will occur between 2:00 AM and 5:00 AM on Saturday.', 'NORMAL', 'ALL', '[]', $now, $now + 864000000);")
        db.execSQL("INSERT OR REPLACE INTO announcements VALUES ('anc_2', 'GLOBAL_CAMPUS', 'admin_001', 'ADMIN', 'Campus Housing Association', 'Winter Break Hostel Guidelines', 'All residents planning to stay during the winter term must submit vacation permission slips by Nov 25th.', 'IMPORTANT', 'ALL', '[]', $now, $now + 864000000);")

        // Notifications
        db.execSQL("INSERT OR REPLACE INTO notifications VALUES ('notif_1', 'std_001', 'Rent Due Reminder', 'Your accommodation fee for November is due in 5 days.', 'PAYMENT_DUE', 'fee_002', 0, $now);")
        db.execSQL("INSERT OR REPLACE INTO notifications VALUES ('notif_2', 'std_001', 'Complaint Status Updated', 'Your complaint #comp_001 has been assigned to Carl Johnson (Electrician).', 'COMPLAINT_UPDATE', 'comp_001', 0, $now);")
        db.execSQL("INSERT OR REPLACE INTO notifications VALUES ('notif_3', 'std_003', 'Leave Application Approved', 'Your leave request for Oct 22 - Oct 25 has been approved by Warden.', 'LEAVE_APPROVED', 'leave_001', 1, $now);")

        // Audit Logs
        db.execSQL("INSERT OR REPLACE INTO audit_logs VALUES ('log_001', 'host_001', 'ASSIGN_BED', 'BED', 'bed_1', 'Allocated Bed-A in Room A-204 to Alex Mercer (std_001)', '127.0.0.1', $now);")
        db.execSQL("INSERT OR REPLACE INTO audit_logs VALUES ('log_002', 'std_001', 'RECORD_PAYMENT', 'PAYMENT', 'pay_101', 'Paid 450.0 for fee_001 via UPI (TXN-98421049-OCT)', '127.0.0.1', $now);")
    }
}
