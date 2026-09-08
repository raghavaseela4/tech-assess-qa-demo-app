package com.example.demo.adapter.in.web;

import com.example.demo.application.auth.InvalidCredentialsException;
import com.example.demo.application.auth.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class AuthExceptionHandler {

    private final ErrorTypeConstants errorTypes;

    public AuthExceptionHandler(ErrorTypeConstants errorTypes) {
        this.errorTypes = errorTypes;
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleUserAlreadyExists(UserAlreadyExistsException exception) {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
        problem.setTitle("User Already Exists");
        problem.setType(errorTypes.userExists());
        problem.setProperty("email", exception.getEmail());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleInvalidCredentials(InvalidCredentialsException exception) {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password"
        );
        problem.setTitle("Authentication Failed");
        problem.setType(errorTypes.authFailed());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();

        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed"
        );
        problem.setTitle("Validation Error");
        problem.setType(errorTypes.validationError());
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }
}
