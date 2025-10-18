package studi.doryanbessiere.jo2024.notifications;

public interface NotificationService<T> {
    void sendNotification(T request);
}
