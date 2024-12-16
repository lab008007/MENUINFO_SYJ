package com.aloha.menuinfo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@EnableCaching
@SpringBootApplication
public class MenuinfoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MenuinfoApplication.class, args);
	}

}
