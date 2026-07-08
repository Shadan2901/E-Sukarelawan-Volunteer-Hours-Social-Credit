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
  profile_program VARCHAR(140),
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
  (1, 'Volunteer Coordinator', 'admin@demo.my', 'ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f', 'admin', 'ADMIN-001', 'E-Volunteer Admin'),
  (2, 'Aisyah Maisarah', 'student@demo.my', '5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5', 'student', '2025427206', NULL),
  (3, 'Ahmad Shafiq Daniel Bin Salimi', 'shafiq@student.my', '5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5', 'student', '2025427207', NULL),
  (4, 'Nur Adlina Zainal', 'adlina@student.my', '5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5', 'student', '2025427208', NULL),
  (5, 'Muhammad Danish Hakim', 'danish@student.my', '5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5', 'student', '2025427209', NULL),
  (6, 'Farah Nazihah Roslan', 'farah@student.my', '5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5', 'student', '2025427210', NULL),
  (7, 'Haqim Rashid', 'haqim@student.my', '5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5', 'student', '2025427211', NULL),
  (8, 'E-Volunteer Outreach Team', 'outreach@demo.my', 'ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f', 'admin', 'NGO-OUTREACH', 'E-Volunteer Outreach'),
  (9, 'Green Earth Volunteers', 'green@demo.my', 'ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f', 'admin', 'NGO-GREEN', 'Green Earth Volunteers')
ON DUPLICATE KEY UPDATE
  full_name = VALUES(full_name),
  password_hash = VALUES(password_hash),
  role = VALUES(role),
  reference_id = VALUES(reference_id),
  ngo_name = VALUES(ngo_name);

INSERT INTO opportunities (id, admin_id, event, ngo, description, event_date, seats, location, status, category)
VALUES
  (1, 1, 'Langat River Cleanup', 'E-Volunteer', 'A community program to clean the river area with local residents.', '2025-05-24', 28, 'Kajang, Selangor', 'open', 'Environment'),
  (2, 1, 'Community Tuition Class', 'E-Volunteer', 'Support primary school pupils through weekend guidance classes.', '2025-05-31', 16, 'Bangi', 'open', 'Education'),
  (3, 1, 'Senior Care Visit and Donation Drive', 'E-Volunteer', 'Social support visit and donation handover at a care center.', '2025-06-07', 10, 'Kasih Harmoni Care Centre, Kajang', 'limited', 'Community'),
  (4, 1, 'Ramadan Community Kitchen', 'E-Volunteer', 'Prepare and distribute meals to families in need.', '2025-06-14', 0, 'Bangi', 'closed', 'Food Aid'),
  (5, 8, 'Community Blood Donation Campaign', 'E-Volunteer Outreach', 'Assist registration, queue management, and participant support for blood donors.', '2026-08-03', 35, 'Shah Alam Community Hall', 'open', 'Health'),
  (6, 9, 'Recreational Park Cleanup', 'Green Earth Volunteers', 'Clean the park, repaint public benches, and sort recyclable materials.', '2026-08-10', 24, 'Cyberjaya Lake Park', 'open', 'Environment'),
  (7, 8, 'Senior Digital Literacy Workshop', 'E-Volunteer Outreach', 'Teach smartphone basics, internet safety, and everyday app usage.', '2026-08-17', 18, 'Klang Senior Activity Centre', 'limited', 'Education'),
  (8, 9, 'Food Pack Relief Mission', 'Green Earth Volunteers', 'Organize and distribute food packs to families in need.', '2026-08-24', 30, 'Puchong Community Centre', 'open', 'Food Aid'),
  (9, 1, 'Volunteer Charity Run', 'E-Volunteer', 'Assist registration counters, route control, and participant water stations.', '2026-09-06', 40, 'Stadium UiTM Shah Alam', 'open', 'Sports'),
  (10, 8, 'Weekend SPM Coaching Class', 'E-Volunteer Outreach', 'Support secondary school students with Mathematics and English practice.', '2026-09-13', 20, 'Subang Community Library', 'open', 'Education')
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
  (3, 2, 3, '2025-05-21', 'pending', NULL, NULL),
  (4, 3, 5, '2026-07-12', 'approved', 8, '2026-07-13'),
  (5, 3, 6, '2026-07-13', 'pending', NULL, NULL),
  (6, 4, 5, '2026-07-14', 'approved', 8, '2026-07-14'),
  (7, 4, 7, '2026-07-15', 'pending', NULL, NULL),
  (8, 5, 8, '2026-07-16', 'approved', 9, '2026-07-17'),
  (9, 5, 9, '2026-07-17', 'approved', 1, '2026-07-18'),
  (10, 6, 6, '2026-07-18', 'approved', 9, '2026-07-18'),
  (11, 6, 10, '2026-07-19', 'pending', NULL, NULL),
  (12, 7, 8, '2026-07-20', 'approved', 9, '2026-07-21')
