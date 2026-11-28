package com.printmate.order.state;

import com.printmate.common.exception.InvalidOperationException;
import com.printmate.order.OrderStatus;
import com.printmate.order.PrintOrder;
import com.printmate.payment.PaymentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReadyForPickupOrderState extends AbstractOrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.READY_FOR_PICKUP;
    }

    @Override
    public void complete(PrintOrder order, String changedBy) {
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new InvalidOperationException("Cannot complete order before payment is recorded as PAID.");
        }
        order.setOrderStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
    }
}

