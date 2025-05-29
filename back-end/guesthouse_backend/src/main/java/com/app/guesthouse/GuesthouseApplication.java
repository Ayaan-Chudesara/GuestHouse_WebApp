package com.app.guesthouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.app.guesthouse")

public class GuesthouseApplication {

	public static void main(String[] args) {
		SpringApplication.run(GuesthouseApplication.class, args);
	}

}


