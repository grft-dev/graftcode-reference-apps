package org.springframework.samples.petclinic.customers.model;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CustomersService {

	private final OwnerRepository owners;

	private final PetRepository pets;

	CustomersService(OwnerRepository owners, PetRepository pets) {
		this.owners = owners;
		this.pets = pets;
	}

	@Transactional(readOnly = true)
	OwnerDto getOwner(int id) {
		return toOwner(requireOwner(id));
	}

	@Transactional(readOnly = true)
	OwnerDto getOwnerForPet(int petId) {
		Pet pet = this.pets.findById(petId)
			.orElseThrow(() -> new CustomersException("Pet with id " + petId + " not found."));
		Owner owner = pet.getOwner();
		if (owner == null) {
			throw new CustomersException("Owner for pet " + petId + " not found.");
		}
		return toOwner(owner);
	}

	private Owner requireOwner(int id) {
		return this.owners.findById(id)
			.orElseThrow(() -> new CustomersException("Owner with id " + id + " not found."));
	}

	private static OwnerDto toOwner(Owner owner) {
		OwnerDto dto = new OwnerDto();
		dto.setId(owner.getId());
		dto.setFirstName(owner.getFirstName());
		dto.setLastName(owner.getLastName());
		dto.setAddress(owner.getAddress());
		dto.setCity(owner.getCity());
		dto.setTelephone(owner.getTelephone());
		return dto;
	}

}
