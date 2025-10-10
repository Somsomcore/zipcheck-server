package somsomcore.zipcheck.global.util;

import org.springframework.stereotype.Component;

@Component
public class ValidationUtil {
	
	public String createCode() {
		StringBuilder code = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			int digit = (int) (Math.random() * 10);
			code.append(digit);
		}
		return code.toString();
	}
}
