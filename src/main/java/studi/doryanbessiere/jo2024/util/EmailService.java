package studi.doryanbessiere.jo2024.util;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
    public void sendPasswordResetLink(String to, String link) {
        System.out.println("=== Password Reset Email Mock ===");
        System.out.println("To: " + to);
        System.out.println("Link: " + link);
        System.out.println("=================================");
    }
}
