package somsomcore.zipcheck.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "policy")
@Getter
@Setter
public class PolicyProperties {

    private Map<String, RegionRule> repaymentRules;

    @Getter
    @Setter
    public static class RegionRule {
        private String description;
        private long depositLimit;
        private long repaymentAmount;
    }
}