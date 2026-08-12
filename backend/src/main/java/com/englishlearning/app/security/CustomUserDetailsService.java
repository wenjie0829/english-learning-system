package com.englishlearning.app.security;

import com.englishlearning.app.entity.User;
import com.englishlearning.app.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrId) throws UsernameNotFoundException {
        User user;
        try {
            Long id = Long.parseLong(usernameOrId);
            user = userRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + usernameOrId));
        } catch (NumberFormatException e) {
            user = userRepository.findByUsername(usernameOrId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + usernameOrId));
        }
        return UserPrincipal.create(user);
    }
}
