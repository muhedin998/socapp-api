package com.example.keklock.notification.port;

import com.example.keklock.notification.domain.Notification;

public interface NotificationDeliveryPort {
    
    void sendRealTimeNotification(Notification notification);
    
    void sendPushNotification(Notification notification);
    
    void sendEmailNotification(Notification notification);
}