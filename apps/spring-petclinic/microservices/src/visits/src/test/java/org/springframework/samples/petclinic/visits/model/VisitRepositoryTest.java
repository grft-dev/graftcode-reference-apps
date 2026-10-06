package org.springframework.samples.petclinic.visits.model;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.samples.petclinic.visits.VisitsSpringBoot;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = VisitsSpringBoot.class)
class VisitRepositoryTest {

	@Autowired
	private VisitRepository visits;

	@Test
	void seededVisitIsRabiesShot() {
		List<Visit> found = this.visits.findByPetId(7);
		assertThat(found).anySatisfy(visit -> {
			assertThat(visit.getDescription()).isEqualTo("rabies shot");
			assertThat(VisitDates.iso(visit.getDate())).isEqualTo("2013-01-01");
		});
	}

}
