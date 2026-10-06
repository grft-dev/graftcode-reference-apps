package org.springframework.samples.petclinic.customers.model;

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
					ConfigurableApplicationContext context = SpringApplication.run(CustomersSpringBoot.class);
					current = context.getBean(CustomersService.class);
					service = current;
				}
			}
		}
		return current;
	}

}
