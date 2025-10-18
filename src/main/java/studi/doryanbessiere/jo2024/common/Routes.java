package studi.doryanbessiere.jo2024.common;

public final class Routes {

    private Routes() {
    }

    public static final String API_BASE = "/api";

    public static final class Auth {
        public static final String BASE = API_BASE + "/auth";

        public static final class Customer {
            public static final String BASE = Auth.BASE + "/customer";
            public static final String REGISTER = "/register";
            public static final String LOGIN = "/login";
            public static final String FORGOT_PASSWORD = "/forgot-password";
            public static final String RESET_PASSWORD = "/reset-password";
        }

        public static final class Admin {
            public static final String BASE = Auth.BASE + "/admin";
            public static final String LOGIN = "/login";
        }
    }
}
