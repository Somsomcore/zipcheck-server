package somsomcore.zipcheck.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import somsomcore.zipcheck.domain.user.dto.UserProfileResponseDto;
import somsomcore.zipcheck.domain.user.entity.User;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    public UserProfileResponseDto getUserProfile(User user) {
        return UserProfileResponseDto.from(user);
    }
}