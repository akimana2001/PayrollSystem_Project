package com.payroll.payrollsystem.security;

import com.payroll.payrollsystem.model.User;
import com.payroll.payrollsystem.model.enums.Role;
import com.payroll.payrollsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserService
        extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {

        OAuth2User oAuth2User = super.loadUser(request);

        String provider = request.getClientRegistration()
                .getRegistrationId();

        // Extract email and name from provider
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // GitHub fallback — use login if name is null
        if (name == null) {
            name = oAuth2User.getAttribute("login");
        }

        // GitHub email fallback if set to private
        if (email == null) {
            String login =
                    oAuth2User.getAttribute("login");
            email = login + "@github-user.com";
        }

        final String finalEmail = email;
        final String finalName = name != null
                ? name : email;
        final String finalProvider = provider;

        log.info("OAuth2 login: {} via {}",
                finalEmail, finalProvider);

        userRepository.findByEmail(finalEmail)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .username(finalName)
                            .email(finalEmail)
                            .password("OAUTH2_"
                                    + finalProvider
                                    .toUpperCase())
                            .role(Role.ROLE_HR_MANAGER)
                            .enabled(true)
                            .build();
                    userRepository.save(newUser);
                    log.info("New OAuth2 user: {}",
                            finalEmail);
                    return newUser;
                });

        return oAuth2User;
    }
}
