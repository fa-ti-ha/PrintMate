package com.printmate.order.state;

import com.printmate.order.OrderStatus;
import com.printmate.order.PrintOrder;

import java.time.LocalDateTime;

/**
 * State Pattern: Contract for handling order lifecycle state transitions.
 */
public interface OrderState {
    OrderStatus getStatus();

    void accept(PrintOrder order, String changedBy);

    void reject(PrintOrder order, String reason, String changedBy);

    void startPrinting(PrintOrder order, String changedBy);

    void markReadyForPickup(PrintOrder order, String pickupCodeHash, LocalDateTime expiresAt, String changedBy);

    void complete(PrintOrder order, String changedBy);

    void cancel(PrintOrder order, String reason, String changedBy);
}

