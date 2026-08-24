import sqlite3
import json

def test_hostel_database():
    conn = sqlite3.connect(':memory:')
    conn.execute("PRAGMA foreign_keys = ON;")
    
    # 1. Load Schema
    with open('database/schema.sql', 'r', encoding='utf-8') as f:
        conn.executescript(f.read())
    print("[PASS] Schema loaded successfully.")

    # 2. Load Seed Data
    with open('database/seed.sql', 'r', encoding='utf-8') as f:
        conn.executescript(f.read())
    print("[PASS] Seed data loaded successfully.")

    # 3. Test Relational Join: Student -> Bed -> Room -> Hostel -> Host
    query = """
    SELECT 
        s.full_name as student_name,
        s.roll_number,
        h.name as hostel_name,
        r.room_number,
        r.room_type,
        b.bed_number,
        w.full_name as warden_name
    FROM students s
    JOIN hostels h ON s.hostel_id = h.hostel_id
    JOIN rooms r ON s.room_id = r.room_id
    JOIN beds b ON b.room_id = r.room_id AND b.bed_number = s.bed_number
    JOIN hosts w ON h.host_id = w.host_id
    WHERE s.student_id = 'std_001'
    """
    row = conn.execute(query).fetchone()
    assert row is not None, "Failed to join student, bed, room, hostel, and host"
    print(f"[PASS] Relational query joined successfully: {row}")
    assert row[0] == "Alex Mercer"
    assert row[2] == "Green Valley Residencies"
    assert row[3] == "A-204"
    assert row[5] == "Bed-A"
    assert row[6] == "Robert Vance"

    # 4. Test Uniqueness Constraint: Duplicate Student Roll Number
    try:
        conn.execute("""
            INSERT INTO students (student_id, user_id, full_name, roll_number, college_name, course, gender, permanent_address, emergency_contact_name, emergency_contact_phone)
            VALUES ('std_dup', 'user_dup', 'Duplicate Student', 'STD-2024-0042', 'Eng', 'CS', 'male', 'Addr', 'Contact', '555')
        """)
        raise AssertionError("Duplicate roll number constraint failed to trigger!")
    except sqlite3.IntegrityError:
        print("[PASS] UNIQUE constraint on students.roll_number verified.")

    # 5. Test Uniqueness Constraint: Duplicate Room in Same Hostel
    try:
        conn.execute("""
            INSERT INTO rooms (room_id, hostel_id, room_number, room_type, total_capacity, occupied_count, monthly_rent)
            VALUES ('room_dup', 'hostel_001', 'A-204', 'DOUBLE', 2, 0, 450.0)
        """)
        raise AssertionError("Duplicate hostel room constraint failed to trigger!")
    except sqlite3.IntegrityError:
        print("[PASS] UNIQUE constraint on rooms (hostel_id, room_number) verified.")

    # 6. Test Uniqueness Constraint: Duplicate Bed in Same Room
    try:
        conn.execute("""
            INSERT INTO beds (bed_id, room_id, bed_number, is_occupied)
            VALUES ('bed_dup', 'room_204', 'Bed-A', 0)
        """)
        raise AssertionError("Duplicate room bed constraint failed to trigger!")
    except sqlite3.IntegrityError:
        print("[PASS] UNIQUE constraint on beds (room_id, bed_number) verified.")

    # 7. Test Uniqueness Constraint: Duplicate Attendance on Same Date
    try:
        conn.execute("""
            INSERT INTO attendance_records (attendance_id, hostel_id, student_id, student_name, room_number, date, status)
            VALUES ('att_dup', 'hostel_001', 'std_001', 'Alex Mercer', 'A-204', '2026-10-20', 'PRESENT')
        """)
        raise AssertionError("Duplicate attendance constraint failed to trigger!")
    except sqlite3.IntegrityError:
        print("[PASS] UNIQUE constraint on attendance_records (student_id, date) verified.")

    # 8. Test Foreign Key Constraint Violation (Invalid Hostel ID)
    try:
        conn.execute("""
            INSERT INTO rooms (room_id, hostel_id, room_number, room_type, total_capacity, occupied_count, monthly_rent)
            VALUES ('room_invalid', 'non_existent_hostel', 'Z-999', 'DOUBLE', 2, 0, 450.0)
        """)
        raise AssertionError("Foreign key constraint failed to trigger on invalid hostel_id!")
    except sqlite3.IntegrityError:
        print("[PASS] FOREIGN KEY constraint on rooms.hostel_id verified.")

    # 9. Test Payments and Fees Update Logic
    # Check pending fee
    pending_fee = conn.execute("SELECT amount, amount_paid, status FROM fees WHERE fee_id = 'fee_002'").fetchone()
    assert pending_fee[2] == 'PENDING'
    
    # Record payment for fee_002
    conn.execute("""
        INSERT INTO payments (payment_id, fee_id, student_id, hostel_id, amount_paid, payment_method, transaction_reference, payment_date, status)
        VALUES ('pay_nov_001', 'fee_002', 'std_001', 'hostel_001', 450.0, 'UPI', 'TXN-NOV-1199', 1730500000000, 'SUCCESS')
    """)
    conn.execute("""
        UPDATE fees 
        SET amount_paid = amount_paid + 450.0,
            status = 'PAID'
        WHERE fee_id = 'fee_002'
    """)
    updated_fee = conn.execute("SELECT amount, amount_paid, status FROM fees WHERE fee_id = 'fee_002'").fetchone()
    assert updated_fee[1] == 450.0 and updated_fee[2] == 'PAID'
    print("[PASS] Fee payment processing and status update verified.")

    # 10. Test Complaints Status Update & Resolution
    conn.execute("""
        UPDATE complaints
        SET status = 'RESOLVED',
            resolution_summary = 'Wall socket replaced with 16A modular switch and tested under load.',
            resolved_at = 1729100000000
        WHERE complaint_id = 'comp_001'
    """)
    resolved_comp = conn.execute("SELECT status, resolution_summary FROM complaints WHERE complaint_id = 'comp_001'").fetchone()
    assert resolved_comp[0] == 'RESOLVED' and '16A modular switch' in resolved_comp[1]
    print("[PASS] Complaint workflow and resolution status verified.")

    # 11. Test Bed Allocation and Capacity Counting
    conn.execute("""
        INSERT INTO room_allocations (allocation_id, bed_id, room_id, hostel_id, student_id, allocation_date, status)
        VALUES ('alloc_005', 'bed_4', 'room_205', 'hostel_001', 'std_005', 1729200000000, 'ACTIVE')
    """)
    conn.execute("UPDATE beds SET is_occupied = 1 WHERE bed_id = 'bed_4'")
    conn.execute("UPDATE rooms SET occupied_count = occupied_count + 1, status = 'FULL' WHERE room_id = 'room_205'")
    conn.execute("UPDATE students SET hostel_id = 'hostel_001', room_id = 'room_205', room_number = 'A-205', bed_number = 'Bed-B', status = 'ACTIVE' WHERE student_id = 'std_005'")
    
    assigned_bed = conn.execute("SELECT is_occupied FROM beds WHERE bed_id = 'bed_4'").fetchone()
    assigned_room = conn.execute("SELECT occupied_count, status FROM rooms WHERE room_id = 'room_205'").fetchone()
    assert assigned_bed[0] == 1
    assert assigned_room[0] == 2 and assigned_room[1] == 'FULL'
    print("[PASS] Bed allocation, room occupancy count and student link verified.")

    print("\n==========================================")
    print("ALL 11 DATABASE TESTS PASSED WITH 100% SUCCESS")
    print("==========================================")
    conn.close()

if __name__ == '__main__':
    test_hostel_database()
