package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = CustomersSpringBoot.class)
class CustomersServiceTest {

	@Autowired
	private CustomersService customers;

	@Test
	void ownerSixIsJeanColeman() {
		OwnerDto owner = this.customers.getOwner(6);
		assertThat(owner.getFirstName()).isEqualTo("Jean");
		assertThat(owner.getLastName()).isEqualTo("Coleman");
	}

	@Test
	void petSevenBelongsToJeanColeman() {
		OwnerDto owner = this.customers.getOwnerForPet(7);
		assertThat(owner.getFirstName()).isEqualTo("Jean");
		assertThat(owner.getLastName()).isEqualTo("Coleman");
		assertThat(owner.getId()).isEqualTo(6);
	}

	@Test
	void missingOwnerKeepsTheMessage() {
		assertThatThrownBy(() -> this.customers.getOwner(999)).isInstanceOf(CustomersException.class)
			.hasMessage("Owner with id 999 not found.");
	}

	@Test
	void missingPetKeepsTheMessage() {
		assertThatThrownBy(() -> this.customers.getOwnerForPet(999)).isInstanceOf(CustomersException.class)
			.hasMessage("Pet with id 999 not found.");
	}

}
