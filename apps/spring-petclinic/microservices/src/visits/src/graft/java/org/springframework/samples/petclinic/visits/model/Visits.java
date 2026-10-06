package org.springframework.samples.petclinic.visits.model;

import java.util.List;

import org.springframework.samples.petclinic.visits.VisitLine;
import org.springframework.samples.petclinic.visits.VisitsSpringBoot;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import graft.maven.org.springframework.samples.petclinic.customers.model.Customers;
import graft.maven.org.springframework.samples.petclinic.customers.model.OwnerDto;

public final class Visits {

	private static volatile VisitRepository repository;

	private Visits() {
	}

	public static VisitLine[] read(int petId) {
		List<Visit> stored = repository().findByPetId(petId);
		OwnerDto owner = Customers.getOwnerForPet(petId);
		VisitLine[] lines = new VisitLine[stored.size()];
		for (int i = 0; i < stored.size(); i++) {
			Visit visit = stored.get(i);
			lines[i] = new VisitLine(VisitDates.iso(visit.getDate()), visit.getDescription(), owner.getFirstName(),
					owner.getLastName());
		}
		return lines;
	}

	private static VisitRepository repository() {
		VisitRepository current = repository;
		if (current == null) {
			synchronized (Visits.class) {
				current = repository;
				if (current == null) {
					ConfigurableApplicationContext context = SpringApplication.run(VisitsSpringBoot.class);
					current = context.getBean(VisitRepository.class);
					repository = current;
				}
			}
		}
		return current;
	}

}
