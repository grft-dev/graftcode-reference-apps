package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ClinicService {

	private final OwnerRepository owners;

	ClinicService(OwnerRepository owners) {
		this.owners = owners;
	}

	@Transactional(readOnly = true)
	OwnerDto[] listOwners() {
		return this.owners.findAll(Sort.by("id")).stream().map(ClinicService::toOwner).toArray(OwnerDto[]::new);
	}

	@Transactional(readOnly = true)
	OwnerDto getOwner(int id) {
		return toOwner(requireOwner(id));
	}

	@Transactional(readOnly = true)
	PetDto[] getPets(int ownerId) {
		return requireOwner(ownerId).getPets().stream().map(ClinicService::toPet).toArray(PetDto[]::new);
	}

	@Transactional(readOnly = true)
	VisitDto[] getVisits(int ownerId, int petId) {
		return requirePet(ownerId, petId).getVisits().stream().map(ClinicService::toVisit).toArray(VisitDto[]::new);
	}

	@Transactional
	VisitDto addVisit(int ownerId, int petId, String date, String description) {
		Owner owner = requireOwner(ownerId);
		Visit visit = new Visit();
		visit.setDate(LocalDate.parse(date));
		visit.setDescription(description);
		owner.addVisit(petId, visit);
		this.owners.saveAndFlush(owner);
		Visit stored = requirePet(ownerId, petId).getVisits()
			.stream()
			.filter(item -> visit.getDate().equals(item.getDate()) && description.equals(item.getDescription()))
			.reduce((first, second) -> second)
			.orElseThrow(() -> new ClinicException("Visit was not stored for pet " + petId + "."));
		return toVisit(stored);
	}

	private Owner requireOwner(int id) {
		return this.owners.findById(id)
			.orElseThrow(() -> new ClinicException("Owner with id " + id + " not found."));
	}

	private Pet requirePet(int ownerId, int petId) {
		Owner owner = requireOwner(ownerId);
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new ClinicException("Pet with id " + petId + " not found for owner " + ownerId + ".");
		}
		return pet;
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

	private static PetDto toPet(Pet pet) {
		PetDto dto = new PetDto();
		dto.setId(pet.getId());
		dto.setName(pet.getName());
		dto.setBirthDate(pet.getBirthDate().toString());
		dto.setType(pet.getType().getName());
		return dto;
	}

	private static VisitDto toVisit(Visit visit) {
		VisitDto dto = new VisitDto();
		dto.setId(visit.getId());
		dto.setDate(visit.getDate().toString());
		dto.setDescription(visit.getDescription());
		return dto;
	}

}
