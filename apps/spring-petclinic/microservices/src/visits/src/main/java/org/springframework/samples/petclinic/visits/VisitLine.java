package org.springframework.samples.petclinic.visits;

public final class VisitLine {

	private final String date;

	private final String description;

	private final String ownerFirstName;

	private final String ownerLastName;

	public VisitLine(String date, String description, String ownerFirstName, String ownerLastName) {
		this.date = date;
		this.description = description;
		this.ownerFirstName = ownerFirstName;
		this.ownerLastName = ownerLastName;
	}

	public String getDate() {
		return this.date;
	}

	public String getDescription() {
		return this.description;
	}

	public String getOwnerFirstName() {
		return this.ownerFirstName;
	}

	public String getOwnerLastName() {
		return this.ownerLastName;
	}

}
