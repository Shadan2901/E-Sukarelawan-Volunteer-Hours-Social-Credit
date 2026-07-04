package com.mycompany.e.sukarelawan.resources;

import com.mycompany.e.sukarelawan.resources.ApiModels.AppState;
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
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "ESukarelawanApiServlet", urlPatterns = "/resources/api/*")
public class ESukarelawanApiServlet extends HttpServlet {
    private static final EVolunteerStore STORE = EVolunteerStoreFactory.create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if ("/state".equals(path(request))) {
            ok(response, stateJson(STORE.state()));
            return;
        }
        notFound(response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handle(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handle(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handle(request, response);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String path = path(request);
            String method = request.getMethod();
            String body = body(request);

            if ("POST".equals(method) && "/auth/login".equals(path)) {
                LoginRequest login = new LoginRequest();
                login.role = text(body, "role");
                login.email = text(body, "email");
                login.password = text(body, "password");
                ok(response, userJson(STORE.login(login)));
                return;
            }

            if ("POST".equals(method) && "/auth/register".equals(path)) {
                RegisterRequest register = new RegisterRequest();
                register.fullName = text(body, "fullName");
                register.email = text(body, "email");
                register.password = text(body, "password");
                register.role = text(body, "role");
                register.referenceId = text(body, "referenceId");
                ok(response, userJson(STORE.register(register)));
                return;
            }

            if ("POST".equals(method) && "/opportunities".equals(path)) {
                ok(response, opportunityJson(STORE.createOpportunity(opportunity(body))));
                return;
            }

            Matcher opportunityUpdate = Pattern.compile("^/opportunities/(\\d+)$").matcher(path);
            if ("PUT".equals(method) && opportunityUpdate.matches()) {
                ok(response, opportunityJson(STORE.updateOpportunity(Integer.parseInt(opportunityUpdate.group(1)), opportunity(body))));
                return;
            }

            Matcher opportunityDelete = Pattern.compile("^/opportunities/(\\d+)$").matcher(path);
            if ("DELETE".equals(method) && opportunityDelete.matches()) {
                STORE.deleteOpportunity(Integer.parseInt(opportunityDelete.group(1)));
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }

            if ("POST".equals(method) && "/applications".equals(path)) {
                ApplicationRequest apply = new ApplicationRequest();
                apply.studentId = number(body, "studentId");
                apply.opportunityId = number(body, "opportunityId");
                STORE.apply(apply);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }

            if ("POST".equals(method) && "/hours".equals(path)) {
                HourRecord record = new HourRecord();
                record.studentId = number(body, "studentId");
                record.opportunityId = number(body, "opportunityId");
                record.activity = text(body, "activity");
                record.amount = decimal(body, "amount");
                record.note = text(body, "note");
                ok(response, hourJson(STORE.submitHours(record)));
                return;
            }

            Matcher hoursReview = Pattern.compile("^/hours/(\\d+)/(approve|reject)$").matcher(path);
            if ("POST".equals(method) && hoursReview.matches()) {
                ok(response, hourJson(STORE.reviewHours(Integer.parseInt(hoursReview.group(1)), hoursReview.group(2), number(body, "adminId"))));
                return;
            }

            Matcher applicationReview = Pattern.compile("^/applications/(\\d+)/(approve|reject)$").matcher(path);
            if ("POST".equals(method) && applicationReview.matches()) {
                ok(response, applicationJson(STORE.reviewApplication(Integer.parseInt(applicationReview.group(1)), applicationReview.group(2), number(body, "adminId"))));
                return;
            }

            Matcher profileUpdate = Pattern.compile("^/users/(\\d+)/profile$").matcher(path);
            if ("PUT".equals(method) && profileUpdate.matches()) {
                ProfileRequest profileRequest = new ProfileRequest();
                profileRequest.fullName = text(body, "fullName");
                profileRequest.email = text(body, "email");
                profileRequest.referenceId = text(body, "referenceId");
                profileRequest.profile = profile(body);
                ok(response, userJson(STORE.updateProfile(Integer.parseInt(profileUpdate.group(1)), profileRequest)));
                return;
            }

            if ("POST".equals(method) && "/feedback".equals(path)) {
                FeedbackRequest feedback = new FeedbackRequest();
                feedback.studentId = number(body, "studentId");
                feedback.subject = text(body, "subject");
                feedback.message = text(body, "message");
                ok(response, feedbackJson(STORE.submitFeedback(feedback)));
                return;
            }

            Matcher feedbackReply = Pattern.compile("^/feedback/(\\d+)/reply$").matcher(path);
            if ("POST".equals(method) && feedbackReply.matches()) {
                FeedbackReplyRequest reply = new FeedbackReplyRequest();
                reply.adminId = number(body, "adminId");
                reply.reply = text(body, "reply");
                ok(response, feedbackJson(STORE.replyFeedback(Integer.parseInt(feedbackReply.group(1)), reply)));
                return;
            }

            notFound(response);
        } catch (IllegalArgumentException exception) {
            error(response, HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (RuntimeException exception) {
            error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    private static String path(HttpServletRequest request) {
        String info = request.getPathInfo();
        return info == null || info.isBlank() ? "/" : info;
    }

    private static String body(HttpServletRequest request) throws IOException {
        return new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static Opportunity opportunity(String json) {
        Opportunity opportunity = new Opportunity();
        opportunity.id = number(json, "id");
        opportunity.adminId = number(json, "adminId");
        opportunity.event = text(json, "event");
        opportunity.ngo = text(json, "ngo");
        opportunity.description = text(json, "description");
        opportunity.date = text(json, "date");
        opportunity.seats = number(json, "seats");
        opportunity.location = text(json, "location");
        opportunity.status = text(json, "status");
        opportunity.category = text(json, "category");
        return opportunity;
    }

    private static Profile profile(String json) {
        String profileJson = object(json, "profile");
        Profile profile = new Profile();
        profile.phone = nullableText(profileJson, "phone");
        profile.faculty = nullableText(profileJson, "faculty");
        profile.programme = nullableText(profileJson, "programme");
        profile.bio = nullableText(profileJson, "bio");
        profile.photo = nullableText(profileJson, "photo");
        return profile;
    }

    private static void ok(HttpServletResponse response, String json) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(json);
    }

    private static void error(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + escape(message) + "\"}");
    }

    private static void notFound(HttpServletResponse response) throws IOException {
        error(response, HttpServletResponse.SC_NOT_FOUND, "API endpoint was not found.");
    }

    private static String stateJson(AppState state) {
        StringBuilder json = new StringBuilder();
        json.append("{\"version\":").append(state.version);
        json.append(",\"users\":[");
        for (int i = 0; i < state.users.size(); i++) {
            if (i > 0) json.append(",");
            json.append(userJson(state.users.get(i)));
        }
        json.append("],\"opportunities\":[");
        for (int i = 0; i < state.opportunities.size(); i++) {
            if (i > 0) json.append(",");
            json.append(opportunityJson(state.opportunities.get(i)));
        }
        json.append("],\"applications\":[");
        for (int i = 0; i < state.applications.size(); i++) {
            if (i > 0) json.append(",");
            json.append(applicationJson(state.applications.get(i)));
        }
        json.append("],\"hours\":[");
        for (int i = 0; i < state.hours.size(); i++) {
            if (i > 0) json.append(",");
            json.append(hourJson(state.hours.get(i)));
        }
        json.append("],\"feedback\":[");
        for (int i = 0; i < state.feedback.size(); i++) {
            if (i > 0) json.append(",");
            json.append(feedbackJson(state.feedback.get(i)));
        }
        json.append("]}");
        return json.toString();
    }

    private static String userJson(User user) {
        return "{"
                + "\"id\":" + user.id
                + ",\"fullName\":\"" + escape(user.fullName) + "\""
                + ",\"email\":\"" + escape(user.email) + "\""
                + ",\"passwordHash\":\"" + escape(user.passwordHash) + "\""
                + ",\"role\":\"" + escape(user.role) + "\""
                + ",\"referenceId\":\"" + escape(user.referenceId) + "\""
                + ",\"ngoName\":" + nullable(user.ngoName)
                + ",\"profile\":" + profileJson(user.profile)
                + "}";
    }

    private static String profileJson(Profile profile) {
        if (profile == null) return "{}";
        return "{"
                + "\"phone\":" + nullable(profile.phone)
                + ",\"faculty\":" + nullable(profile.faculty)
                + ",\"programme\":" + nullable(profile.programme)
                + ",\"bio\":" + nullable(profile.bio)
                + ",\"photo\":" + nullable(profile.photo)
                + "}";
    }

    private static String opportunityJson(Opportunity opportunity) {
        return "{"
                + "\"id\":" + opportunity.id
                + ",\"adminId\":" + opportunity.adminId
                + ",\"event\":\"" + escape(opportunity.event) + "\""
                + ",\"ngo\":\"" + escape(opportunity.ngo) + "\""
                + ",\"description\":\"" + escape(opportunity.description) + "\""
                + ",\"date\":\"" + escape(opportunity.date) + "\""
                + ",\"seats\":" + opportunity.seats
                + ",\"location\":\"" + escape(opportunity.location) + "\""
                + ",\"status\":\"" + escape(opportunity.status) + "\""
                + ",\"category\":\"" + escape(opportunity.category) + "\""
                + "}";
    }

    private static String applicationJson(ApiModels.Application application) {
        return "{"
                + "\"id\":" + application.id
                + ",\"studentId\":" + application.studentId
                + ",\"opportunityId\":" + application.opportunityId
                + ",\"applicationDate\":\"" + escape(application.applicationDate) + "\""
                + ",\"status\":\"" + escape(application.status) + "\""
                + ",\"reviewedByAdminId\":" + (application.reviewedByAdminId == null ? "null" : application.reviewedByAdminId)
                + ",\"reviewedDate\":" + nullable(application.reviewedDate)
                + "}";
    }

    private static String hourJson(HourRecord record) {
        return "{"
                + "\"id\":" + record.id
                + ",\"studentId\":" + record.studentId
                + ",\"studentName\":" + nullable(record.studentName)
                + ",\"opportunityId\":" + record.opportunityId
                + ",\"activity\":\"" + escape(record.activity) + "\""
                + ",\"amount\":" + record.amount
                + ",\"status\":\"" + escape(record.status) + "\""
                + ",\"note\":" + nullable(record.note)
                + ",\"approvedByAdminId\":" + (record.approvedByAdminId == null ? "null" : record.approvedByAdminId)
                + "}";
    }

    private static String feedbackJson(Feedback feedback) {
        return "{"
                + "\"id\":" + feedback.id
                + ",\"studentId\":" + feedback.studentId
                + ",\"subject\":\"" + escape(feedback.subject) + "\""
                + ",\"message\":\"" + escape(feedback.message) + "\""
                + ",\"status\":\"" + escape(feedback.status) + "\""
                + ",\"createdAt\":" + nullable(feedback.createdAt)
                + ",\"adminId\":" + (feedback.adminId == null ? "null" : feedback.adminId)
                + ",\"adminReply\":" + nullable(feedback.adminReply)
                + ",\"repliedAt\":" + nullable(feedback.repliedAt)
                + "}";
    }

    private static String nullable(String value) {
        return value == null ? "null" : "\"" + escape(value) + "\"";
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private static String text(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(json == null ? "" : json);
        return matcher.find() ? unescape(matcher.group(1)) : "";
    }

    private static String nullableText(String json, String key) {
        Matcher nullMatcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*null\\b").matcher(json == null ? "" : json);
        if (nullMatcher.find()) return null;
        String value = text(json, key);
        return value == null || value.isBlank() ? null : value;
    }

    private static int number(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)").matcher(json == null ? "" : json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private static double decimal(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)").matcher(json == null ? "" : json);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : 0;
    }

    private static String object(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\\{(.*)}\\s*}?", Pattern.DOTALL).matcher(json == null ? "" : json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String unescape(String value) {
        return value.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }
}
