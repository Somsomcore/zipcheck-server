package somsomcore.zipcheck.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ValidationMessageRequestDto {
	
	@NotBlank(message = "전화번호는 필수입니다.")
	private String phone;
}
