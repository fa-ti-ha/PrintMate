package com.printmate.order;

import com.printmate.user.User;
import org.springframework.context.ApplicationEvent;

public class OrderStatusChangedEvent extends ApplicationEvent {

    private final Long orderId;
    private final String orderNumber;
    private final User customer;
    private final OrderStatus previousStatus;
    private final OrderStatus newStatus;
    private final String changedBy;
    private final String remarks;
    private final String plainPickupCode;

    public OrderStatusChangedEvent(Object source, Long orderId, String orderNumber, User customer,
                                 OrderStatus previousStatus, OrderStatus newStatus,
                                 String changedBy, String remarks, String plainPickupCode) {
        super(source);
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.customer = customer;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.remarks = remarks;
        this.plainPickupCode = plainPickupCode;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public User getCustomer() {
        return customer;
    }

    public OrderStatus getPreviousStatus() {
        return previousStatus;
    }

    public OrderStatus getNewStatus() {
        return newStatus;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getPlainPickupCode() {
        return plainPickupCode;
    }
}

