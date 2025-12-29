package com.mopl.api.interfaces.api.user;

import com.mopl.domain.model.user.UserModel;
import org.springframework.stereotype.Component;

@Component
public class UserSummaryMapper {
    public UserSummary toSummary(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        return new UserSummary(
                userModel.getId(),
                userModel.getName(),
                userModel.getProfileImageUrl()
        );
    }
}
