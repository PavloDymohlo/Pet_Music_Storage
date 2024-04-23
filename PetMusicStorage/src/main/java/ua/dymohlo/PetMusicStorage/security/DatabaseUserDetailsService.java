package ua.dymohlo.PetMusicStorage.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ua.dymohlo.PetMusicStorage.entity.User;
import ua.dymohlo.PetMusicStorage.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {//test+
        long phoneNumber = Long.parseLong(username);
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user != null) {
            String subscriptionRole = user.getSubscription().getSubscriptionName();
            List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_" + subscriptionRole);
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    String.valueOf(user.getPhoneNumber()),
                    user.getPassword(),
                    authorities
            );
            return userDetails;
        }
        throw new UsernameNotFoundException("user with phone number " + phoneNumber + " not found!");
    }
}
