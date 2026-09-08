package com.example.demo.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SecurityExceptionHandler {

    private final ErrorTypeConstants errorTypes;

    public SecurityExceptionHandler(ErrorTypeConstants errorTypes) {
        this.errorTypes = errorTypes;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException exception) {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                exception.getMessage()
        );
        problem.setTitle("Access Denied");
        problem.setType(errorTypes.accessDenied());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationFailure(AuthenticationException exception) {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Authentication required"
        );
        problem.setTitle("Unauthorized");
        problem.setType(errorTypes.unauthorized());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }
}
