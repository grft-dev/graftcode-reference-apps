package org.springframework.samples.petclinic.visits.model;

import java.util.List;

import org.springframework.samples.petclinic.visits.VisitLine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import graft.maven.org.springframework.samples.petclinic.customers.model.Customers;
import graft.maven.org.springframework.samples.petclinic.customers.model.OwnerDto;
import graft.maven.petclinic_customers.GraftConfig;

@Service
class VisitsService {

	private static final String DEFAULT_CUSTOMERS_GRAFT_HOST = "ws://localhost:8092/ws";

	private static volatile boolean customersGraftConfigured;

	private final VisitRepository visits;

	VisitsService(VisitRepository visits) {
		this.visits = visits;
	}

	@Transactional(readOnly = true)
	VisitLine[] read(int petId) {
		configureCustomersGraft();
		List<Visit> stored = this.visits.findByPetId(petId);
		OwnerDto owner = Customers.getOwnerForPet(petId);
		VisitLine[] lines = new VisitLine[stored.size()];
		for (int i = 0; i < stored.size(); i++) {
			Visit visit = stored.get(i);
			lines[i] = new VisitLine(VisitDates.iso(visit.getDate()), visit.getDescription(), owner.getFirstName(),
					owner.getLastName());
		}
		return lines;
	}

	private static void configureCustomersGraft() {
		if (customersGraftConfigured) {
			return;
		}
		synchronized (VisitsService.class) {
			if (customersGraftConfigured) {
				return;
			}
			String host = System.getenv("CUSTOMERS_GRAFT_HOST");
			GraftConfig.host = (host != null && !host.isBlank()) ? host.trim() : DEFAULT_CUSTOMERS_GRAFT_HOST;
			GraftConfig.stateless = false;
			customersGraftConfigured = true;
		}
	}

}
