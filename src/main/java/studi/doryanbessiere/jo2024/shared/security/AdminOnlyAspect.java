package studi.doryanbessiere.jo2024.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import studi.doryanbessiere.jo2024.common.exceptions.AccessDeniedException;
import studi.doryanbessiere.jo2024.services.admins.AdminAuthService;
import studi.doryanbessiere.jo2024.services.admins.dto.AdminMeResponse;
import studi.doryanbessiere.jo2024.common.exceptions.UnauthorizedException;

@Aspect
@Component
@RequiredArgsConstructor
public class AdminOnlyAspect {

    private final HttpServletRequest request;
    private final AdminAuthService adminAuthService;

    @Before("@annotation(AdminOnly)")
    public void verifyAdminAccess() {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new UnauthorizedException();
        }

        String token = header.substring(7);
        AdminMeResponse admin = adminAuthService.getAuthenticatedAdmin(token);

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            throw new AccessDeniedException();
        }
    }
}
