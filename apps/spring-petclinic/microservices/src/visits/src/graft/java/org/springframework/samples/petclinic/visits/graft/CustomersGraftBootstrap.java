package org.springframework.samples.petclinic.visits.graft;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import graft.maven.org.springframework.samples.petclinic.customers.model.Customers;
import graft.maven.org.springframework.samples.petclinic.customers.model.OwnerDto;
import graft.maven.petclinic_customers.GraftConfig;

/**
 * JVM graft client to the customers host. Initialization follows {@code JavaGCSample/Client}:
 * static block runs classpath setup and {@link GraftConfig} before any graft API use.
 */
public final class CustomersGraftBootstrap {

	private static final String DEFAULT_CUSTOMERS_GRAFT_HOST = "ws://localhost:8092/ws";

	private static String hostOverride;

	private static volatile boolean graftConfigured;

	static {
		ensureCustomersModuleOnJavaClasspath();
	}

	private CustomersGraftBootstrap() {
	}

	/**
	 * Override gateway host before {@link #ensureReady()} (e.g. in {@code @BeforeAll}). Must not run after
	 * this class has been initialized.
	 */
	public static void configureHostForTests(String host) {
		if (host == null || host.isBlank()) {
			throw new IllegalArgumentException("host must be non-blank");
		}
		hostOverride = host.trim();
	}

	public static void ensureReady() {
		configureGraftOnce();
	}

	public static OwnerDto getOwnerForPet(int petId) {
		configureGraftOnce();
		return Customers.getOwnerForPet(petId);
	}

	private static void configureGraftOnce() {
		if (graftConfigured) {
			return;
		}
		synchronized (CustomersGraftBootstrap.class) {
			if (graftConfigured) {
				return;
			}
			GraftConfig.host = resolveHost();
			GraftConfig.stateless = false;
			graftConfigured = true;
		}
	}

	private static String resolveHost() {
		if (hostOverride != null && !hostOverride.isBlank()) {
			return hostOverride;
		}
		String host = System.getenv("CUSTOMERS_GRAFT_HOST");
		if (host != null && !host.isBlank()) {
			return host.trim();
		}
		host = System.getenv("GRAFT_HOST");
		if (host != null && !host.isBlank()) {
			return host.trim();
		}
		return DEFAULT_CUSTOMERS_GRAFT_HOST;
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
