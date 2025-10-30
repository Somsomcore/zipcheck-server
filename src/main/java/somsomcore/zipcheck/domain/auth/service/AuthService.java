package somsomcore.zipcheck.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import somsomcore.zipcheck.domain.auth.dto.*;
import somsomcore.zipcheck.domain.auth.entity.RefreshToken;
import somsomcore.zipcheck.domain.auth.repository.RefreshTokenRepository;
import somsomcore.zipcheck.domain.user.entity.User;
import somsomcore.zipcheck.domain.user.entity.enums.Role;
import somsomcore.zipcheck.domain.user.repository.UserRepository;
import somsomcore.zipcheck.global.apiPayload.code.status.ErrorStatus;
import somsomcore.zipcheck.global.apiPayload.exception.GeneralException;
import somsomcore.zipcheck.global.jwt.JwtUtil;
import somsomcore.zipcheck.global.util.SmsUtil;
import somsomcore.zipcheck.global.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    @Value("${zipcheck.verification.code-expiry-minutes}")
    private long codeExpiryMinutes;

    private final OAuthService oAuthService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
	private final ValidationUtil validationUtil;
	private final SmsUtil smsUtil;

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
						.role(user.getRole())
                        .build())
                .build();
    }

    private User findOrCreateUser(SocialUserInfoDto socialUserInfo) {
        if (socialUserInfo.getEmail() == null || socialUserInfo.getEmail().isBlank()) {
            throw new GeneralException(ErrorStatus.OAUTH_EMAIL_REQUIRED);
        }

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
                .isVerified(false)
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("새 사용자 생성: {}", socialUserInfo.getEmail());

        return savedUser;
    }

    public TokenRefreshResponseDto refreshToken(TokenRefreshRequestDto request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new GeneralException(ErrorStatus.REFRESH_TOKEN_INVALID);
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REFRESH_TOKEN_NOT_FOUND));

        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw new GeneralException(ErrorStatus.REFRESH_TOKEN_EXPIRED);
        }

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

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

    public AuthResponseDto generateTestToken(TestTokenRequestDto request) {
        String accessToken = jwtUtil.generateAccessToken(request.getUserId(), request.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(request.getUserId(), request.getEmail());

        saveOrUpdateRefreshToken(request.getUserId(), refreshToken);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(AuthResponseDto.UserInfo.builder()
                        .id(request.getUserId())
                        .name("테스트 사용자")
                        .email(request.getEmail())
						.role(Role.MEMBER)
                        .build())
                .build();
    }
	
	public void sendValidationMessage(Long userId, ValidationMessageRequestDto request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
		
		if (!request.getPhone().matches("^010(-\\d{4}-\\d{4}|\\d{8})$")) {
			throw new GeneralException(ErrorStatus.INVALID_PHONE_NUMBER);
		}
		
		String verificationCode = validationUtil.createCode();
		user.updatePhoneValidation(verificationCode, LocalDateTime.now().plusMinutes(codeExpiryMinutes));
		userRepository.save(user);
		
		smsUtil.sendOne(request.getPhone(), verificationCode);
	}
	
	public void validateUserPhone(Long userId, ValidatePhoneRequestDto request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
		String userVerificationCode = user.getPhoneValidationCode();
		
		if (userVerificationCode == null || userVerificationCode.isEmpty()) {
			throw new GeneralException(ErrorStatus.VERIFICATION_NOT_FOUND);
		}
		if(user.getPhoneValidationExpiresAt().isBefore(LocalDateTime.now())) {
			throw new GeneralException(ErrorStatus.VERIFICATION_EXPIRED);
		}
		if(!userVerificationCode.equals(request.getVerificationCode())) {
			throw new GeneralException(ErrorStatus.VERIFICATION_INVALID);
		}
		
		user.setVerified(true);
		user.setPhoneValidationCode(null);
		user.setPhoneValidationExpiresAt(null);
		userRepository.save(user);
	}
}
