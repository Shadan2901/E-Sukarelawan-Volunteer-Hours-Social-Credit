CREATE TABLE IF NOT EXISTS users (
  id INT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(160) NOT NULL UNIQUE,
  password_hash VARCHAR(128) NOT NULL,
  role VARCHAR(20) NOT NULL,
  reference_id VARCHAR(60) NOT NULL,
  ngo_name VARCHAR(120),
  profile_phone VARCHAR(40),
  profile_faculty VARCHAR(140),
  profile_programme VARCHAR(140),
  profile_bio TEXT,
  profile_photo LONGTEXT
);

CREATE TABLE IF NOT EXISTS opportunities (
  id INT PRIMARY KEY AUTO_INCREMENT,
  admin_id INT NOT NULL,
  event VARCHAR(160) NOT NULL,
  ngo VARCHAR(120) NOT NULL,
  description TEXT,
  event_date DATE NOT NULL,
  seats INT NOT NULL,
  location VARCHAR(180) NOT NULL,
  status VARCHAR(20) NOT NULL,
  category VARCHAR(80) NOT NULL,
  FOREIGN KEY (admin_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS applications (
  id INT PRIMARY KEY AUTO_INCREMENT,
  student_id INT NOT NULL,
  opportunity_id INT NOT NULL,
  application_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL,
  reviewed_by_admin_id INT,
  reviewed_date DATE,
  UNIQUE KEY unique_student_opportunity (student_id, opportunity_id),
  FOREIGN KEY (student_id) REFERENCES users(id),
  FOREIGN KEY (opportunity_id) REFERENCES opportunities(id),
  FOREIGN KEY (reviewed_by_admin_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS volunteer_hours (
  id INT PRIMARY KEY AUTO_INCREMENT,
  student_id INT,
  student_name VARCHAR(120),
  opportunity_id INT NOT NULL,
  activity VARCHAR(160) NOT NULL,
  amount DECIMAL(6,1) NOT NULL,
  status VARCHAR(20) NOT NULL,
  note TEXT,
  approved_by_admin_id INT,
  FOREIGN KEY (student_id) REFERENCES users(id),
  FOREIGN KEY (opportunity_id) REFERENCES opportunities(id),
  FOREIGN KEY (approved_by_admin_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS feedback (
  id INT PRIMARY KEY AUTO_INCREMENT,
  student_id INT NOT NULL,
  subject VARCHAR(160) NOT NULL,
  message TEXT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'open',
  created_at DATETIME NOT NULL,
  admin_id INT,
  admin_reply TEXT,
  replied_at DATETIME,
  FOREIGN KEY (student_id) REFERENCES users(id),
  FOREIGN KEY (admin_id) REFERENCES users(id)
);

INSERT INTO users (id, full_name, email, password_hash, role, reference_id, ngo_name)
VALUES
  (1, 'Volunteer Coordinator', 'admin@demo.my', 'ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f', 'admin', 'ADMIN-001', 'E-Sukarelawan Admin'),
  (2, 'Aisyah Maisarah', 'student@demo.my', '5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5', 'student', '2025427206', NULL)
ON DUPLICATE KEY UPDATE
  full_name = VALUES(full_name),
  password_hash = VALUES(password_hash),
  role = VALUES(role),
  reference_id = VALUES(reference_id),
  ngo_name = VALUES(ngo_name);

INSERT INTO opportunities (id, admin_id, event, ngo, description, event_date, seats, location, status, category)
VALUES
  (1, 1, 'Program Pembersihan Sungai Langat', 'E-Sukarelawan', 'Program komuniti membersihkan kawasan sungai bersama penduduk setempat.', '2025-05-24', 28, 'Kajang, Selangor', 'open', 'Environment'),
  (2, 1, 'Kelas Tuisyen Komuniti', 'E-Sukarelawan', 'Bantu pelajar sekolah rendah melalui kelas bimbingan hujung minggu.', '2025-05-31', 16, 'Bangi', 'open', 'Education'),
  (3, 1, 'Sahabat Warga: Lawatan & Sumbangan', 'E-Sukarelawan', 'Lawatan sokongan sosial dan penyerahan sumbangan ke pusat jagaan.', '2025-06-07', 10, 'Pusat Jagaan Kasih Harmoni, Kajang', 'limited', 'Community'),
  (4, 1, 'Dapur Komuniti Ramadan', 'E-Sukarelawan', 'Menyediakan dan mengagihkan makanan kepada keluarga memerlukan.', '2025-06-14', 0, 'Bangi', 'closed', 'Food Aid')
ON DUPLICATE KEY UPDATE
  admin_id = VALUES(admin_id),
  event = VALUES(event),
  ngo = VALUES(ngo),
  description = VALUES(description),
  event_date = VALUES(event_date),
  seats = VALUES(seats),
  location = VALUES(location),
  status = VALUES(status),
  category = VALUES(category);

INSERT INTO applications (id, student_id, opportunity_id, application_date, status, reviewed_by_admin_id, reviewed_date)
VALUES
  (1, 2, 1, '2025-05-18', 'approved', 1, '2025-05-18'),
  (2, 2, 2, '2025-05-20', 'approved', 1, '2025-05-20'),
  (3, 2, 3, '2025-05-21', 'pending', NULL, NULL)
ON DUPLICATE KEY UPDATE
  application_date = VALUES(application_date),
  status = VALUES(status),
  reviewed_by_admin_id = VALUES(reviewed_by_admin_id),
  reviewed_date = VALUES(reviewed_date);

INSERT INTO volunteer_hours (id, student_id, student_name, opportunity_id, activity, amount, status, note, approved_by_admin_id)
VALUES
  (1, 2, NULL, 1, 'Program Pembersihan Sungai Langat', 84.5, 'approved', 'Attendance verified by programme coordinator', 1),
  (2, 2, NULL, 2, 'Kelas Tuisyen Komuniti', 36, 'approved', 'Teaching log completed', 1),
  (3, 2, NULL, 3, 'Sahabat Warga: Lawatan & Sumbangan', 2, 'pending', 'Reflection pending review', NULL),
  (4, NULL, 'Muhammad Danish', 1, 'Program Pembersihan Sungai Langat', 6, 'pending', '18 May 2025', NULL),
  (5, NULL, 'Nur Adlina', 2, 'Kelas Tuisyen Komuniti', 4, 'pending', '11 May 2025', NULL),
  (6, NULL, 'Arif Hakimi', 3, 'Sahabat Warga: Lawatan & Sumbangan', 5.5, 'pending', '10 May 2025', NULL),
  (7, NULL, 'Farah Nazihah', 1, 'Program Pembersihan Sungai Langat', 6, 'approved', '4 May 2025', 1),
  (8, NULL, 'Haqim Rashid', 2, 'Kelas Tuisyen Komuniti', 3, 'approved', '3 May 2025', 1)
ON DUPLICATE KEY UPDATE
  student_id = VALUES(student_id),
  student_name = VALUES(student_name),
  opportunity_id = VALUES(opportunity_id),
  activity = VALUES(activity),
  amount = VALUES(amount),
  status = VALUES(status),
  note = VALUES(note),
  approved_by_admin_id = VALUES(approved_by_admin_id);
