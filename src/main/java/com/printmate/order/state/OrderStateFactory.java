package com.printmate.order.state;

import com.printmate.order.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderStateFactory {

    private final Map<OrderStatus, OrderState> stateMap = new EnumMap<>(OrderStatus.class);

    public OrderStateFactory(List<OrderState> states) {
        for (OrderState state : states) {
            stateMap.put(state.getStatus(), state);
        }
    }

    public OrderState getState(OrderStatus status) {
        OrderState state = stateMap.get(status);
        if (state == null) {
            throw new IllegalArgumentException("No state handler registered for status: " + status);
        }
        return state;
    }
}

