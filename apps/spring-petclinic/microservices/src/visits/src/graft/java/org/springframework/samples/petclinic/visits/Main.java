package org.springframework.samples.petclinic.visits;

import org.springframework.samples.petclinic.visits.model.Visits;

import graft.maven.petclinic_customers.GraftConfig;

public final class Main {

	private Main() {
	}

	public static void main(String[] args) {
		GraftConfig.host = "ws://localhost:8092/ws";
		GraftConfig.stateless = true;

		VisitLine[] visits = Visits.read(7);
		for (VisitLine visit : visits) {
			if ("2013-01-01".equals(visit.getDate()) && "rabies shot".equals(visit.getDescription())) {
				System.out.println("Visit: " + visit.getDate() + " " + visit.getDescription() + ", owner "
						+ visit.getOwnerFirstName() + " " + visit.getOwnerLastName());
			}
		}

		System.exit(0);
	}

}
