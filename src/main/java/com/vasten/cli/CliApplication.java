package com.vasten.cli;

import java.util.concurrent.Executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan({ "com.vasten.cli", "com.vasten.cli.controller", "com.vasten.cli.security.config",
		"com.vasten.cli.config", "com.vasten.cli.service.impl" })
//@EnableAsync
public class CliApplication {

	public static void main(String[] args) {
		SpringApplication.run(CliApplication.class, args);
	}

//	@Bean(name = "asyncExecutor")
//	public Executor asyncExecutor() {
//		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//		executor.setCorePoolSize(5);
//		executor.setMaxPoolSize(5);
//		executor.setQueueCapacity(500);
//		executor.setThreadNamePrefix("VastenAsync-");
//		executor.initialize();
//		return executor;
//	}

}
