package org.springframework.samples.petclinic.visits.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.springframework.samples.petclinic.visits.VisitLine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import graft.maven.org.springframework.samples.petclinic.customers.model.Customers;
import graft.maven.org.springframework.samples.petclinic.customers.model.OwnerDto;
import graft.maven.petclinic_customers.GraftConfig;

@Service
class VisitsService {

	private static final String DEFAULT_CUSTOMERS_GRAFT_HOST = "ws://localhost:8092/ws";

	private static volatile boolean customersGraftConfigured;

	private final VisitRepository visits;

	VisitsService(VisitRepository visits) {
		this.visits = visits;
	}

	@Transactional(readOnly = true)
	VisitLine[] read(int petId) {
		configureCustomersGraft();
		List<Visit> stored = this.visits.findByPetId(petId);
		OwnerDto owner = Customers.getOwnerForPet(petId);
		VisitLine[] lines = new VisitLine[stored.size()];
		for (int i = 0; i < stored.size(); i++) {
			Visit visit = stored.get(i);
			lines[i] = new VisitLine(VisitDates.iso(visit.getDate()), visit.getDescription(), owner.getFirstName(),
					owner.getLastName());
		}
		return lines;
	}

	private static void configureCustomersGraft() {
		if (customersGraftConfigured) {
			return;
		}
		synchronized (VisitsService.class) {
			if (customersGraftConfigured) {
				return;
			}
			ensureCustomersModuleOnJavaClasspath();
			String host = System.getenv("CUSTOMERS_GRAFT_HOST");
			host = (host != null && !host.isBlank()) ? host.trim() : DEFAULT_CUSTOMERS_GRAFT_HOST;
			GraftConfig.host = host;
			GraftConfig.stateless = false;
			GraftConfig.setConfig(
					"name=graft.maven.petclinic_customers; runtime=jvm; host=" + host + "; stateless=false");
			customersGraftConfigured = true;
		}
	}

	/**
	 * gg 1.4.9 hosts JVM with a minimal {@code java.class.path}; graft SDK still validates
	 * {@code {user.dir}/resources/maven/.../petclinic-customers} on that property (JDK 17+).
	 */
	private static void ensureCustomersModuleOnJavaClasspath() {
		Path moduleDir = Paths.get(System.getProperty("user.dir"), "resources", "maven",
				"org.springframework.samples", "petclinic-customers");
		try {
			Files.createDirectories(moduleDir);
		}
		catch (IOException ignored) {
			// SDK may create the tree during GraftConfig.init()
		}
		String absolute = moduleDir.toAbsolutePath().normalize().toString();
		String classpath = System.getProperty("java.class.path", "");
		boolean present = Arrays.stream(classpath.split(File.pathSeparator))
			.filter(entry -> !entry.isBlank())
			.anyMatch(entry -> absolute.equals(Paths.get(entry).toAbsolutePath().normalize().toString()));
		if (!present) {
			System.setProperty("java.class.path",
					classpath.isEmpty() ? absolute : classpath + File.pathSeparator + absolute);
		}
	}

}
