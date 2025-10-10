package somsomcore.zipcheck.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ValidatePhoneRequestDto {
	
	@NotBlank(message = "인증 번호는 필수입니다.")
	private String verificationCode;
}
