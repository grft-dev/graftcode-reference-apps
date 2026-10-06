package org.springframework.samples.petclinic.vets.model;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class VetsService {

	private final VetRepository vets;

	VetsService(VetRepository vets) {
		this.vets = vets;
	}

	@Transactional(readOnly = true)
	VetDto getVet(int id) {
		Vet vet = this.vets.findById(id)
			.orElseThrow(() -> new VetsException("Vet with id " + id + " not found."));
		VetDto dto = new VetDto();
		dto.setId(vet.getId());
		dto.setFirstName(vet.getFirstName());
		dto.setLastName(vet.getLastName());
		return dto;
	}

}
