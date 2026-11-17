package br.com.diegopimenta.food_app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class FoodAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodAppApplication.class, args);
	}

}
