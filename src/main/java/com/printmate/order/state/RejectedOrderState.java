package com.printmate.order.state;

import com.printmate.order.OrderStatus;
import org.springframework.stereotype.Component;

@Component
public class RejectedOrderState extends AbstractOrderState {
    @Override
    public OrderStatus getStatus() {
        return OrderStatus.REJECTED;
    }
}

