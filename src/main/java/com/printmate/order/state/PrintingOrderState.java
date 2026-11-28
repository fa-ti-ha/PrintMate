package com.printmate.order.state;

import com.printmate.order.OrderStatus;
import com.printmate.order.PrintOrder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PrintingOrderState extends AbstractOrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PRINTING;
    }

    @Override
    public void markReadyForPickup(PrintOrder order, String pickupCodeHash, LocalDateTime expiresAt, String changedBy) {
        order.setOrderStatus(OrderStatus.READY_FOR_PICKUP);
        order.setPickupCodeHash(pickupCodeHash);
        order.setPickupCodeExpiresAt(expiresAt);
        order.setPickupAttempts(0);
    }
}

