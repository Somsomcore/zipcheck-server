package somsomcore.zipcheck.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import somsomcore.zipcheck.domain.auth.dto.AuthResponseDto;
import somsomcore.zipcheck.domain.auth.dto.SocialLoginRequestDto;
import somsomcore.zipcheck.domain.auth.dto.SocialUserInfoDto;
import somsomcore.zipcheck.domain.auth.dto.TokenRefreshRequestDto;
import somsomcore.zipcheck.domain.auth.dto.TokenRefreshResponseDto;
import somsomcore.zipcheck.domain.auth.entity.RefreshToken;
import somsomcore.zipcheck.domain.auth.repository.RefreshTokenRepository;
import somsomcore.zipcheck.domain.user.entity.User;
import somsomcore.zipcheck.domain.user.entity.enums.Role;
import somsomcore.zipcheck.domain.user.repository.UserRepository;
import somsomcore.zipcheck.global.jwt.JwtUtil;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final OAuthService oAuthService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    public AuthResponseDto socialLogin(SocialLoginRequestDto request) {
        SocialUserInfoDto socialUserInfo = oAuthService.getSocialUserInfo(request.getAccessToken(), request.getProvider());

        User user = findOrCreateUser(socialUserInfo);

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        saveOrUpdateRefreshToken(user.getId(), refreshToken);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(AuthResponseDto.UserInfo.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .build())
                .build();
    }

    private User findOrCreateUser(SocialUserInfoDto socialUserInfo) {
        Optional<User> existingUser = userRepository.findByEmailAndOauthType(
                socialUserInfo.getEmail(), socialUserInfo.getProvider());

        if (existingUser.isPresent()) {
            log.info("기존 사용자 로그인: {}", socialUserInfo.getEmail());
            return existingUser.get();
        }

        User newUser = User.builder()
                .name(socialUserInfo.getName())
                .email(socialUserInfo.getEmail())
                .oauthType(socialUserInfo.getProvider())
                .role(Role.MEMBER)
                .phone("")
                .isVerified(true)
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("새 사용자 생성: {}", socialUserInfo.getEmail());

        return savedUser;
    }

    public TokenRefreshResponseDto refreshToken(TokenRefreshRequestDto request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("리프레시 토큰을 찾을 수 없습니다."));

        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw new RuntimeException("만료된 리프레시 토큰입니다.");
        }

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        storedToken.updateToken(newRefreshToken, LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(storedToken);

        return TokenRefreshResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    private void saveOrUpdateRefreshToken(Long userId, String refreshToken) {
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);

        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(userId);

        if (existingToken.isPresent()) {
            existingToken.get().updateToken(refreshToken, expiryDate);
            refreshTokenRepository.save(existingToken.get());
        } else {
            RefreshToken newRefreshToken = RefreshToken.builder()
                    .userId(userId)
                    .token(refreshToken)
                    .expiryDate(expiryDate)
                    .build();
            refreshTokenRepository.save(newRefreshToken);
        }
    }

    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info("사용자 로그아웃 완료: {}", userId);
    }
}