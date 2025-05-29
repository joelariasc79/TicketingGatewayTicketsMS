package com.ticketing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class TicketingGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketingGatewayApplication.class, args);
	}

}