ON DUPLICATE KEY UPDATE
  student_id = VALUES(student_id),
  opportunity_id = VALUES(opportunity_id),
  application_date = VALUES(application_date),
  status = VALUES(status),
  reviewed_by_admin_id = VALUES(reviewed_by_admin_id),
  reviewed_date = VALUES(reviewed_date);

INSERT INTO volunteer_hours (id, student_id, student_name, opportunity_id, activity, amount, status, note, approved_by_admin_id)
VALUES
  (1, 2, NULL, 1, 'Langat River Cleanup', 84.5, 'approved', 'Attendance verified by program coordinator', 1),
  (2, 2, NULL, 2, 'Community Tuition Class', 36, 'approved', 'Teaching log completed', 1),
  (3, 2, NULL, 1, 'Langat River Cleanup', 2, 'pending', 'Additional reflection pending review', NULL),
  (4, 3, NULL, 5, 'Community Blood Donation Campaign', 6, 'approved', 'Registration counter and donor flow completed', 8),
  (5, 3, NULL, 5, 'Community Blood Donation Campaign', 4.5, 'pending', 'Additional donor support pending review', NULL),
  (6, 4, NULL, 5, 'Community Blood Donation Campaign', 5, 'approved', 'Helped manage donor waiting area', 8),
  (7, 4, NULL, 5, 'Community Blood Donation Campaign', 3, 'pending', 'Submitted reflection form', NULL),
  (8, 5, NULL, 8, 'Food Pack Relief Mission', 7.5, 'approved', 'Packed and distributed food aid', 9),
  (9, 5, NULL, 9, 'Volunteer Charity Run', 6, 'approved', 'Route marshal duty completed', 1),
  (10, 6, NULL, 6, 'Recreational Park Cleanup', 5.5, 'approved', 'Recycling station and cleanup duty', 9),
  (11, 6, NULL, 6, 'Recreational Park Cleanup', 2, 'pending', 'Pending attendance check', NULL),
  (12, 7, NULL, 8, 'Food Pack Relief Mission', 4, 'approved', 'Inventory and packing support', 9),
  (13, NULL, 'Arif Hakimi', 3, 'Senior Care Visit and Donation Drive', 5.5, 'pending', '10 May 2025', NULL),
  (14, NULL, 'Siti Hajar', 7, 'Senior Digital Literacy Workshop', 3.5, 'pending', 'Manual entry by admin', NULL)
ON DUPLICATE KEY UPDATE
  student_id = VALUES(student_id),
  student_name = VALUES(student_name),
  opportunity_id = VALUES(opportunity_id),
  activity = VALUES(activity),
  amount = VALUES(amount),
  status = VALUES(status),
  note = VALUES(note),
  approved_by_admin_id = VALUES(approved_by_admin_id);

INSERT INTO feedback (id, student_id, subject, message, status, created_at, admin_id, admin_reply, replied_at)
VALUES
  (1, 2, 'Certificate request', 'Can I get a certificate for the river cleanup program?', 'replied', '2026-07-01 09:30:00', 1, 'Yes, the certificate will be available in your profile after final verification.', '2026-07-01 14:15:00'),
  (2, 3, 'Unable to edit profile picture', 'My profile picture upload took a long time. Can admin check if it saved?', 'replied', '2026-07-02 10:20:00', 1, 'Your latest profile picture has been saved successfully.', '2026-07-02 12:10:00'),
  (3, 4, 'Opportunity location detail', 'Please add the exact hall name for the blood donation campaign.', 'open', '2026-07-03 11:05:00', NULL, NULL, NULL),
  (4, 5, 'Volunteer hours pending', 'My food aid hours are still pending after the event.', 'replied', '2026-07-04 15:45:00', 9, 'The hours have been reviewed and approved. Thank you for volunteering.', '2026-07-04 17:30:00'),
  (5, 6, 'New program suggestion', 'Can we add a beach cleanup program next month?', 'open', '2026-07-05 08:50:00', NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE
  student_id = VALUES(student_id),
  subject = VALUES(subject),
  message = VALUES(message),
  status = VALUES(status),
  created_at = VALUES(created_at),
  admin_id = VALUES(admin_id),
  admin_reply = VALUES(admin_reply),
  replied_at = VALUES(replied_at);
