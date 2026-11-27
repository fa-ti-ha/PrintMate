package com.printmate.order;

import com.printmate.common.exception.InvalidOperationException;

/** A failed verification must commit its attempt counter without recording payment. */
public class InvalidPickupCodeException extends InvalidOperationException {
    public InvalidPickupCodeException(String message) {
        super(message);
    }
}
