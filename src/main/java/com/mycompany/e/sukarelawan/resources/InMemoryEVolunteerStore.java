package com.mycompany.e.sukarelawan.resources;

import com.mycompany.e.sukarelawan.resources.ApiModels.AppState;
import com.mycompany.e.sukarelawan.resources.ApiModels.Application;
import com.mycompany.e.sukarelawan.resources.ApiModels.ApplicationRequest;
import com.mycompany.e.sukarelawan.resources.ApiModels.Feedback;
import com.mycompany.e.sukarelawan.resources.ApiModels.FeedbackReplyRequest;
import com.mycompany.e.sukarelawan.resources.ApiModels.FeedbackRequest;
import com.mycompany.e.sukarelawan.resources.ApiModels.HourRecord;
import com.mycompany.e.sukarelawan.resources.ApiModels.LoginRequest;
import com.mycompany.e.sukarelawan.resources.ApiModels.Opportunity;
import com.mycompany.e.sukarelawan.resources.ApiModels.Profile;
import com.mycompany.e.sukarelawan.resources.ApiModels.ProfileRequest;
import com.mycompany.e.sukarelawan.resources.ApiModels.RegisterRequest;
import com.mycompany.e.sukarelawan.resources.ApiModels.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class InMemoryEVolunteerStore implements EVolunteerStore {
    private final AppState data = new AppState();

    public InMemoryEVolunteerStore() {
        seed();
    }

    @Override
    public synchronized AppState state() {
        return data;
    }

    @Override
    public synchronized User login(LoginRequest request) {
        requireFilled("Role", request.role);
        requireFilled("Email", request.email);
        requireFilled("Password", request.password);
        String email = normalize(request.email);
        for (User user : data.users) {
            if (normalize(user.email).equals(email) && safe(user.role).equals(request.role)) {
                if (safe(user.passwordHash).equals(sha256(request.password))
                        || ("admin@demo.my".equals(email) && "12345678".equals(request.password))
                        || ("student@demo.my".equals(email) && "12345".equals(request.password))) {
                    return publicUser(user);
                }
            }
        }
        throw new IllegalArgumentException("Email, password, or role is incorrect.");
    }

    @Override
    public synchronized User register(RegisterRequest request) {
        requireFilled("Full name", request.fullName);
        requireFilled("Email", request.email);
        requireFilled("Role", request.role);
        requireFilled("Student ID / NGO code", request.referenceId);
        requireFilled("Password", request.password);
        String email = normalize(request.email);
        for (User user : data.users) {
            if (normalize(user.email).equals(email)) {
                throw new IllegalArgumentException("An account with this email already exists.");
            }
        }
        User user = new User();
        user.id = nextUserId();
        user.fullName = request.fullName;
        user.email = email;
        user.passwordHash = sha256(request.password);
        user.role = request.role;
        user.referenceId = request.referenceId;
        user.ngoName = "admin".equals(request.role) ? request.fullName : null;
        data.users.add(user);
        return publicUser(user);
    }

    @Override
    public synchronized Opportunity createOpportunity(Opportunity opportunity) {
        opportunity.id = nextOpportunityId();
        data.opportunities.add(0, opportunity);
        return opportunity;
    }

    @Override
    public synchronized Opportunity updateOpportunity(int id, Opportunity incoming) {
        Opportunity opportunity = findOpportunity(id);
        opportunity.adminId = incoming.adminId;
        opportunity.event = incoming.event;
        opportunity.ngo = incoming.ngo;
        opportunity.description = incoming.description;
        opportunity.date = incoming.date;
        opportunity.seats = incoming.seats;
        opportunity.location = incoming.location;
        opportunity.status = incoming.status;
        opportunity.category = incoming.category;
        return opportunity;
    }

    @Override
    public synchronized void deleteOpportunity(int id) {
        data.opportunities.removeIf(item -> item.id == id);
        data.applications.removeIf(item -> item.opportunityId == id);
    }

    @Override
    public synchronized void apply(ApplicationRequest request) {
        Opportunity opportunity = findOpportunity(request.opportunityId);
        if ("closed".equals(opportunity.status)) {
            throw new IllegalArgumentException("This opportunity is already closed.");
        }
        for (Application item : data.applications) {
            if (item.studentId == request.studentId && item.opportunityId == request.opportunityId) {
                throw new IllegalArgumentException("You have already applied for this opportunity.");
            }
        }
        Application application = new Application();
        application.id = nextApplicationId();
        application.studentId = request.studentId;
        application.opportunityId = request.opportunityId;
        application.applicationDate = LocalDate.now().toString();
        application.status = "pending";
        data.applications.add(application);
        opportunity.seats = Math.max(0, opportunity.seats - 1);
        if (opportunity.seats == 0) {
            opportunity.status = "closed";
        }
    }

    @Override
    public synchronized HourRecord submitHours(HourRecord record) {
        record.id = nextHourId();
        record.status = "pending";
        record.approvedByAdminId = null;
        data.hours.add(0, record);
        return record;
    }

    @Override
    public synchronized HourRecord reviewHours(int id, String action, int adminId) {
        for (HourRecord record : data.hours) {
            if (record.id == id) {
                record.status = "approve".equals(action) ? "approved" : "rejected";
                record.approvedByAdminId = adminId;
                return record;
            }
        }
        throw new IllegalArgumentException("Hour record was not found.");
    }

    @Override
    public synchronized Application reviewApplication(int id, String action, int adminId) {
        for (Application application : data.applications) {
            if (application.id == id) {
                application.status = "approve".equals(action) ? "approved" : "rejected";
                application.reviewedByAdminId = adminId;
                application.reviewedDate = LocalDate.now().toString();
                return application;
            }
        }
        throw new IllegalArgumentException("Application was not found.");
    }

    @Override
    public synchronized User updateProfile(int id, ProfileRequest request) {
        for (User user : data.users) {
            if (user.id == id) {
                user.fullName = request.fullName;
                user.email = normalize(request.email);
                user.referenceId = request.referenceId;
                user.profile = copyProfile(request.profile);
                if ("admin".equals(user.role)) {
                    user.ngoName = request.fullName;
                }
                return publicUser(user);
            }
        }
        throw new IllegalArgumentException("User was not found.");
    }

    @Override
    public synchronized Feedback submitFeedback(FeedbackRequest request) {
        if (request.subject == null || request.subject.isBlank()) {
            throw new IllegalArgumentException("Feedback subject is required.");
        }
        if (request.message == null || request.message.isBlank()) {
            throw new IllegalArgumentException("Feedback message is required.");
        }
        Feedback feedback = new Feedback();
        feedback.id = nextFeedbackId();
        feedback.studentId = request.studentId;
        feedback.subject = request.subject.trim();
        feedback.message = request.message.trim();
        feedback.status = "open";
        feedback.createdAt = LocalDateTime.now().toString();
        data.feedback.add(0, feedback);
        return feedback;
    }

    @Override
    public synchronized Feedback replyFeedback(int id, FeedbackReplyRequest request) {
        if (request.reply == null || request.reply.isBlank()) {
            throw new IllegalArgumentException("Reply message is required.");
        }
        for (Feedback feedback : data.feedback) {
            if (feedback.id == id) {
                feedback.adminId = request.adminId;
                feedback.adminReply = request.reply.trim();
                feedback.repliedAt = LocalDateTime.now().toString();
                feedback.status = "replied";
                return feedback;
            }
        }
        throw new IllegalArgumentException("Feedback was not found.");
    }

    private Opportunity findOpportunity(int id) {
        for (Opportunity opportunity : data.opportunities) {
            if (opportunity.id == id) {
                return opportunity;
            }
        }
        throw new IllegalArgumentException("Opportunity was not found.");
    }

    private int nextUserId() {
        int next = 1;
        for (User item : data.users) next = Math.max(next, item.id + 1);
        return next;
    }

    private int nextOpportunityId() {
        int next = 1;
        for (Opportunity item : data.opportunities) next = Math.max(next, item.id + 1);
        return next;
    }

    private int nextApplicationId() {
        int next = 1;
        for (Application item : data.applications) next = Math.max(next, item.id + 1);
        return next;
    }

    private int nextHourId() {
        int next = 1;
        for (HourRecord item : data.hours) next = Math.max(next, item.id + 1);
        return next;
    }

    private int nextFeedbackId() {
        int next = 1;
        for (Feedback item : data.feedback) next = Math.max(next, item.id + 1);
        return next;
    }

    private static String normalize(String value) {
        return safe(value).trim().toLowerCase();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static void requireFilled(String label, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
    }

    private static User publicUser(User user) {
        User copy = new User();
        copy.id = user.id;
        copy.fullName = user.fullName;
        copy.email = user.email;
        copy.passwordHash = user.passwordHash;
        copy.role = user.role;
        copy.referenceId = user.referenceId;
        copy.ngoName = user.ngoName;
        copy.profile = copyProfile(user.profile);
        return copy;
    }

    private static Profile copyProfile(Profile profile) {
        if (profile == null) return null;
        Profile copy = new Profile();
        copy.phone = profile.phone;
        copy.faculty = profile.faculty;
        copy.programme = profile.programme;
        copy.bio = profile.bio;
        copy.photo = profile.photo;
        return copy;
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(safe(value).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : hash) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Password hashing is unavailable.", exception);
        }
    }

    private void seed() {
        data.users.add(user(1, "Volunteer Coordinator", "admin@demo.my", "admin", "ADMIN-001", "E-Sukarelawan Admin"));
        data.users.add(user(2, "Aisyah Maisarah", "student@demo.my", "student", "2025427206", null));
        data.users.add(user(3, "Ahmad Shafiq Daniel Bin Salimi", "shafiq@student.my", "student", "2025427207", null));
        data.users.add(user(4, "Nur Adlina Zainal", "adlina@student.my", "student", "2025427208", null));
        data.users.add(user(5, "Muhammad Danish Hakim", "danish@student.my", "student", "2025427209", null));
        data.users.add(user(6, "Farah Nazihah Roslan", "farah@student.my", "student", "2025427210", null));
        data.users.add(user(7, "Haqim Rashid", "haqim@student.my", "student", "2025427211", null));
        data.users.add(user(8, "E-Sukarelawan Outreach Team", "outreach@demo.my", "admin", "NGO-OUTREACH", "E-Sukarelawan Outreach"));
        data.users.add(user(9, "Green Earth Volunteers", "green@demo.my", "admin", "NGO-GREEN", "Green Earth Volunteers"));

        data.opportunities.add(opportunity(1, 1, "Program Pembersihan Sungai Langat", "E-Sukarelawan", "Program komuniti membersihkan kawasan sungai bersama penduduk setempat.", "2025-05-24", 28, "Kajang, Selangor", "open", "Environment"));
        data.opportunities.add(opportunity(2, 1, "Kelas Tuisyen Komuniti", "E-Sukarelawan", "Bantu pelajar sekolah rendah melalui kelas bimbingan hujung minggu.", "2025-05-31", 16, "Bangi", "open", "Education"));
        data.opportunities.add(opportunity(3, 1, "Sahabat Warga: Lawatan & Sumbangan", "E-Sukarelawan", "Lawatan sokongan sosial dan penyerahan sumbangan ke pusat jagaan.", "2025-06-07", 10, "Pusat Jagaan Kasih Harmoni, Kajang", "limited", "Community"));
        data.opportunities.add(opportunity(4, 1, "Dapur Komuniti Ramadan", "E-Sukarelawan", "Menyediakan dan mengagihkan makanan kepada keluarga memerlukan.", "2025-06-14", 0, "Bangi", "closed", "Food Aid"));
        data.opportunities.add(opportunity(5, 8, "Kempen Derma Darah Komuniti", "E-Sukarelawan Outreach", "Membantu pendaftaran, pengurusan barisan, dan sokongan peserta derma darah.", "2026-08-03", 35, "Dewan Komuniti Shah Alam", "open", "Health"));
        data.opportunities.add(opportunity(6, 9, "Gotong-Royong Taman Rekreasi", "Green Earth Volunteers", "Membersihkan taman, mengecat bangku awam, dan mengasingkan bahan kitar semula.", "2026-08-10", 24, "Taman Tasik Cyberjaya", "open", "Environment"));
        data.opportunities.add(opportunity(7, 8, "Bengkel Literasi Digital Warga Emas", "E-Sukarelawan Outreach", "Mengajar asas telefon pintar, keselamatan internet, dan penggunaan aplikasi harian.", "2026-08-17", 18, "Pusat Aktiviti Warga Emas Klang", "limited", "Education"));
        data.opportunities.add(opportunity(8, 9, "Misi Bantuan Pek Makanan", "Green Earth Volunteers", "Menyusun dan mengagihkan pek makanan kepada keluarga memerlukan.", "2026-08-24", 30, "Pusat Komuniti Puchong", "open", "Food Aid"));
        data.opportunities.add(opportunity(9, 1, "Larian Amal Sukarelawan", "E-Sukarelawan", "Membantu kaunter pendaftaran, kawalan laluan, dan stesen minuman peserta.", "2026-09-06", 40, "Stadium UiTM Shah Alam", "open", "Sports"));
        data.opportunities.add(opportunity(10, 8, "Kelas Bimbingan SPM Hujung Minggu", "E-Sukarelawan Outreach", "Membantu pelajar sekolah menengah dengan latihan Matematik dan Bahasa Inggeris.", "2026-09-13", 20, "Perpustakaan Komuniti Subang", "open", "Education"));

        data.applications.add(application(1, 2, 1, "2025-05-18", "approved", 1, "2025-05-18"));
        data.applications.add(application(2, 2, 2, "2025-05-20", "approved", 1, "2025-05-20"));
        data.applications.add(application(3, 2, 3, "2025-05-21", "pending", null, null));
        data.applications.add(application(4, 3, 5, "2026-07-12", "approved", 8, "2026-07-13"));
        data.applications.add(application(5, 3, 6, "2026-07-13", "pending", null, null));
        data.applications.add(application(6, 4, 5, "2026-07-14", "approved", 8, "2026-07-14"));
        data.applications.add(application(7, 4, 7, "2026-07-15", "pending", null, null));
        data.applications.add(application(8, 5, 8, "2026-07-16", "approved", 9, "2026-07-17"));
        data.applications.add(application(9, 5, 9, "2026-07-17", "approved", 1, "2026-07-18"));
        data.applications.add(application(10, 6, 6, "2026-07-18", "approved", 9, "2026-07-18"));
        data.applications.add(application(11, 6, 10, "2026-07-19", "pending", null, null));
        data.applications.add(application(12, 7, 8, "2026-07-20", "approved", 9, "2026-07-21"));

        data.hours.add(hour(1, 2, null, 1, "Program Pembersihan Sungai Langat", 84.5, "approved", "Attendance verified by programme coordinator", 1));
        data.hours.add(hour(2, 2, null, 2, "Kelas Tuisyen Komuniti", 36, "approved", "Teaching log completed", 1));
        data.hours.add(hour(3, 2, null, 3, "Sahabat Warga: Lawatan & Sumbangan", 2, "pending", "Reflection pending review", null));
        data.hours.add(hour(4, 3, null, 5, "Kempen Derma Darah Komuniti", 6, "approved", "Registration counter and donor flow completed", 8));
        data.hours.add(hour(5, 3, null, 6, "Gotong-Royong Taman Rekreasi", 4.5, "pending", "Awaiting NGO confirmation", null));
        data.hours.add(hour(6, 4, null, 5, "Kempen Derma Darah Komuniti", 5, "approved", "Helped manage donor waiting area", 8));
        data.hours.add(hour(7, 4, null, 7, "Bengkel Literasi Digital Warga Emas", 3, "pending", "Submitted reflection form", null));
        data.hours.add(hour(8, 5, null, 8, "Misi Bantuan Pek Makanan", 7.5, "approved", "Packed and distributed food aid", 9));
        data.hours.add(hour(9, 5, null, 9, "Larian Amal Sukarelawan", 6, "approved", "Route marshal duty completed", 1));
        data.hours.add(hour(10, 6, null, 6, "Gotong-Royong Taman Rekreasi", 5.5, "approved", "Recycling station and cleanup duty", 9));
        data.hours.add(hour(11, 6, null, 10, "Kelas Bimbingan SPM Hujung Minggu", 2, "pending", "Pending tutor attendance check", null));
        data.hours.add(hour(12, 7, null, 8, "Misi Bantuan Pek Makanan", 4, "approved", "Inventory and packing support", 9));
        data.hours.add(hour(13, 0, "Arif Hakimi", 3, "Sahabat Warga: Lawatan & Sumbangan", 5.5, "pending", "10 May 2025", null));
        data.hours.add(hour(14, 0, "Siti Hajar", 7, "Bengkel Literasi Digital Warga Emas", 3.5, "pending", "Manual entry by admin", null));

        data.feedback.add(feedback(1, 2, "Certificate request", "Can I get a certificate for the river cleanup programme?", "replied", "2026-07-01T09:30:00", 1, "Yes, the certificate will be available in your profile after final verification.", "2026-07-01T14:15:00"));
        data.feedback.add(feedback(2, 3, "Unable to edit profile picture", "My profile picture upload took a long time. Can admin check if it saved?", "replied", "2026-07-02T10:20:00", 1, "Your latest profile picture has been saved successfully.", "2026-07-02T12:10:00"));
        data.feedback.add(feedback(3, 4, "Opportunity location detail", "Please add the exact hall name for the blood donation campaign.", "open", "2026-07-03T11:05:00", null, null, null));
        data.feedback.add(feedback(4, 5, "Volunteer hours pending", "My food aid hours are still pending after the event.", "replied", "2026-07-04T15:45:00", 9, "The hours have been reviewed and approved. Thank you for volunteering.", "2026-07-04T17:30:00"));
        data.feedback.add(feedback(5, 6, "New programme suggestion", "Can we add a beach cleanup programme next month?", "open", "2026-07-05T08:50:00", null, null, null));
    }

    private User user(int id, String fullName, String email, String role, String referenceId, String ngoName) {
        User user = new User();
        user.id = id;
        user.fullName = fullName;
        user.email = email;
        user.passwordHash = "admin".equals(role)
                ? "ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f"
                : "5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5";
        user.role = role;
        user.referenceId = referenceId;
        user.ngoName = ngoName;
        return user;
    }

    private Opportunity opportunity(int id, int adminId, String event, String ngo, String description, String date, int seats, String location, String status, String category) {
        Opportunity opportunity = new Opportunity();
        opportunity.id = id;
        opportunity.adminId = adminId;
        opportunity.event = event;
        opportunity.ngo = ngo;
        opportunity.description = description;
        opportunity.date = date;
        opportunity.seats = seats;
        opportunity.location = location;
        opportunity.status = status;
        opportunity.category = category;
        return opportunity;
    }

    private Application application(int id, int studentId, int opportunityId, String applicationDate, String status, Integer adminId, String reviewedDate) {
        Application application = new Application();
        application.id = id;
        application.studentId = studentId;
        application.opportunityId = opportunityId;
        application.applicationDate = applicationDate;
        application.status = status;
        application.reviewedByAdminId = adminId;
        application.reviewedDate = reviewedDate;
        return application;
    }

    private HourRecord hour(int id, int studentId, String studentName, int opportunityId, String activity, double amount, String status, String note, Integer adminId) {
        HourRecord record = new HourRecord();
        record.id = id;
        record.studentId = studentId;
        record.studentName = studentName;
        record.opportunityId = opportunityId;
        record.activity = activity;
        record.amount = amount;
        record.status = status;
        record.note = note;
        record.approvedByAdminId = adminId;
        return record;
    }

    private Feedback feedback(int id, int studentId, String subject, String message, String status, String createdAt, Integer adminId, String adminReply, String repliedAt) {
        Feedback feedback = new Feedback();
        feedback.id = id;
        feedback.studentId = studentId;
        feedback.subject = subject;
        feedback.message = message;
        feedback.status = status;
        feedback.createdAt = createdAt;
        feedback.adminId = adminId;
        feedback.adminReply = adminReply;
        feedback.repliedAt = repliedAt;
        return feedback;
    }
}
