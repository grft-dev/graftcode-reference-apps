package org.springframework.samples.petclinic.visits;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.visits.model.Visits;

import graft.maven.petclinic_customers.GraftConfig;

import static org.assertj.core.api.Assertions.assertThat;

class VisitEndToEndTest {

	@BeforeAll
	static void pointAtGateway() {
		GraftConfig.host = "ws://localhost:8092/ws";
		GraftConfig.stateless = true;
	}

	@Test
	void seededVisitNamesJeanColeman() {
		VisitLine[] visits = Visits.read(7);
		VisitLine rabies = null;
		for (VisitLine visit : visits) {
			if ("2013-01-01".equals(visit.getDate()) && "rabies shot".equals(visit.getDescription())) {
				rabies = visit;
			}
		}
		assertThat(rabies).isNotNull();
		assertThat(rabies.getOwnerFirstName()).isEqualTo("Jean");
		assertThat(rabies.getOwnerLastName()).isEqualTo("Coleman");
	}

}
