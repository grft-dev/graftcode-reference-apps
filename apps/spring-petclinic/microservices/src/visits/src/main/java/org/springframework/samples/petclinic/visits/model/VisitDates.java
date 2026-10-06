package org.springframework.samples.petclinic.visits.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

final class VisitDates {

	private VisitDates() {
	}

	static String iso(Date date) {
		if (date instanceof java.sql.Date sqlDate) {
			return sqlDate.toLocalDate().toString();
		}
		LocalDate day = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return day.toString();
	}

}
