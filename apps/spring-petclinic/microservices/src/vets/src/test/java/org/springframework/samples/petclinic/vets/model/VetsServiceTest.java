package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = VetsSpringBoot.class)
class VetsServiceTest {

	@Autowired
	private VetsService vets;

	@Test
	void firstVetIsJamesCarter() {
		VetDto vet = this.vets.getVet(1);
		assertThat(vet.getFirstName()).isEqualTo("James");
		assertThat(vet.getLastName()).isEqualTo("Carter");
	}

	@Test
	void missingVetKeepsTheMessage() {
		assertThatThrownBy(() -> this.vets.getVet(999)).isInstanceOf(VetsException.class)
			.hasMessage("Vet with id 999 not found.");
	}

}
