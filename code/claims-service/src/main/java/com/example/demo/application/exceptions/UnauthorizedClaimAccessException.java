package com.example.demo.application.exceptions;

import java.util.UUID;

public class UnauthorizedClaimAccessException extends RuntimeException {

    public UnauthorizedClaimAccessException(UUID requestingUserId, UUID claimId) {
        super("User " + requestingUserId + " is not authorized to access claim " + claimId);
    }
}
