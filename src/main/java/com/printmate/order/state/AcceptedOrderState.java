package com.printmate.order.state;

import com.printmate.order.OrderStatus;
import com.printmate.order.PrintOrder;
import org.springframework.stereotype.Component;

@Component
public class AcceptedOrderState extends AbstractOrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.ACCEPTED;
    }

    @Override
    public void startPrinting(PrintOrder order, String changedBy) {
        order.setOrderStatus(OrderStatus.PRINTING);
    }
}

