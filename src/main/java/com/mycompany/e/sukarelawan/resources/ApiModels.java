package com.mycompany.e.sukarelawan.resources;

import java.util.ArrayList;
import java.util.List;

public class ApiModels {
    public static class User {
        public int id;
        public String fullName;
        public String email;
        public String passwordHash;
        public String role;
        public String referenceId;
        public String ngoName;
        public Profile profile;
    }

    public static class Profile {
        public String phone;
        public String faculty;
        public String programme;
        public String bio;
        public String photo;
    }

    public static class Opportunity {
        public int id;
        public int adminId;
        public String event;
        public String ngo;
        public String description;
        public String date;
        public int seats;
        public String location;
        public String status;
        public String category;
    }

    public static class Application {
        public int id;
        public int studentId;
        public int opportunityId;
        public String applicationDate;
        public String status;
        public Integer reviewedByAdminId;
        public String reviewedDate;
    }

    public static class HourRecord {
        public int id;
        public int studentId;
        public String studentName;
        public int opportunityId;
        public String activity;
        public double amount;
        public String status;
        public String note;
        public Integer approvedByAdminId;
    }

    public static class Feedback {
        public int id;
        public int studentId;
        public String subject;
        public String message;
        public String status;
        public String createdAt;
        public Integer adminId;
        public String adminReply;
        public String repliedAt;
    }

    public static class AppState {
        public int version = 6;
        public List<User> users = new ArrayList<>();
        public List<Opportunity> opportunities = new ArrayList<>();
        public List<Application> applications = new ArrayList<>();
        public List<HourRecord> hours = new ArrayList<>();
        public List<Feedback> feedback = new ArrayList<>();
    }

    public static class LoginRequest {
        public String role;
        public String email;
        public String password;
    }

    public static class RegisterRequest {
        public String fullName;
        public String email;
        public String password;
        public String role;
        public String referenceId;
    }

    public static class ApplicationRequest {
        public int studentId;
        public int opportunityId;
    }

    public static class ReviewRequest {
        public int adminId;
    }

    public static class ProfileRequest {
        public String fullName;
        public String email;
        public String referenceId;
        public Profile profile;
    }

    public static class FeedbackRequest {
        public int studentId;
        public String subject;
        public String message;
    }

    public static class FeedbackReplyRequest {
        public int adminId;
        public String reply;
    }

    public static class ErrorMessage {
        public String message;

        public ErrorMessage() {
        }

        public ErrorMessage(String message) {
            this.message = message;
        }
    }
}
