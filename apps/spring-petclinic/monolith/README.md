# Spring Petclinic — Graftcode

Original project:
https://github.com/spring-projects/spring-petclinic

Sources for the owner, pet, and visit slice are taken from commit `500158f732419217507c7656904b8e6aa1bcc0d6` (Apache-2.0). The license is in [`src/ClinicService/LICENSE.txt`](src/ClinicService/LICENSE.txt).

The host and the web consumer are in this folder. A call through Graftcode Gateway may not complete end to end. Failures are recorded in `petclinic-monolith-graftcode-blockers.md` at the workspace root.

## What was changed

- Kept the Petclinic owner, pet, and visit classes, Spring Data repositories, and the H2 `schema.sql` / `data.sql`
- Collapsed the graft entry point into one `@Service` class, `Clinic`: static methods for the gateway, instance methods and repositories for Spring (no separate facade class)
- Hosted that JAR with Graftcode Gateway
- Added `ClinicWeb`, a small Vite app that installs the npm graft and shows graft call results in the browser

Vets, Thymeleaf, and the MVC controllers stay in the original project. They are not in the hosted JAR.

| Application location | Before | With Graftcode | Developer responsibility removed |
| --- | --- | --- | --- |
| Owner, pet, and visit UI | Spring MVC controller and Thymeleaf form | `Clinic.getOwner`, `getPets`, `getVisits`, `addVisit` via npm graft | HTTP routes, form binding, and a handwritten REST client |

`BaseEntity`, `NamedEntity`, and `Person` sit in the `owner` package, and the domain types are package-private. Gateway publishes every public type. The running gateway should enable only `Clinic`, `OwnerDto`, `PetDto`, and `VisitDto`.

## Original architecture

Spring Petclinic is one Java process. Owners, pets, visits, and vets share one application, one relational database, and a server-rendered UI. The default database is H2, loaded from `db/h2/schema.sql` and `db/h2/data.sql`.

## Graftcode architecture

`src/ClinicService` is the host. Graftcode Gateway serves calls on container port 80 and Graftcode Vision. H2 stays inside that process; there is no separate database container. `src/ClinicWeb` is a Node project that installs the generated npm graft and calls it from the browser.

On the host, `gg` is the process. The first `Clinic` call starts the Spring context and uses the existing `OwnerRepository`. There is no `spring-boot-starter-web`.

The graft surface is static and synchronous. Outside the host the types are `String`, `int`, the three DTOs, and arrays. `List`, `Optional`, `LocalDate`, and Spring types stay on the entities. A visit date on `VisitDto` is an ISO-8601 `String` because `LocalDate` is not a graft surface type. The entity still stores `LocalDate`.

Seed checked against `data.sql`:

- owner `1` is George Franklin
- his pet is Leo, a cat, born `2010-09-07`; that pet has no visit in the seed
- the first visit row is pet `7` (Samantha, owner `6`, Jean Coleman): `2013-01-01`, `rabies shot`

## Run

Prerequisites:

- [Docker](https://docs.docker.com/get-docker/) with Docker Compose
- [Node.js](https://nodejs.org/) for `ClinicWeb`
- [JDK 17](https://adoptium.net/) and [Maven](https://maven.apache.org/download.cgi) only for in-process host tests

From `apps/spring-petclinic/monolith/src`:

```bash
docker compose up --build -d
```

Compose publishes port 8090 for calls (`ws://localhost:8090/ws`) and port 8091. The gateway build used while writing this sample (gg 1.4.9) serves Graftcode Vision on the call port: [http://localhost:8090/GV](http://localhost:8090/GV). If `docker compose logs backend` prints a different Vision URL, use that one.

Wait until the install snippet is available:

```bash
curl http://localhost:8090/npm
```

You should see `Clinic`, `OwnerDto`, `PetDto`, and `VisitDto` in Vision. The registry GUID changes every time the container starts. Copy the current install command from `http://localhost:8090/npm` or Vision. Do not reuse a GUID from an earlier run.

In-process host tests (`mvn test` in `src/ClinicService`) exercise Spring and H2 directly. They do not go through the graft.

## Try it

The feed at `grft.dev` serves grafts only. Copy the npm install command from Vision (package name follows Maven coordinates, typically `@graft/maven-groupid_org-springframework-samples_artifactid_petclinic-clinic@1.0.0`).

From `src/ClinicWeb`, point npm at the current registry (replace `YOUR_GUID`):

```bash
echo '@graft:registry=https://grft.dev/YOUR_GUID__free' > .npmrc
npm install
npm run dev
```

Open the URL Vite prints (typically [http://localhost:5173](http://localhost:5173)). [`src/ClinicWeb/src/main.js`](src/ClinicWeb/src/main.js) calls [`configureGraft()`](src/ClinicWeb/src/graftSetup.js) and runs the owner, pet, and visit scenario. Graft setup (host and `stateless = false`) lives in [`src/ClinicWeb/src/graftSetup.js`](src/ClinicWeb/src/graftSetup.js):

```javascript
import { configureGraft } from "./graftSetup.js";
import { Clinic } from "@graft/maven-groupid_org-springframework-samples_artifactid_petclinic-clinic";

configureGraft(); // GraftConfig.host + GraftConfig.stateless = false

const owner = await Clinic.getOwner(1);
```

Expected output once calls complete:

```text
Getting owner 1: George Franklin
Pet: Leo the cat
Visit: 2013-01-01 rabies shot
Getting owner 999: Owner with id 999 not found.
```

When the gateway and host are up, the full output above should match, including pet and visit lines (`getPets` / `getVisits`). Verified 2026-10-12: Vitest e2e in `ClinicWeb` passes 5/5 with **`GraftConfig.stateless = false`** (see `petclinic-graftcode-defects.md` §4). Do not use `stateless = true` for this sample — DTO getters fail (defect #3).

The host includes a **documented sample workaround** for gg binding `NOPLoggerFactory` before Spring Boot starts (`preferLogbackOverGatewayNop()` in `Clinic.service()`). That is not a product fix; see `petclinic-monolith-graftcode-blockers.md` §2.

This sample sets **`GraftConfig.stateless = false`** on purpose (workaround for defect #3 — DTO getters with `stateless: true`). Array returns (`PetDto[]` / `VisitDto[]`) work through the **npm** graft in stateful mode; the JVM graft client may still hit defect #4. See `petclinic-monolith-graftcode-blockers.md` and `petclinic-graftcode-defects.md`.

Cross-boundary tests: `src/run-e2e.ps1` or `src/run-e2e.sh` starts Compose, writes `ClinicWeb/.npmrc` from `http://localhost:8090/npm`, and runs `npm test` (Vitest through the graft).

Stop the containers with `docker compose down`.

## Graft

```text
Clinic.listOwners()
Clinic.getOwner(...)
Clinic.getPets(...)
Clinic.getVisits(...)
Clinic.addVisit(...)
```

Imports and `GraftConfig` come from the npm graft. Copy the current snippet from Vision if the package name differs. The npm graft uses **camelCase** method names.
