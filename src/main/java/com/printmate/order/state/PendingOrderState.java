package com.printmate.order.state;

import com.printmate.order.OrderStatus;
import com.printmate.order.PrintOrder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PendingOrderState extends AbstractOrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PENDING;
    }

    @Override
    public void accept(PrintOrder order, String changedBy) {
        order.setOrderStatus(OrderStatus.ACCEPTED);
        order.setAcceptedAt(LocalDateTime.now());
    }

    @Override
    public void reject(PrintOrder order, String reason, String changedBy) {
        order.setOrderStatus(OrderStatus.REJECTED);
        order.setOperatorNotes(reason);
    }

    @Override
    public void cancel(PrintOrder order, String reason, String changedBy) {
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setOperatorNotes(reason);
    }
}

