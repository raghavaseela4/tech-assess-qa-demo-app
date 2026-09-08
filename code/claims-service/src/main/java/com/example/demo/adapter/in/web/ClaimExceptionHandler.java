package com.example.demo.adapter.in.web;

import com.example.demo.application.exceptions.UnauthorizedClaimAccessException;
import com.example.demo.application.exceptions.UserNotFoundException;
import com.example.demo.domain.claim.ClaimNotFoundException;
import com.example.demo.domain.claim.InvalidClaimException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ClaimExceptionHandler {

    private final ErrorTypeConstants errorTypes;

    public ClaimExceptionHandler(ErrorTypeConstants errorTypes) {
        this.errorTypes = errorTypes;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle("Validation Error");
        problemDetail.setType(errorTypes.validationError());
        return problemDetail;
    }

    @ExceptionHandler(InvalidClaimException.class)
    public ProblemDetail handleInvalidClaim(InvalidClaimException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle("Validation Error");
        problemDetail.setType(errorTypes.validationError());
        return problemDetail;
    }

    @ExceptionHandler(ClaimNotFoundException.class)
    public ProblemDetail handleClaimNotFound(ClaimNotFoundException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("Not Found");
        problemDetail.setType(errorTypes.notFound());
        return problemDetail;
    }

    @ExceptionHandler(UnauthorizedClaimAccessException.class)
    public ProblemDetail handleUnauthorizedClaimAccess(UnauthorizedClaimAccessException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, exception.getMessage());
        problemDetail.setTitle("Forbidden");
        problemDetail.setType(errorTypes.forbidden());
        return problemDetail;
    }
}
