package org.springframework.samples.petclinic.vets.model;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public final class Vets {

	private static volatile VetsService service;

	private Vets() {
	}

	public static VetDto getVet(int id) {
		return service().getVet(id);
	}

	private static VetsService service() {
		VetsService current = service;
		if (current == null) {
			synchronized (Vets.class) {
				current = service;
				if (current == null) {
					ConfigurableApplicationContext context = SpringApplication.run(VetsSpringBoot.class);
					current = context.getBean(VetsService.class);
					service = current;
				}
			}
		}
		return current;
	}

}
