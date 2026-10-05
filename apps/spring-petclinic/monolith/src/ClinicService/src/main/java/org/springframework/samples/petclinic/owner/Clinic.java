package org.springframework.samples.petclinic.owner;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public final class Clinic {

	private static volatile ClinicService service;

	private Clinic() {
	}

	public static OwnerDto[] listOwners() {
		return service().listOwners();
	}

	public static OwnerDto getOwner(int id) {
		return service().getOwner(id);
	}

	public static PetDto[] getPets(int ownerId) {
		return service().getPets(ownerId);
	}

	public static VisitDto[] getVisits(int ownerId, int petId) {
		return service().getVisits(ownerId, petId);
	}

	public static VisitDto addVisit(int ownerId, int petId, String date, String description) {
		return service().addVisit(ownerId, petId, date, description);
	}

	private static ClinicService service() {
		ClinicService current = service;
		if (current == null) {
			synchronized (Clinic.class) {
				current = service;
				if (current == null) {
					ConfigurableApplicationContext context = SpringApplication.run(ClinicSpringBoot.class);
					current = context.getBean(ClinicService.class);
					service = current;
				}
			}
		}
		return current;
	}

}
