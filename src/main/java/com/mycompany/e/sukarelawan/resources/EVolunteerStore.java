package com.mycompany.e.sukarelawan.resources;

import com.mycompany.e.sukarelawan.resources.ApiModels.AppState;
import com.mycompany.e.sukarelawan.resources.ApiModels.ApplicationRequest;
import com.mycompany.e.sukarelawan.resources.ApiModels.Feedback;
import com.mycompany.e.sukarelawan.resources.ApiModels.FeedbackReplyRequest;
import com.mycompany.e.sukarelawan.resources.ApiModels.FeedbackRequest;
import com.mycompany.e.sukarelawan.resources.ApiModels.HourRecord;
import com.mycompany.e.sukarelawan.resources.ApiModels.LoginRequest;
import com.mycompany.e.sukarelawan.resources.ApiModels.Opportunity;
import com.mycompany.e.sukarelawan.resources.ApiModels.ProfileRequest;
import com.mycompany.e.sukarelawan.resources.ApiModels.RegisterRequest;
import com.mycompany.e.sukarelawan.resources.ApiModels.User;

public interface EVolunteerStore {
    AppState state();
    User login(LoginRequest request);
    User register(RegisterRequest request);
    Opportunity createOpportunity(Opportunity opportunity);
    Opportunity updateOpportunity(int id, Opportunity opportunity);
    void deleteOpportunity(int id);
    void apply(ApplicationRequest request);
    HourRecord submitHours(HourRecord record);
    HourRecord reviewHours(int id, String action, int adminId);
    ApiModels.Application reviewApplication(int id, String action, int adminId);
    User updateProfile(int id, ProfileRequest request);
    Feedback submitFeedback(FeedbackRequest request);
    Feedback replyFeedback(int id, FeedbackReplyRequest request);
}
