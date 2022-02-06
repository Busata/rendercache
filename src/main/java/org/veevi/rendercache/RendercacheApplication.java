package org.veevi.rendercache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.veevi.rendercache.configuration.RenderCacheProperties;

@SpringBootApplication
public class RendercacheApplication {

	public static void main(String[] args) {
		SpringApplication.run(RendercacheApplication.class, args);
	}

}
