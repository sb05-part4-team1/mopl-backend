package com.mopl.domain.service.user;

import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserModel signUp() {

        validateDuplicateEmail(userModel.getEmail());

        // 2. 저장 (인터페이스 호출)
        // 실제 저장 처리는 modules:jpa의 UserRepositoryImpl에서 수행됩니다.
        return userRepository.save(userModel);
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            // supports:common에 정의한 커스텀 예외를 던지는 것이 좋습니다.
            throw new IllegalArgumentException("이미 가입된 이메일입니다: " + email);
        }
    }

    /**
     * 회원 조회 (도메인 모델 반환)
     */
    public UserModel getUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
