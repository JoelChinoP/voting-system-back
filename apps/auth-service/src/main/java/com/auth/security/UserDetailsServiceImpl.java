package com.auth.security;

import com.auth.model.User;
import com.auth.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscar por email o username
        User user = userRepository
                .findByEmailOrUsername(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Convertir roles en GrantedAuthority
        Collection<GrantedAuthority> authorities = createAuthorities(user.getRoles());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isActive(),
                true,
                true,
                user.isActive(),
                authorities
        );
    }

    /**
     * Convierte el conjunto de roles en GrantedAuthorities
     */
    private Collection<GrantedAuthority> createAuthorities(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return roles.stream()
                .map(String::toUpperCase)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
