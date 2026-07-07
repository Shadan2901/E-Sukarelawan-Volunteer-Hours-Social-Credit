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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalDate;

public class JdbcEVolunteerStore implements EVolunteerStore {
    private final String url;
    private final String user;
    private final String password;

    public JdbcEVolunteerStore(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        try (Connection connection = connection()) {
            if (!connection.isValid(3)) {
                throw new IllegalStateException("Database connection is not valid.");
            }
            ensureProfileColumns(connection);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not connect to database.", exception);
        }
    }

    private void ensureProfileColumns(Connection connection) throws Exception {
        addColumnIfMissing(connection, "profile_phone", "VARCHAR(40)");
        addColumnIfMissing(connection, "profile_faculty", "VARCHAR(140)");
        addColumnIfMissing(connection, "profile_programme", "VARCHAR(140)");
        addColumnIfMissing(connection, "profile_bio", "TEXT");
        addColumnIfMissing(connection, "profile_photo", "LONGTEXT");
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE users MODIFY profile_photo LONGTEXT");
        }
    }

    private void addColumnIfMissing(Connection connection, String column, String definition) throws Exception {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet columns = metadata.getColumns(connection.getCatalog(), null, "users", column)) {
            if (columns.next()) return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE users ADD COLUMN " + column + " " + definition);
        }
    }

    @Override
    public AppState state() {
        AppState state = new AppState();
        try (Connection connection = connection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT id, full_name, email, password_hash, role, reference_id, ngo_name, profile_phone, profile_faculty, profile_programme, profile_bio, profile_photo FROM users ORDER BY id");
                 ResultSet rs = statement.executeQuery()) {
                while (rs.next()) state.users.add(user(rs));
            }
            try (PreparedStatement statement = connection.prepareStatement("SELECT id, admin_id, event, ngo, description, event_date, seats, location, status, category FROM opportunities ORDER BY id");
                 ResultSet rs = statement.executeQuery()) {
                while (rs.next()) state.opportunities.add(opportunity(rs));
            }
            try (PreparedStatement statement = connection.prepareStatement("SELECT id, student_id, opportunity_id, application_date, status, reviewed_by_admin_id, reviewed_date FROM applications ORDER BY id");
                 ResultSet rs = statement.executeQuery()) {
                while (rs.next()) state.applications.add(application(rs));
            }
            try (PreparedStatement statement = connection.prepareStatement("SELECT id, student_id, student_name, opportunity_id, activity, amount, status, note, approved_by_admin_id FROM volunteer_hours ORDER BY id DESC");
                 ResultSet rs = statement.executeQuery()) {
                while (rs.next()) state.hours.add(hour(rs));
            }
            try (PreparedStatement statement = connection.prepareStatement("SELECT id, student_id, subject, message, status, created_at, admin_id, admin_reply, replied_at FROM feedback ORDER BY created_at DESC, id DESC");
                 ResultSet rs = statement.executeQuery()) {
                while (rs.next()) state.feedback.add(feedback(rs));
            }
            return state;
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load database state.", exception);
        }
    }

    @Override
    public User login(LoginRequest request) {
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, full_name, email, password_hash, role, reference_id, ngo_name, profile_phone, profile_faculty, profile_programme, profile_bio, profile_photo FROM users WHERE LOWER(email) = LOWER(?) AND role = ?")) {
            statement.setString(1, request.email);
            statement.setString(2, request.role);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    User found = user(rs);
                    if (found.passwordHash.equals(sha256(request.password))) {
                        return found;
                    }
                }
            }
            throw new IllegalArgumentException("Email, password, or role is incorrect.");
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Could not login with database.", exception);
        }
    }

    @Override
    public User register(RegisterRequest request) {
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO users (full_name, email, password_hash, role, reference_id, ngo_name) VALUES (?, ?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, request.fullName);
            statement.setString(2, request.email.toLowerCase());
            statement.setString(3, sha256(request.password));
            statement.setString(4, request.role);
            statement.setString(5, request.referenceId);
            statement.setString(6, "admin".equals(request.role) ? request.fullName : null);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                User created = new User();
                created.id = keys.getInt(1);
                created.fullName = request.fullName;
                created.email = request.email.toLowerCase();
                created.passwordHash = sha256(request.password);
                created.role = request.role;
                created.referenceId = request.referenceId;
                created.ngoName = "admin".equals(request.role) ? request.fullName : null;
                return created;
            }
        } catch (Exception exception) {
            if (exception.getMessage() != null && exception.getMessage().toLowerCase().contains("duplicate")) {
                throw new IllegalArgumentException("An account with this email already exists.");
            }
            throw new IllegalStateException("Could not register account in database.", exception);
        }
    }

    @Override
    public Opportunity createOpportunity(Opportunity opportunity) {
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO opportunities (admin_id, event, ngo, description, event_date, seats, location, status, category) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            bindOpportunity(statement, opportunity, false);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                opportunity.id = keys.getInt(1);
                return opportunity;
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create opportunity in database.", exception);
        }
    }

    @Override
    public Opportunity updateOpportunity(int id, Opportunity opportunity) {
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE opportunities SET admin_id=?, event=?, ngo=?, description=?, event_date=?, seats=?, location=?, status=?, category=? WHERE id=?")) {
            bindOpportunity(statement, opportunity, false);
            statement.setInt(10, id);
            if (statement.executeUpdate() == 0) throw new IllegalArgumentException("Opportunity was not found.");
            opportunity.id = id;
            return opportunity;
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Could not update opportunity in database.", exception);
        }
    }

    @Override
    public void deleteOpportunity(int id) {
        try (Connection connection = connection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM applications WHERE opportunity_id=?")) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM opportunities WHERE id=?")) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Could not delete opportunity from database.", exception);
        }
    }

    @Override
    public void apply(ApplicationRequest request) {
        try (Connection connection = connection()) {
            try (PreparedStatement check = connection.prepareStatement("SELECT status, seats FROM opportunities WHERE id=?")) {
                check.setInt(1, request.opportunityId);
                try (ResultSet rs = check.executeQuery()) {
                    if (!rs.next()) throw new IllegalArgumentException("Opportunity was not found.");
                    if ("closed".equals(rs.getString("status"))) throw new IllegalArgumentException("This opportunity is already closed.");
                }
            }
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO applications (student_id, opportunity_id, application_date, status) VALUES (?, ?, ?, ?)")) {
                statement.setInt(1, request.studentId);
                statement.setInt(2, request.opportunityId);
                statement.setDate(3, Date.valueOf(LocalDate.now()));
                statement.setString(4, "pending");
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement("UPDATE opportunities SET seats = GREATEST(seats - 1, 0), status = CASE WHEN seats <= 1 THEN 'closed' ELSE status END WHERE id=?")) {
                statement.setInt(1, request.opportunityId);
                statement.executeUpdate();
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            if (exception.getMessage() != null && exception.getMessage().toLowerCase().contains("duplicate")) {
                throw new IllegalArgumentException("You have already applied for this opportunity.");
            }
            throw new IllegalStateException("Could not save application in database.", exception);
        }
    }

    @Override
    public HourRecord submitHours(HourRecord record) {
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO volunteer_hours (student_id, student_name, opportunity_id, activity, amount, status, note, approved_by_admin_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            bindHour(statement, record);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                record.id = keys.getInt(1);
                record.status = "pending";
                return record;
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Could not submit hours to database.", exception);
        }
    }

    @Override
    public HourRecord reviewHours(int id, String action, int adminId) {
        String status = "approve".equals(action) ? "approved" : "rejected";
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement("UPDATE volunteer_hours SET status=?, approved_by_admin_id=? WHERE id=?")) {
            statement.setString(1, status);
            statement.setInt(2, adminId);
            statement.setInt(3, id);
            if (statement.executeUpdate() == 0) throw new IllegalArgumentException("Hour record was not found.");
            try (PreparedStatement find = connection.prepareStatement("SELECT id, student_id, student_name, opportunity_id, activity, amount, status, note, approved_by_admin_id FROM volunteer_hours WHERE id=?")) {
                find.setInt(1, id);
                try (ResultSet rs = find.executeQuery()) {
                    rs.next();
                    return hour(rs);
                }
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Could not review hours in database.", exception);
        }
    }

    @Override
    public Application reviewApplication(int id, String action, int adminId) {
        String status = "approve".equals(action) ? "approved" : "rejected";
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement("UPDATE applications SET status=?, reviewed_by_admin_id=?, reviewed_date=? WHERE id=?")) {
            statement.setString(1, status);
            statement.setInt(2, adminId);
            statement.setDate(3, Date.valueOf(LocalDate.now()));
            statement.setInt(4, id);
            if (statement.executeUpdate() == 0) throw new IllegalArgumentException("Application was not found.");
            try (PreparedStatement find = connection.prepareStatement("SELECT id, student_id, opportunity_id, application_date, status, reviewed_by_admin_id, reviewed_date FROM applications WHERE id=?")) {
                find.setInt(1, id);
                try (ResultSet rs = find.executeQuery()) {
                    rs.next();
                    return application(rs);
                }
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Could not review application in database.", exception);
        }
    }

    @Override
    public User updateProfile(int id, ProfileRequest request) {
        Profile profile = request.profile == null ? new Profile() : request.profile;
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE users SET full_name=?, email=?, reference_id=?, ngo_name=CASE WHEN role='admin' THEN ? ELSE ngo_name END, profile_phone=?, profile_faculty=?, profile_programme=?, profile_bio=?, profile_photo=? WHERE id=?")) {
            statement.setString(1, request.fullName);
            statement.setString(2, request.email == null ? "" : request.email.toLowerCase());
            statement.setString(3, request.referenceId);
            statement.setString(4, request.fullName);
            statement.setString(5, profile.phone);
            statement.setString(6, profile.faculty);
            statement.setString(7, profile.programme);
            statement.setString(8, profile.bio);
            statement.setString(9, profile.photo);
            statement.setInt(10, id);
            if (statement.executeUpdate() == 0) throw new IllegalArgumentException("User was not found.");
            try (PreparedStatement find = connection.prepareStatement("SELECT id, full_name, email, password_hash, role, reference_id, ngo_name, profile_phone, profile_faculty, profile_programme, profile_bio, profile_photo FROM users WHERE id=?")) {
                find.setInt(1, id);
                try (ResultSet rs = find.executeQuery()) {
                    rs.next();
                    return user(rs);
                }
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            if (exception.getMessage() != null && exception.getMessage().toLowerCase().contains("duplicate")) {
                throw new IllegalArgumentException("An account with this email already exists.");
            }
            throw new IllegalStateException("Could not update profile in database.", exception);
        }
    }

    @Override
    public Feedback submitFeedback(FeedbackRequest request) {
        if (request.subject == null || request.subject.isBlank()) {
            throw new IllegalArgumentException("Feedback subject is required.");
        }
        if (request.message == null || request.message.isBlank()) {
            throw new IllegalArgumentException("Feedback message is required.");
        }
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO feedback (student_id, subject, message, status, created_at) VALUES (?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, request.studentId);
            statement.setString(2, request.subject.trim());
            statement.setString(3, request.message.trim());
            statement.setString(4, "open");
            statement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return findFeedback(connection, keys.getInt(1));
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Could not save feedback in database.", exception);
        }
    }

    @Override
    public Feedback replyFeedback(int id, FeedbackReplyRequest request) {
        if (request.reply == null || request.reply.isBlank()) {
            throw new IllegalArgumentException("Reply message is required.");
        }
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE feedback SET admin_id=?, admin_reply=?, replied_at=?, status=? WHERE id=?")) {
            statement.setInt(1, request.adminId);
            statement.setString(2, request.reply.trim());
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(4, "replied");
            statement.setInt(5, id);
            if (statement.executeUpdate() == 0) throw new IllegalArgumentException("Feedback was not found.");
            return findFeedback(connection, id);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Could not reply to feedback in database.", exception);
        }
    }

    private Feedback findFeedback(Connection connection, int id) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id, student_id, subject, message, status, created_at, admin_id, admin_reply, replied_at FROM feedback WHERE id=?")) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Feedback was not found.");
                return feedback(rs);
            }
        }
    }

    private Connection connection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, user, password);
    }

    private static void bindOpportunity(PreparedStatement statement, Opportunity opportunity, boolean includeId) throws Exception {
        statement.setInt(1, opportunity.adminId);
        statement.setString(2, opportunity.event);
        statement.setString(3, opportunity.ngo);
        statement.setString(4, opportunity.description);
        statement.setDate(5, Date.valueOf(opportunity.date));
        statement.setInt(6, opportunity.seats);
        statement.setString(7, opportunity.location);
        statement.setString(8, opportunity.status);
        statement.setString(9, opportunity.category);
        if (includeId) statement.setInt(10, opportunity.id);
    }

    private static void bindHour(PreparedStatement statement, HourRecord record) throws Exception {
        if (record.studentId == 0) statement.setNull(1, java.sql.Types.INTEGER);
        else statement.setInt(1, record.studentId);
        statement.setString(2, record.studentName);
        statement.setInt(3, record.opportunityId);
        statement.setString(4, record.activity);
        statement.setDouble(5, record.amount);
        statement.setString(6, "pending");
        statement.setString(7, record.note);
        if (record.approvedByAdminId == null) statement.setNull(8, java.sql.Types.INTEGER);
        else statement.setInt(8, record.approvedByAdminId);
    }

    private static User user(ResultSet rs) throws Exception {
        User user = new User();
        user.id = rs.getInt("id");
        user.fullName = rs.getString("full_name");
        user.email = rs.getString("email");
        user.passwordHash = rs.getString("password_hash");
        user.role = rs.getString("role");
        user.referenceId = rs.getString("reference_id");
        user.ngoName = rs.getString("ngo_name");
        Profile profile = new Profile();
        profile.phone = rs.getString("profile_phone");
        profile.faculty = rs.getString("profile_faculty");
        profile.programme = rs.getString("profile_programme");
        profile.bio = rs.getString("profile_bio");
        profile.photo = rs.getString("profile_photo");
        user.profile = profile;
        return user;
    }

    private static Opportunity opportunity(ResultSet rs) throws Exception {
        Opportunity opportunity = new Opportunity();
        opportunity.id = rs.getInt("id");
        opportunity.adminId = rs.getInt("admin_id");
        opportunity.event = rs.getString("event");
        opportunity.ngo = rs.getString("ngo");
        opportunity.description = rs.getString("description");
        opportunity.date = rs.getDate("event_date").toString();
        opportunity.seats = rs.getInt("seats");
        opportunity.location = rs.getString("location");
        opportunity.status = rs.getString("status");
        opportunity.category = rs.getString("category");
        return opportunity;
    }

    private static Application application(ResultSet rs) throws Exception {
        Application application = new Application();
        application.id = rs.getInt("id");
        application.studentId = rs.getInt("student_id");
        application.opportunityId = rs.getInt("opportunity_id");
        application.applicationDate = rs.getDate("application_date").toString();
        application.status = rs.getString("status");
        int adminId = rs.getInt("reviewed_by_admin_id");
        application.reviewedByAdminId = rs.wasNull() ? null : adminId;
        Date reviewedDate = rs.getDate("reviewed_date");
        application.reviewedDate = reviewedDate == null ? null : reviewedDate.toString();
        return application;
    }

    private static HourRecord hour(ResultSet rs) throws Exception {
        HourRecord record = new HourRecord();
        record.id = rs.getInt("id");
        record.studentId = rs.getInt("student_id");
        record.studentName = rs.getString("student_name");
        record.opportunityId = rs.getInt("opportunity_id");
        record.activity = rs.getString("activity");
        record.amount = rs.getDouble("amount");
        record.status = rs.getString("status");
        record.note = rs.getString("note");
        int adminId = rs.getInt("approved_by_admin_id");
        record.approvedByAdminId = rs.wasNull() ? null : adminId;
        return record;
    }

    private static Feedback feedback(ResultSet rs) throws Exception {
        Feedback feedback = new Feedback();
        feedback.id = rs.getInt("id");
        feedback.studentId = rs.getInt("student_id");
        feedback.subject = rs.getString("subject");
        feedback.message = rs.getString("message");
        feedback.status = rs.getString("status");
        Timestamp createdAt = rs.getTimestamp("created_at");
        feedback.createdAt = createdAt == null ? null : createdAt.toLocalDateTime().toString();
        int adminId = rs.getInt("admin_id");
        feedback.adminId = rs.wasNull() ? null : adminId;
        feedback.adminReply = rs.getString("admin_reply");
        Timestamp repliedAt = rs.getTimestamp("replied_at");
        feedback.repliedAt = repliedAt == null ? null : repliedAt.toLocalDateTime().toString();
        return feedback;
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : hash) builder.append(String.format("%02x", item));
            return builder.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Password hashing is unavailable.", exception);
        }
    }
}
