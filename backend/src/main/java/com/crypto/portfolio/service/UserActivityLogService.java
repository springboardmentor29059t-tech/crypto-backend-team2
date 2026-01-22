package com.crypto.portfolio.service;

import com.crypto.portfolio.model.User;
import com.crypto.portfolio.model.UserActivityLog;
import com.crypto.portfolio.repository.UserActivityLogRepository;
import org.springframework.stereotype.Service;

@Service
public class UserActivityLogService {
    private final UserActivityLogRepository activityLogRepository;

    public UserActivityLogService(UserActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public void logActivity(User user, String activity, String details) {
        UserActivityLog log = new UserActivityLog(user, activity, details);
        activityLogRepository.save(log);
    }
}
