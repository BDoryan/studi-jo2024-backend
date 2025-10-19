package studi.doryanbessiere.jo2024.common.exceptions;

public class AccessDeniedException extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "access_denied";

    public AccessDeniedException() {
        super(DEFAULT_MESSAGE);
    }
}
