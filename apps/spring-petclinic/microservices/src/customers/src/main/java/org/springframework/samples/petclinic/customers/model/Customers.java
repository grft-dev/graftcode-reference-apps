package org.springframework.samples.petclinic.customers.model;

import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactoryFriend;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public final class Customers {

	private static volatile CustomersService service;

	private Customers() {
	}

	public static OwnerDto getOwner(int id) {
		return service().getOwner(id);
	}

	public static OwnerDto getOwnerForPet(int petId) {
		return service().getOwnerForPet(petId);
	}

	private static CustomersService service() {
		CustomersService current = service;
		if (current == null) {
			synchronized (Customers.class) {
				current = service;
				if (current == null) {
					preferLogbackOverGatewayNop();
					ConfigurableApplicationContext context = SpringApplication.run(CustomersSpringBoot.class);
					current = context.getBean(CustomersService.class);
					service = current;
				}
			}
		}
		return current;
	}

	/**
	 * Sample workaround when the host runs inside gg: hypertube-java-core may bind NOPLoggerFactory
	 * before Spring Boot starts. Product fix belongs in the gg JVM runtime, not in host apps.
	 */
	private static void preferLogbackOverGatewayNop() {
		System.setProperty("slf4j.provider", "ch.qos.logback.classic.spi.LogbackServiceProvider");
		LoggerFactoryFriend.reset();
		LoggerFactory.getILoggerFactory();
	}

}
