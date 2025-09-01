package co.com.crediya.model.user.exceptions;

public class DomainConflictException extends RuntimeException {
    public DomainConflictException(String code, String detail) { super(code + ":" + detail); }
}
