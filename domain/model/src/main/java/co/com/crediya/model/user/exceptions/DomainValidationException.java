package co.com.crediya.model.user.exceptions;

public class DomainValidationException extends RuntimeException {
    public DomainValidationException(String code, String detail) {
        super(buildMessage(code, detail));
    }

    private static String buildMessage(String code, String detail) {
        if (code == null && detail == null) {
            return null;
        }
        if (code != null && detail != null) {
            return code + ": " + detail;
        }
        return code != null ? code : detail;
    }
}
