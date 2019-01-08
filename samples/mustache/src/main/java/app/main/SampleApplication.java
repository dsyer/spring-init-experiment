package app.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.mustache.MustacheAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.init.SpringInitApplication;

@SpringInitApplication({ WebFluxAutoConfiguration.class, 
		MustacheAutoConfiguration.class })
public class SampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}
}
