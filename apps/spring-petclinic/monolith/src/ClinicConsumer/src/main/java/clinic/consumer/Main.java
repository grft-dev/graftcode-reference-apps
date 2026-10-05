package clinic.consumer;

import graft.maven.org.springframework.samples.petclinic.owner.Clinic;
import graft.maven.org.springframework.samples.petclinic.owner.OwnerDto;
import graft.maven.org.springframework.samples.petclinic.owner.PetDto;
import graft.maven.org.springframework.samples.petclinic.owner.VisitDto;
import graft.maven.petclinic_clinic.GraftConfig;

public final class Main {

	private Main() {
	}

	public static void main(String[] args) {
		GraftConfig.host = "ws://localhost:8090/ws";
		GraftConfig.stateless = true;

		OwnerDto owner = Clinic.getOwner(1);
		System.out.println("Getting owner 1: " + owner.getFirstName() + " " + owner.getLastName());

		PetDto[] pets = Clinic.getPets(1);
		System.out.println("Pet: " + pets[0].getName() + " the " + pets[0].getType());

		VisitDto[] seeded = Clinic.getVisits(6, 7);
		System.out.println("Visit: " + seeded[0].getDate() + " " + seeded[0].getDescription());

		try {
			Clinic.getOwner(999);
		}
		catch (Exception ex) {
			System.out.println("Getting owner 999: " + ex.getMessage());
		}

		System.exit(0);
	}

}
