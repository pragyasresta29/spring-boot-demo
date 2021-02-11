package com.example.springboot;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.Arrays;


@EnableJpaRepositories("com.example.springboot.**")
@EntityScan("com.example.springboot.**")
@ComponentScan("com.example.springboot.**")
/**
 *  @SpringBootApplicaion is equivalent to using @Configuration, @EnableAutoConfiguration, @ComponentScan
 */
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			System.out.println("Let's inspect the beans provided by Spring Boot:");

			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				System.out.println(beanName);
			}
		};
	}

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http.authorizeExchange()
				.pathMatchers("/actuator/**").permitAll()
				.anyExchange().authenticated()
				.and().build();
	}

    @Bean
    MeterRegistryCustomizer<MeterRegistry> addPersonRegistry() {
        return registry -> registry.config().namingConvention().name("counter.login.success", Meter.Type.COUNTER);
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> deletePersonRegistry() {
        return registry -> registry.config().namingConvention().name("counter.login.failure", Meter.Type.COUNTER);
    }


}
