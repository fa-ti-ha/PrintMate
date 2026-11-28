package com.printmate.user;

import com.printmate.common.exception.InvalidOperationException;
import com.printmate.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {
    @Mock UserRepository users;
    @Mock PasswordEncoder encoder;
    @Mock AccessRoleRepository roles;
    UserService service;
    @BeforeEach void setup() {
        service = new UserService(users, encoder, roles);
        lenient().when(roles.findById(anyString())).thenAnswer(i -> Optional.of(new AccessRole(i.getArgument(0))));
    }

    @Test void registrationNormalizesEmailAndHashesPassword() {
        when(encoder.encode("secret12")).thenReturn("hashed");
        when(users.save(any())).thenAnswer(i -> i.getArgument(0));
        User user = service.registerCustomer(" Alice ", " ALICE@EXAMPLE.COM ", "secret12", " 123 ");
        assertEquals("alice@example.com", user.getEmail()); assertEquals("Alice", user.getFullName());
        assertEquals("hashed", user.getPasswordHash()); assertEquals(Role.CUSTOMER, user.getRole());
    }
    @Test void duplicateEmailIsRejectedBeforeHashing() {
        when(users.existsByEmailIgnoreCase("alice@example.com")).thenReturn(true);
        assertThrows(InvalidOperationException.class, () -> service.registerCustomer("Alice", "alice@example.com", "secret12", null));
        verifyNoInteractions(encoder);
    }
    @Test void shortNameIsRejected() { assertThrows(InvalidOperationException.class, () -> service.registerCustomer("A", "a@b.com", "secret12", null)); }
    @Test void malformedEmailIsRejected() { assertThrows(InvalidOperationException.class, () -> service.registerCustomer("Alice", "a@@b.com", "secret12", null)); }
    @Test void shortPasswordIsRejected() { assertThrows(InvalidOperationException.class, () -> service.registerCustomer("Alice", "a@b.com", "123", null)); }
    @Test void bcryptByteLimitIsEnforced() { assertThrows(InvalidOperationException.class, () -> service.registerCustomer("Alice", "a@b.com", "界".repeat(25), null)); }
    @Test void overlongPhoneIsRejected() { assertThrows(InvalidOperationException.class, () -> service.registerCustomer("Alice", "a@b.com", "secret12", "1".repeat(31))); }
    @Test void operatorCreationUsesSameValidation() { assertThrows(InvalidOperationException.class, () -> service.createOperator("", null, "", null)); verifyNoInteractions(users, encoder); }
    @Test void operatorCreationAssignsOperatorRole() {
        when(encoder.encode("secret12")).thenReturn("hash"); when(users.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals(Role.OPERATOR, service.createOperator("Shop Staff", "staff@example.com", "secret12", null).getRole());
    }
    @Test void administratorCannotBeDisabled() {
        when(users.findById(1L)).thenReturn(Optional.of(new User("Admin", "a@b.com", "hash", null, Role.ADMIN)));
        assertThrows(InvalidOperationException.class, () -> service.toggleUserEnabled(1L)); verify(users, never()).save(any());
    }
    @Test void administratorCannotBeDeleted() {
        when(users.findById(1L)).thenReturn(Optional.of(new User("Admin", "a@b.com", "hash", null, Role.ADMIN)));
        assertThrows(InvalidOperationException.class, () -> service.deleteUser(1L)); verify(users, never()).delete(any());
    }
    @Test void missingProfileReturnsNotFound() { assertThrows(ResourceNotFoundException.class, () -> service.updateProfile(99L, "Alice", null)); }
    @Test void profileEditPreservesRoleAndCredentials() {
        User user = new User("Alice", "a@b.com", "hash", null, Role.CUSTOMER);
        when(users.findById(1L)).thenReturn(Optional.of(user)); when(users.save(user)).thenReturn(user);
        service.updateProfile(1L, " Alice Updated ", "123");
        assertEquals("Alice Updated", user.getFullName()); assertEquals("hash", user.getPasswordHash()); assertEquals(Role.CUSTOMER, user.getRole());
    }
}
