# Spring Petclinic — Graftcode

Original project:
https://github.com/spring-projects/spring-petclinic

Sources for the owner, pet, and visit slice are taken from commit `500158f732419217507c7656904b8e6aa1bcc0d6` (Apache-2.0). The license is in [`src/ClinicService/LICENSE.txt`](src/ClinicService/LICENSE.txt).

The host and the consumer are in this folder. A call through Graftcode Gateway does not complete. The failure is recorded in `petclinic-monolith-graftcode-blockers.md` at the workspace root.

## What was changed

- Kept the Petclinic owner, pet, and visit classes, Spring Data repositories, and the H2 `schema.sql` / `data.sql`
- Added a static `Clinic` facade that delegates to those repositories and to `Owner.addVisit`
- Hosted that JAR with Graftcode Gateway
- Added `ClinicConsumer`, which calls the generated graft

Vets, Thymeleaf, and the MVC controllers stay in the original project. They are not in the hosted JAR.

| Application location | Before | With Graftcode | Developer responsibility removed |
| --- | --- | --- | --- |
| Owner, pet, and visit UI | Spring MVC controller and Thymeleaf form | `Clinic.getOwner`, `getPets`, `getVisits`, `addVisit` | HTTP routes, form binding, and a handwritten client |

`BaseEntity`, `NamedEntity`, and `Person` sit in the `owner` package, and the domain types are package-private. Gateway publishes every public type. The running gateway enabled only `Clinic`, `OwnerDto`, `PetDto`, and `VisitDto`.

## Original architecture

Spring Petclinic is one Java process. Owners, pets, visits, and vets share one application, one relational database, and a server-rendered UI. The default database is H2, loaded from `db/h2/schema.sql` and `db/h2/data.sql`.

## Graftcode architecture

`src/ClinicService` is the host. Graftcode Gateway serves calls on container port 80 and Graftcode Vision. H2 stays inside that process; there is no separate database container. `src/ClinicConsumer` is a separate Maven project that installs the graft and calls it.

On the host, `gg` is the process. The first `Clinic` call starts the Spring context and uses the existing `OwnerRepository`. There is no `spring-boot-starter-web`.

The graft surface is static and synchronous. Outside the host the types are `String`, `int`, the three DTOs, and arrays. `List`, `Optional`, `LocalDate`, and Spring types stay on the entities. A visit date on `VisitDto` is an ISO-8601 `String` because `LocalDate` is not a graft surface type. The entity still stores `LocalDate`.

Seed checked against `data.sql`:

- owner `1` is George Franklin
- his pet is Leo, a cat, born `2010-09-07`; that pet has no visit in the seed
- the first visit row is pet `7` (Samantha, owner `6`, Jean Coleman): `2013-01-01`, `rabies shot`

## Run

Prerequisites:

- [Docker](https://docs.docker.com/get-docker/) with Docker Compose
- [JDK 17](https://adoptium.net/) and [Maven](https://maven.apache.org/download.cgi) for the consumer

From `apps/spring-petclinic/monolith/src`:

```bash
docker compose up --build -d
```

Compose publishes port 8090 for calls (`ws://localhost:8090/ws`) and port 8091. The gateway build used while writing this sample (gg 1.4.9) serves Graftcode Vision on the call port: [http://localhost:8090/GV](http://localhost:8090/GV). If `docker compose logs backend` prints a different Vision URL, use that one.

Wait until the install snippet is available:

```bash
curl http://localhost:8090/maven
```

You should see `Clinic`, `OwnerDto`, `PetDto`, and `VisitDto` in Vision. The registry GUID changes every time the container starts. Copy the current repository URL from `http://localhost:8090/maven`. Do not reuse a GUID from an earlier run.

`ClinicService` also has an in-process Spring test (`mvn test` in `src/ClinicService`). That test does not go through the graft.

## Try it

The feed at `grft.dev` serves grafts only. [`src/ClinicConsumer/pom.xml`](src/ClinicConsumer/pom.xml) reads the registry GUID from Maven property `graft.guid` (default `YOUR_GUID`). Replace it with the GUID from the install snippet.

From `src/ClinicConsumer`:

```bash
mvn -q compile exec:java -Dgraft.guid=YOUR_GUID
```

`src/ClinicConsumer/src/main/java/clinic/consumer/Main.java` points the graft at the local gateway and reads the seeded owner, pet, and visit:

```java
GraftConfig.host = "ws://localhost:8090/ws";
GraftConfig.stateless = true;

OwnerDto owner = Clinic.getOwner(1);
System.out.println("Getting owner 1: " + owner.getFirstName() + " " + owner.getLastName());
```

Expected output once a call can complete:

```text
Getting owner 1: George Franklin
Pet: Leo the cat
Visit: 2013-01-01 rabies shot
Getting owner 999: Owner with id 999 not found.
```

That output is not what the current gateway returns. On JDK 17 the generated client throws before the call unless the host JAR is also on the consumer classpath. After that JAR is added, the gateway runs `Clinic.getOwner` and Spring Boot fails while starting. Both failures are product blockers; the application was not rewritten to get past them.

`src/run-e2e.ps1` starts Compose, reads the GUID, and runs `ClinicConsumer` tests. Those tests are the cross-boundary path. They fail for the same reason.

Stop the containers with `docker compose down`.

## Graft

```text
Clinic.listOwners()
Clinic.getOwner(...)
Clinic.getPets(...)
Clinic.getVisits(...)
Clinic.addVisit(...)
```

Imports and `GraftConfig` come from the graft. For this module they were:

```text
graft.maven.org.springframework.samples.petclinic.owner.Clinic
graft.maven.petclinic_clinic.GraftConfig
```

Copy the current snippet from Vision if the package differs.
