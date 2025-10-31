package somsomcore.zipcheck.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:policy.properties")
@EnableConfigurationProperties(PolicyProperties.class)
public class PolicyConfig {
}
