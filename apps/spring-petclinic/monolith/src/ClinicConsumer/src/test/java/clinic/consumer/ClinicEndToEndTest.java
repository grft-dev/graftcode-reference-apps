package clinic.consumer;

import graft.maven.org.springframework.samples.petclinic.owner.Clinic;
import graft.maven.org.springframework.samples.petclinic.owner.OwnerDto;
import graft.maven.org.springframework.samples.petclinic.owner.PetDto;
import graft.maven.org.springframework.samples.petclinic.owner.VisitDto;
import graft.maven.petclinic_clinic.GraftConfig;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ClinicEndToEndTest {

	@BeforeAll
	static void pointAtGateway() {
		GraftConfig.host = "ws://localhost:8090/ws";
		GraftConfig.stateless = true;
	}

	@Test
	void firstOwnerIsGeorge() {
		OwnerDto owner = Clinic.getOwner(1);
		assertEquals("George", owner.getFirstName());
		assertEquals("Franklin", owner.getLastName());
	}

	@Test
	void georgeHasLeoTheCat() {
		PetDto[] pets = Clinic.getPets(1);
		assertEquals(1, pets.length);
		assertEquals("Leo", pets[0].getName());
		assertEquals("cat", pets[0].getType());
		assertEquals("2010-09-07", pets[0].getBirthDate());
	}

	@Test
	void seededVisitIsSamanthasRabiesShot() {
		VisitDto[] visits = Clinic.getVisits(6, 7);
		assertTrue(containsDescription(visits, "rabies shot"));
		assertTrue(containsDate(visits, "2013-01-01"));
	}

	@Test
	void addVisitIsVisibleThroughTheGraft() {
		VisitDto created = Clinic.addVisit(1, 1, "2026-10-05", "graft checkup");
		assertEquals("graft checkup", created.getDescription());
		assertEquals("2026-10-05", created.getDate());
		assertTrue(containsDescription(Clinic.getVisits(1, 1), "graft checkup"));
	}

	@Test
	void missingOwnerKeepsTheMessage() {
		try {
			Clinic.getOwner(999);
			fail("expected a missing owner to fail");
		}
		catch (Exception ex) {
			assertTrue(ex.getMessage().contains("Owner with id 999 not found."), ex.getMessage());
		}
	}

	private static boolean containsDescription(VisitDto[] visits, String description) {
		for (VisitDto visit : visits) {
			if (description.equals(visit.getDescription())) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsDate(VisitDto[] visits, String date) {
		for (VisitDto visit : visits) {
			if (date.equals(visit.getDate())) {
				return true;
			}
		}
		return false;
	}

}
