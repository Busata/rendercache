package org.veevi.rendercache.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="rendercache")
@Data
public class RenderCacheProperties {
    public String storagePath;
}
