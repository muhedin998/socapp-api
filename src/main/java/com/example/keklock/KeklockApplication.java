package com.example.keklock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KeklockApplication {

	public static void main(String[] args) {
		SpringApplication.run(KeklockApplication.class, args);
	}

}
