package studi.doryanbessiere.jo2024.common;

public final class Routes {

    private Routes() {
    }

    public static final class Auth {
        public static final String BASE =   "/auth";

        public static final class Customer {
            public static final String BASE = Auth.BASE + "/customer";
            public static final String REGISTER = "/register";
            public static final String LOGIN = "/login";
            public static final String ME = "/me";
            public static final String ME_TICKETS = "/me/tickets";


            public static final String FORGOT_PASSWORD = "/forgot-password";
            public static final String RESET_PASSWORD = "/reset-password";
        }

        public static final class Admin {
            public static final String BASE = Auth.BASE + "/admin";
            public static final String LOGIN = "/login";
            public static final String ME = "/me";
        }
    }

    public static final class Tickets {
        public static final String BASE = "/tickets";
        public static final String SCAN = "/scan";
        public static final String VALIDATE = "/validate";
    }

    public static final class Payment {

        public static final String BASE = "/payments";
        public static final String CHECKOUT = "/checkout";
        public static final String STATUS = "/status/{session_id}";

    }

    public static final class Stripe {
        public static final String BASE = "/stripe";
        public static final String WEBHOOK = "/webhook";
    }

    public static final class Offer {
        public static final String BASE = "/offers";
    }
}
