package com.printmate.order.state;

import com.printmate.common.exception.InvalidOperationException;
import com.printmate.order.PrintOrder;

import java.time.LocalDateTime;

public abstract class AbstractOrderState implements OrderState {

    @Override
    public void accept(PrintOrder order, String changedBy) {
        throw new InvalidOperationException("Cannot accept order in status: " + getStatus());
    }

    @Override
    public void reject(PrintOrder order, String reason, String changedBy) {
        throw new InvalidOperationException("Cannot reject order in status: " + getStatus());
    }

    @Override
    public void startPrinting(PrintOrder order, String changedBy) {
        throw new InvalidOperationException("Cannot start printing order in status: " + getStatus());
    }

    @Override
    public void markReadyForPickup(PrintOrder order, String pickupCodeHash, LocalDateTime expiresAt, String changedBy) {
        throw new InvalidOperationException("Cannot mark ready for pickup for order in status: " + getStatus());
    }

    @Override
    public void complete(PrintOrder order, String changedBy) {
        throw new InvalidOperationException("Cannot complete order in status: " + getStatus());
    }

    @Override
    public void cancel(PrintOrder order, String reason, String changedBy) {
        throw new InvalidOperationException("Cannot cancel order in status: " + getStatus());
    }
}

