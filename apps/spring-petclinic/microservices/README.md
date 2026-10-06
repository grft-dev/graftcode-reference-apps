# Spring Petclinic Microservices — Graftcode

Original project:
https://github.com/spring-petclinic/spring-petclinic-microservices

Sources for the customers, vets, and visits slices are taken from commit `dc9ca15a71189bc4be6c02518d7180232c0555b7` (Apache-2.0). The license is in [`src/LICENSE.txt`](src/LICENSE.txt).

The customers and vets hosts are in this folder. Visits is a separate process that reads its own database and calls the customers graft. That call does not complete. The failure is recorded in `petclinic-microservices-graftcode-blockers.md` at the workspace root.

## What was changed

- Kept the customers, vets, and visits entities, Spring Data repositories, and the HSQLDB `schema.sql` / `data.sql`
- Added static `Customers` and `Vets` facades that delegate to those repositories
- Hosted those two JARs with Graftcode Gateway
- Moved the owner lookup that the UI used to do over HTTP into `Visits.read`, which calls the generated customers graft

Spring Cloud (discovery, configuration, and the edge gateway), the Vue UI, admin, and GenAI stay in the original project.

The original `VisitResource` does not call customers. It returns visits by pet id. The UI asks customers and visits separately and joins the answers. In this sample that join is the visit read.

| Application location | Before | With Graftcode | Developer responsibility removed |
| --- | --- | --- | --- |
| Visit with the owner's name | UI calls customers and visits over HTTP and joins them | `Visits.read` calls `Customers.getOwnerForPet` | The two HTTP routes, a handwritten client, and the join |

Domain types are package-private. Gateway publishes every public type. The running customers gateway enabled only `Customers` and `OwnerDto`. The running vets gateway enabled only `Vets` and `VetDto`.

## Original architecture

The same clinic data is split across Spring Cloud services. Customers owns owners and pets. Visits owns visit rows keyed by pet id. Vets owns veterinarians. Config, discovery, and the API gateway sit in front of them. The default database scripts are HSQLDB.

## Graftcode architecture

`src/customers` and `src/vets` are the hosts. Each gateway serves calls on container port 80 and Graftcode Vision. HSQLDB stays inside that process; there is no separate database container. `src/visits` is a separate Maven project. It keeps the visits database and calls the customers graft when a visit is read. Nothing in this sample calls vets through a graft, because the original visit path does not either.

On each host, `gg` is the process. The first `Customers` or `Vets` call starts the Spring context and uses the existing repository. There is no `spring-boot-starter-web`.

The graft surface is static and synchronous. Outside the customers host the types are `int`, `String`, and `OwnerDto`. `Date` stays on the visit entity. A visit date printed by the reader is an ISO-8601 `String`.

Seed checked against the copied `data.sql` files:

- owner `6` is Jean Coleman
- her pet `7` is Samantha, a cat
- the visit for pet `7` dated `2013-01-01` is `rabies shot`
- vet `1` is James Carter

## Run

Prerequisites:

- [Docker](https://docs.docker.com/get-docker/) with Docker Compose
- [JDK 17](https://adoptium.net/) and [Maven](https://maven.apache.org/download.cgi) for visits

From `apps/spring-petclinic/microservices/src`:

```bash
docker compose up --build -d
```

Compose publishes port 8092 for customers calls (`ws://localhost:8092/ws`) and port 8093. Vets uses 8094 and 8095, so this sample can run beside the monolith on 8090. The gateway build used while writing this sample (gg 1.4.9) logs Graftcode Vision on container port 80. With the mapping above that is [http://localhost:8092](http://localhost:8092) for customers and [http://localhost:8094](http://localhost:8094) for vets. If `docker compose logs customers` prints a different Vision URL, use that one.

Wait until the customers install snippet is available:

```bash
curl http://localhost:8092/maven
```

You should see `Customers` and `OwnerDto` in Vision. The registry GUID changes every time the container starts. Copy the current repository URL from `http://localhost:8092/maven`. Do not reuse a GUID from an earlier run.

In-process Spring tests do not go through the graft:

```bash
mvn test
```

Run that in `src/customers`, `src/vets`, and `src/visits`. The visits command, without `-Dgraft.guid`, runs only `VisitRepositoryTest`.

## Try it

The feed at `grft.dev` serves grafts only. [`src/visits/pom.xml`](src/visits/pom.xml) reads the registry GUID from Maven property `graft.guid`. Passing `-Dgraft.guid` turns on the `graft` profile, which compiles `Visits.read`, `Main`, and `VisitEndToEndTest` against the generated client.

From `src/visits`:

```bash
mvn -q compile exec:java -Dgraft.guid=YOUR_GUID
```

`src/visits/src/graft/java/org/springframework/samples/petclinic/visits/Main.java` points the graft at the local customers gateway and reads the seeded visit:

```java
GraftConfig.host = "ws://localhost:8092/ws";
GraftConfig.stateless = true;

VisitLine[] visits = Visits.read(7);
```

`Visits.read` loads the visit rows from HSQLDB and then calls the generated graft:

```java
OwnerDto owner = Customers.getOwnerForPet(petId);
```

Expected output once a call can complete:

```text
Visit: 2013-01-01 rabies shot, owner Jean Coleman
```

That output is not what the current gateway returns. On JDK 17 the generated client throws before the call. The failure is the same first product blocker as the monolith. The host JAR was not added to the visits classpath, and this run never reached the customers process.

`src/run-e2e.ps1` starts Compose, reads the GUID, and runs `mvn test -Dgraft.guid=...` in `visits`. `VisitRepositoryTest` passes. `VisitEndToEndTest` is the cross-boundary path and fails for the reason above.

Stop the containers with `docker compose down`.

## Graft

```text
Customers.getOwner(...)
Customers.getOwnerForPet(...)
Vets.getVet(...)
```

Imports and `GraftConfig` come from the graft. For this module they were:

```text
graft.maven.org.springframework.samples.petclinic.customers.model.Customers
graft.maven.org.springframework.samples.petclinic.customers.model.OwnerDto
graft.maven.petclinic_customers.GraftConfig
```

Copy the current snippet from Vision if the package differs.
