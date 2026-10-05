package org.springframework.samples.petclinic.owner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = ClinicSpringBoot.class)
class ClinicServiceTest {

	@Autowired
	private ClinicService clinic;

	@Test
	void firstOwnerIsGeorgeFranklin() {
		OwnerDto owner = this.clinic.getOwner(1);
		assertThat(owner.getFirstName()).isEqualTo("George");
		assertThat(owner.getLastName()).isEqualTo("Franklin");
	}

	@Test
	void georgeHasLeoTheCat() {
		PetDto[] pets = this.clinic.getPets(1);
		assertThat(pets).hasSize(1);
		assertThat(pets[0].getName()).isEqualTo("Leo");
		assertThat(pets[0].getType()).isEqualTo("cat");
		assertThat(pets[0].getBirthDate()).isEqualTo("2010-09-07");
	}

	@Test
	void seededVisitIsSamanthasRabiesShot() {
		VisitDto[] visits = this.clinic.getVisits(6, 7);
		assertThat(visits).extracting(VisitDto::getDescription).contains("rabies shot");
		assertThat(visits).extracting(VisitDto::getDate).contains("2013-01-01");
	}

	@Test
	void addVisitDelegatesToOwner() {
		VisitDto created = this.clinic.addVisit(1, 1, "2026-10-05", "graft checkup");
		assertThat(created.getDescription()).isEqualTo("graft checkup");
		assertThat(created.getDate()).isEqualTo("2026-10-05");
		assertThat(this.clinic.getVisits(1, 1)).extracting(VisitDto::getDescription).contains("graft checkup");
	}

	@Test
	void missingOwnerKeepsTheMessage() {
		assertThatThrownBy(() -> this.clinic.getOwner(999)).isInstanceOf(ClinicException.class)
			.hasMessage("Owner with id 999 not found.");
	}

}
