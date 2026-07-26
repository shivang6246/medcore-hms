package com.medcore.hms.auth.service;

import com.medcore.hms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * Custom {@link UserDetailsService} that loads users from the database by email.
 *
 * <p>Extracted from the inline lambda in {@code SecurityConfig} to:
 * <ul>
 *   <li>Keep {@code SecurityConfig} lean (no DB dependency).</li>
 *   <li>Allow injection into the JWT filter and other services.</li>
 *   <li>Support transaction management on the DB call.</li>
 * </ul>
 *
 * <p>Role authorities follow the convention {@code ROLE_<RoleName>}, e.g.
 * {@code ROLE_DOCTOR}, {@code ROLE_SUPER_ADMIN}.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a {@link UserDetails} instance by email (the system's username).
     *
     * @param email the user's email address
     * @return populated UserDetails with authorities
     * @throws UsernameNotFoundException if no user with that email exists
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(user -> {
                    var authorities = user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                            .collect(Collectors.toSet());

                    return org.springframework.security.core.userdetails.User
                            .withUsername(user.getEmail())
                            .password(user.getPasswordHash())
                            .authorities(authorities)
                            .accountExpired(false)
                            .accountLocked(!user.getIsActive())
                            .credentialsExpired(false)
                            .disabled(!user.getIsActive())
                            .build();
                })
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));
    }
}
