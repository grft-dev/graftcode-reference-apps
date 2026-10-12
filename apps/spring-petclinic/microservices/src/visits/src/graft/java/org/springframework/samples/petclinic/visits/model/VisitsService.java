package org.springframework.samples.petclinic.visits.model;

import java.util.List;

import org.springframework.samples.petclinic.visits.VisitLine;
import org.springframework.samples.petclinic.visits.graft.CustomersGraftBootstrap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import graft.maven.org.springframework.samples.petclinic.customers.model.OwnerDto;

@Service
class VisitsService {

	private final VisitRepository visits;

	VisitsService(VisitRepository visits) {
		this.visits = visits;
	}

	@Transactional(readOnly = true)
	VisitLine[] read(int petId) {
		List<Visit> stored = this.visits.findByPetId(petId);
		OwnerDto owner = CustomersGraftBootstrap.getOwnerForPet(petId);
		VisitLine[] lines = new VisitLine[stored.size()];
		for (int i = 0; i < stored.size(); i++) {
			Visit visit = stored.get(i);
			lines[i] = new VisitLine(VisitDates.iso(visit.getDate()), visit.getDescription(), owner.getFirstName(),
					owner.getLastName());
		}
		return lines;
	}

}
