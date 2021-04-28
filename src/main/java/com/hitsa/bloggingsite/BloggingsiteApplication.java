package com.hitsa.bloggingsite;

import com.hitsa.bloggingsite.config.SwaggerConfiguration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@Import(SwaggerConfiguration.class)
public class BloggingsiteApplication {

	public static void main(String[] args) {
		SpringApplication.run(BloggingsiteApplication.class, args);
	}

}
