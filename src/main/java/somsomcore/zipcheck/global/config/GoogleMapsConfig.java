package somsomcore.zipcheck.global.config;

import com.google.maps.GeoApiContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Configuration
public class GoogleMapsConfig {
    @Bean(destroyMethod = "shutdown")
    public GeoApiContext geoApiContext(@Value("${google.api.key}") String apiKey) {
        return new GeoApiContext.Builder().apiKey(apiKey).build();
    }
}