package org.springframework.samples.petclinic.visits.model;

import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactoryFriend;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.samples.petclinic.visits.VisitLine;

public final class Visits {

	private static volatile VisitsService service;

	private Visits() {
	}

	public static VisitLine[] read(int petId) {
		return service().read(petId);
	}

	private static VisitsService service() {
		VisitsService current = service;
		if (current == null) {
			synchronized (Visits.class) {
				current = service;
				if (current == null) {
					preferLogbackOverGatewayNop();
					ConfigurableApplicationContext context = SpringApplication.run(VisitsSpringBoot.class);
					current = context.getBean(VisitsService.class);
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
