package com.example.campus.security;

import com.example.campus.repository.TsukiUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final TsukiUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameIgnoreCase(username)
                .map(UserPrincipal::fromEntity)
                .orElseThrow(() -> new UsernameNotFoundException("未找到用户名为" + username + "的用户"));
    }
}
