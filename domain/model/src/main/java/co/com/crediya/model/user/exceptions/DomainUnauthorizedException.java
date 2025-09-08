package co.com.crediya.model.user.exceptions;

public class DomainUnauthorizedException extends RuntimeException {
    public DomainUnauthorizedException(String message) { super(message); }
}
