package com.printmate.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/** Lab role vocabulary, shared by users through the user_roles join table. */
@Entity
@Table(name = "roles")
public class AccessRole {
    @Id
    @Column(length = 20)
    private String code;

    protected AccessRole() {}
    public AccessRole(String code) { this.code = code; }
    public String getCode() { return code; }
    public static String codeFor(Role role) {
        return switch (role) { case CUSTOMER -> "BUYER"; case OPERATOR -> "SELLER"; case ADMIN -> "ADMIN"; };
    }
    @Override public boolean equals(Object other) { return other instanceof AccessRole role && Objects.equals(code, role.getCode()); }
    @Override public int hashCode() { return Objects.hashCode(code); }
}
