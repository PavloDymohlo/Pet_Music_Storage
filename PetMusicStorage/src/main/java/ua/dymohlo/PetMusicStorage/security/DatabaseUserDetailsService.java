package ua.dymohlo.PetMusicStorage.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("Пользователь '%s' не найден", username)
                ));
        String phoneNumber = String.valueOf(user.getPhoneNumber());
        String password = user.getPassword();
        String subscriptionName = "ROLE_" + user.getSubscription().getSubscriptionName();
        log.info("Loading user with phone number: {}, subscription: {}", phoneNumber, subscriptionName);
        return new org.springframework.security.core.userdetails.User(
                phoneNumber,
                password,
                Collections.singleton(new SimpleGrantedAuthority(subscriptionName))
        );
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(userRepository.findByPhoneNumber(Long.parseLong(username)));
    }
}