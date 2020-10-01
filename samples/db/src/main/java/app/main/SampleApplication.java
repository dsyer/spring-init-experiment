package app.main;

import app.main.foo.Foo;
import app.main.foo.FooRepository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication(proxyBeanMethods = false)
public class SampleApplication {

	private FooRepository entities;

	public SampleApplication(FooRepository entities) {
		this.entities = entities;
	}

	@Bean
	public CommandLineRunner runner() {
		return args -> {
			Foo foo = entities.find(1L);
			if (foo == null) {
				entities.save(new Foo("Hello"));
			}
		};
	}

	@Bean
	public RouterFunction<?> userEndpoints() {
		return route(GET("/"), request -> ok()
				.body(Mono.fromCallable(() -> entities.find(1L)).subscribeOn(Schedulers.boundedElastic()), Foo.class));
	}

	public static void main(String[] args) {
		new SpringApplicationBuilder(SampleApplication.class).initializers(SampleApplication::process).run(args);
	}

	private static void process(ConfigurableApplicationContext registry) {
		if (!registry.containsBeanDefinition(AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			((GenericApplicationContext) registry).registerBean(
					AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME,
					AutowiredAnnotationBeanPostProcessor.class, () -> new AutowiredAnnotationBeanPostProcessor());
		}

	}

}
