package com.printmate.user;

import com.printmate.common.exception.InvalidOperationException;
import com.printmate.common.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessRoleRepository accessRoles;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AccessRoleRepository accessRoles) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessRoles = accessRoles;
    }

    @Transactional
    public User registerCustomer(String fullName, String email, String password, String phone) {
        validateAccount(fullName, email, password, phone);
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new InvalidOperationException("An account with this email already exists.");
        }
        User user = new User(fullName.trim(), normalizedEmail, passwordEncoder.encode(password), phone != null ? phone.trim() : null, Role.CUSTOMER);
        assignAccessRole(user, Role.CUSTOMER);
        return userRepository.save(user);
    }

    private void validateAccount(String fullName, String email, String password, String phone) {
        if (fullName == null || fullName.trim().length() < 2 || fullName.trim().length() > 100) {
            throw new InvalidOperationException("Full name must have at least 2 characters.");
        }
        if (email == null || email.trim().length() > 120 || !email.trim().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new InvalidOperationException("Please provide a valid email address.");
        }
        if (password == null || password.length() < 6 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new InvalidOperationException("Password must have at least 6 characters and at most 72 UTF-8 bytes.");
        }
        if (phone != null && phone.trim().length() > 30) throw new InvalidOperationException("Phone number must be at most 30 characters.");
    }

    @Transactional
    public User createOperator(String fullName, String email, String password, String phone) {
        validateAccount(fullName, email, password, phone);
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new InvalidOperationException("An account with this email already exists.");
        }

        String hash = passwordEncoder.encode(password);
        User user = new User(fullName.trim(), normalizedEmail, hash, phone != null ? phone.trim() : null, Role.OPERATOR);
        assignAccessRole(user, Role.OPERATOR);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        user.setRole(newRole);
        assignAccessRole(user, newRole);
        return userRepository.save(user);
    }

    @Transactional
    public User toggleUserEnabled(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        if (user.getRole() == Role.ADMIN) throw new InvalidOperationException("Administrator accounts cannot be disabled here.");
        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> getOperators() {
        return userRepository.findByRole(Role.OPERATOR);
    }

    @Transactional(readOnly = true)
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    @Transactional
    public User updateProfile(Long id, String fullName, String phone) {
        User user = getUser(id);
        if (fullName == null || fullName.trim().length() < 2 || fullName.trim().length() > 100)
            throw new InvalidOperationException("Full name must be between 2 and 100 characters.");
        if (phone != null && phone.trim().length() > 30) throw new InvalidOperationException("Phone number must be at most 30 characters.");
        user.setFullName(fullName.trim());
        user.setPhone(phone == null ? null : phone.trim());
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUser(id);
        if (user.getRole() == Role.ADMIN) throw new InvalidOperationException("Administrator accounts cannot be deleted here.");
        userRepository.delete(user);
        userRepository.flush(); // Linked documents/orders preserve the customer's audit history.
    }

    private void assignAccessRole(User user, Role role) {
        String code = AccessRole.codeFor(role);
        AccessRole accessRole = accessRoles.findById(code).orElseThrow(() -> new InvalidOperationException("Access roles have not been initialized."));
        user.setAccessRoles(java.util.Set.of(accessRole));
    }
}
