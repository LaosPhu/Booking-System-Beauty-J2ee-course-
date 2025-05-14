package com.J2EEWEB.beautyweb;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class BeautywebApplication {

	@Autowired
	private Environment environment;

	@PostConstruct
	public void loadEnv() {
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load(); // Load .env, don't crash if missing

		//Optionally check if the required variables are set
		if (dotenv.get("GOOGLE_CLIENT_ID") == null || dotenv.get("GOOGLE_CLIENT_SECRET") == null)
		{
			System.err.println("GOOGLE_CLIENT_ID or GOOGLE_CLIENT_SECRET is not set in .env file");
			//Handle the error as you see fit.  You might want to throw an exception.
		}

		//Set system properties.  This is CRUCIAL for Spring to find the values.
		if(dotenv.get("GOOGLE_CLIENT_ID") != null) {
			System.setProperty("GOOGLE_CLIENT_ID", dotenv.get("GOOGLE_CLIENT_ID"));
		}
		if(dotenv.get("GOOGLE_CLIENT_SECRET") != null) {
			System.setProperty("GOOGLE_CLIENT_SECRET", dotenv.get("GOOGLE_CLIENT_SECRET"));
		}
		if (dotenv.get("SPRING_MAIL_USERNAME") != null) {
			System.setProperty("SPRING_MAIL_USERNAME", dotenv.get("SPRING_MAIL_USERNAME"));
		}
		if (dotenv.get("SPRING_MAIL_PASSWORD") != null) {
			System.setProperty("SPRING_MAIL_PASSWORD", dotenv.get("SPRING_MAIL_PASSWORD"));
		}

	}

	public static void main(String[] args) {
		SpringApplication.run(BeautywebApplication.class, args);
	}

}
