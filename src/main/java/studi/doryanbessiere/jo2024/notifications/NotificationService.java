package studi.doryanbessiere.jo2024.notifications;

import org.springframework.stereotype.Service;

@Service
public interface NotificationService<T> {
    void sendNotification(T request);
}
