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
        User admin = new User();
        admin.id = 1;
        admin.fullName = "UKM Volunteer Coordinator";
        admin.email = "admin@demo.my";
        admin.passwordHash = "ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f";
        admin.role = "admin";
        admin.referenceId = "UKM-ADMIN";
        admin.ngoName = "Universiti Kebangsaan Malaysia";
        data.users.add(admin);

        User student = new User();
        student.id = 2;
        student.fullName = "Aisyah Maisarah";
        student.email = "student@demo.my";
        student.passwordHash = "5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5";
        student.role = "student";
        student.referenceId = "A189337";
        data.users.add(student);

        data.opportunities.add(opportunity(1, "Program Pembersihan Sungai Langat", "UKM Sukarelawan", "Program komuniti membersihkan kawasan sungai bersama penduduk setempat.", "2025-05-24", 28, "Kajang, Selangor", "open", "Environment"));
        data.opportunities.add(opportunity(2, "Kelas Tuisyen Komuniti", "UKM Bakti Siswa", "Bantu pelajar sekolah rendah melalui kelas bimbingan hujung minggu.", "2025-05-31", 16, "UKM, Bangi", "open", "Education"));
        data.opportunities.add(opportunity(3, "Sahabat Warga: Lawatan & Sumbangan", "Kelab Kebajikan UKM", "Lawatan sokongan sosial dan penyerahan sumbangan ke pusat jagaan.", "2025-06-07", 10, "Pusat Jagaan Kasih Harmoni, Kajang", "limited", "Community"));
        data.opportunities.add(opportunity(4, "Dapur Komuniti Ramadan", "Sukarelawan Mahasiswa", "Menyediakan dan mengagihkan makanan kepada keluarga memerlukan.", "2025-06-14", 0, "Bangi", "closed", "Food Aid"));

        Application application = new Application();
        application.id = 1;
        application.studentId = 2;
        application.opportunityId = 1;
        application.applicationDate = "2025-05-18";
        application.status = "approved";
        data.applications.add(application);
        Application application2 = new Application();
        application2.id = 2;
        application2.studentId = 2;
        application2.opportunityId = 2;
        application2.applicationDate = "2025-05-20";
        application2.status = "approved";
        data.applications.add(application2);
        Application application3 = new Application();
        application3.id = 3;
        application3.studentId = 2;
        application3.opportunityId = 3;
        application3.applicationDate = "2025-05-21";
        application3.status = "pending";
        data.applications.add(application3);

        data.hours.add(hour(1, 2, null, 1, "Program Pembersihan Sungai Langat", 84.5, "approved", "Attendance verified by programme coordinator", 1));
        data.hours.add(hour(2, 2, null, 2, "Kelas Tuisyen Komuniti", 36, "approved", "Teaching log completed", 1));
        data.hours.add(hour(3, 2, null, 3, "Sahabat Warga: Lawatan & Sumbangan", 2, "pending", "Reflection pending review", null));
        data.hours.add(hour(4, 0, "Muhammad Danish", 1, "Program Pembersihan Sungai Langat", 6, "pending", "18 May 2025", null));
        data.hours.add(hour(5, 0, "Nur Adlina", 2, "Kelas Tuisyen Komuniti", 4, "pending", "11 May 2025", null));
        data.hours.add(hour(6, 0, "Arif Hakimi", 3, "Sahabat Warga: Lawatan & Sumbangan", 5.5, "pending", "10 May 2025", null));
        data.hours.add(hour(7, 0, "Farah Nazihah", 1, "Program Pembersihan Sungai Langat", 6, "approved", "4 May 2025", 1));
        data.hours.add(hour(8, 0, "Haqim Rashid", 2, "Kelas Tuisyen Komuniti", 3, "approved", "3 May 2025", 1));
    }

    private Opportunity opportunity(int id, String event, String ngo, String description, String date, int seats, String location, String status, String category) {
        Opportunity opportunity = new Opportunity();
        opportunity.id = id;
        opportunity.adminId = 1;
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
}
