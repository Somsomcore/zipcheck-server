package somsomcore.zipcheck.global.util;

import jakarta.annotation.PostConstruct;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SmsUtil {

	@Value("${SOLAPI_API_KEY}")
	private String apiKey;

	@Value("${SOLAPI_API_SECRET}")
	private String apiSecret;

	@Value("${solapi.domain}")
	private String domain;

	@Value("${solapi.from-number}")
	private String fromNumber;

	private DefaultMessageService messageService;

	@PostConstruct
	public void init() {
		this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, domain);
	}

	public SingleMessageSentResponse sendOne(String to, String verificationCode) {
		Message message = new Message();
		message.setFrom(fromNumber);
		message.setTo(to);
		message.setText("[Zipcheck] 아래의 인증번호를 입력해주세요\n" + verificationCode);

		return this.messageService.sendOne(new SingleMessageSendingRequest(message));
	}
}
