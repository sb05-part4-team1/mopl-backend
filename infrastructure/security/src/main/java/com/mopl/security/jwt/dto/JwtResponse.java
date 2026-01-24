package com.mopl.security.jwt.dto;

import com.mopl.domain.model.user.UserModel;
import com.mopl.security.userdetails.MoplUserDetails;

import java.time.Instant;
import java.util.UUID;

public record JwtResponse(UserDetailsDto userDto, String accessToken) {

    public static JwtResponse from(MoplUserDetails userDetails, String accessToken) {
        return new JwtResponse(UserDetailsDto.from(userDetails), accessToken);
    }

    public record UserDetailsDto(
        UUID id,
        Instant createdAt,
        String email,
        String name,
        String profileImagePath,
        UserModel.Role role,
        Boolean locked
    ) {

        public static UserDetailsDto from(MoplUserDetails userDetails) {
            return new UserDetailsDto(
                userDetails.userId(),
                userDetails.createdAt(),
                userDetails.email(),
                userDetails.name(),
                userDetails.profileImagePath(),
                userDetails.role(),
                userDetails.locked()
            );
        }
    }
}
