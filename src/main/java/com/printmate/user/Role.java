package com.printmate.user;

public enum Role {
    CUSTOMER,
    OPERATOR,
    ADMIN;

    public String withPrefix() {
        return "ROLE_" + this.name();
    }

    public String getDashboardPath() {
        return switch (this) {
            case CUSTOMER -> "/customer/dashboard";
            case OPERATOR -> "/operator/dashboard";
            case ADMIN -> "/admin/dashboard";
        };
    }
}