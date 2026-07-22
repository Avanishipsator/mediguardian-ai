package com.mediguardian.notification.service;

import com.mediguardian.core.common.ErrorCodes;
import com.mediguardian.core.common.SecurityUtils;
import com.mediguardian.core.exception.BusinessException;
import com.mediguardian.notification.entity.Notification;
import com.mediguardian.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void createNotification(UUID accountId, String message) {
        if (accountId == null) return; // E.g., newborn profiles have no account
        Notification notification = Notification.builder()
                .accountId(accountId)
                .message(message)
                .build();
        notificationRepository.save(notification);
    }

    public List<Notification> getMyNotifications() {
        UUID accountId = SecurityUtils.getCurrentAccountId()
                .orElseThrow(() -> new BusinessException("User not authenticated", ErrorCodes.UNAUTHORIZED));

        return notificationRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }
}
