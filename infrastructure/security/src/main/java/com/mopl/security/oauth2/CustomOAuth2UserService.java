package com.mopl.security.oauth2;

import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.domain.service.user.UserService;
import com.mopl.security.oauth2.userinfo.OAuth2UserInfo;
import com.mopl.security.oauth2.userinfo.OAuth2UserInfoFactory;
import com.mopl.security.userdetails.MoplUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Locale;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.create(
            registrationId,
            oAuth2User.getAttributes()
        );

        String email = userInfo.getEmail();
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("email_not_found"),
                "OAuth2 제공자로부터 이메일을 가져올 수 없습니다."
            );
        }

        email = email.strip().toLowerCase(Locale.ROOT);

        Optional<UserModel> existingUser = userRepository.findByEmail(email);
        UserModel user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            validateExistingUser(user, userInfo);
        } else {
            user = registerNewUser(userInfo, email);
        }

        MoplUserDetails userDetails = MoplUserDetails.from(user);
        return new OAuth2UserPrincipal(userDetails, oAuth2User.getAttributes());
    }

    private void validateExistingUser(UserModel user, OAuth2UserInfo userInfo) {
        if (user.isLocked()) {
            log.warn("OAuth2 로그인 실패: 계정이 잠겨 있습니다. email={}", user.getEmail());
            throw new LockedException("계정이 잠겨 있습니다.");
        }

        if (user.getAuthProvider() != userInfo.getProvider()) {
            log.warn(
                "OAuth2 로그인 실패: 다른 제공자로 가입된 계정입니다. email={}, existingProvider={}, attemptedProvider={}",
                user.getEmail(), user.getAuthProvider(), userInfo.getProvider());
            throw new OAuth2AuthenticationException(
                new OAuth2Error("provider_mismatch"),
                "이미 " + user.getAuthProvider().name() + "(으)로 가입된 계정입니다."
            );
        }
    }

    private UserModel registerNewUser(OAuth2UserInfo userInfo, String email) {
        log.info("OAuth2 신규 사용자 등록: email={}, provider={}", email, userInfo.getProvider());

        UserModel newUser = UserModel.createOAuthUser(
            userInfo.getProvider(),
            email,
            userInfo.getName()
        );

        return userService.create(newUser);
    }
}
