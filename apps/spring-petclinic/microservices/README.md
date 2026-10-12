# Spring Petclinic Microservices — Graftcode

Original project:
https://github.com/spring-petclinic/spring-petclinic-microservices

Sources for the customers, vets, and visits slices are taken from commit `dc9ca15a71189bc4be6c02518d7180232c0555b7` (Apache-2.0). The license is in [`src/LICENSE.txt`](src/LICENSE.txt).

Three Java hosts (`customers`, `vets`, `visits`) and a Vite consumer (`ClinicWeb`) are in this folder. End-to-end verification (`run-e2e.ps1` / `run-e2e.sh`, **2026-10-12**): **`ClinicWeb` Vitest PASS** and **`VisitEndToEndTest` PASS** with **`GraftConfig.stateless = false`** and **`ws://`** hosts. Product limitations and sample workarounds are in `petclinic-microservices-graftcode-blockers.md` and `petclinic-graftcode-defects.md` at the workspace root.

## What was changed

- Kept the customers, vets, and visits entities, Spring Data repositories, and the HSQLDB `schema.sql` / `data.sql`
- Added static `Customers` and `Vets` facades that delegate to those repositories
- Hosted all three slices with Graftcode Gateway (`visits` calls the generated customers graft inside `Visits.read`)
- Added `ClinicWeb`, a small Vite app that installs the npm graft from the visits gateway and runs the visit scenario in the browser

Spring Cloud (discovery, configuration, and the edge gateway), the Vue UI, admin, and GenAI stay in the original project.

The original `VisitResource` does not call customers. It returns visits by pet id. The UI asked customers and visits separately and joined the answers. In this sample the join lives in `Visits.read` on the visits host; the browser calls only the visits graft.

| Application location | Before | With Graftcode | Developer responsibility removed |
| --- | --- | --- | --- |
| UI / consumer | Vue + HTTP to customers and visits + join in the browser | `ClinicWeb` → `Visits.read` | REST client, serialization, and join in JS |
| Visits → customers | HTTP in the original layout | `Customers.getOwnerForPet` (Java graft) | REST client and response mapping in visits |

Domain types are package-private. Gateway publishes every public type. Enable only the intended public types: `Customers`, `OwnerDto`; `Vets`, `VetDto`; `Visits`, `VisitLine`.

## Original architecture

The same clinic data is split across Spring Cloud services. Customers owns owners and pets. Visits owns visit rows keyed by pet id. Vets owns veterinarians. Config, discovery, and the API gateway sit in front of them. The default database scripts are HSQLDB.

## Graftcode architecture

`src/customers`, `src/vets`, and `src/visits` are the hosts. Each gateway serves calls on container port 80 and Graftcode Vision. HSQLDB stays inside each process; there is no separate database container. `src/ClinicWeb` installs the npm graft from the visits gateway.

On each host, `gg` is the process. The first graft call starts Spring (or loads the visits repository) and uses the existing repositories. There is no `spring-boot-starter-web`.

The visits host sets `CUSTOMERS_GRAFT_HOST` (default in Compose: `ws://customers/ws`) before calling the customers graft. The graft surface is static and synchronous.

Seed checked against the copied `data.sql` files:

- owner `6` is Jean Coleman
- her pet `7` is Samantha, a cat
- the visit for pet `7` dated `2013-01-01` is `rabies shot`
- vet `1` is James Carter

## Run

Prerequisites:

- [Docker](https://docs.docker.com/get-docker/) with Docker Compose
- [Node.js](https://nodejs.org/) for `ClinicWeb`
- [JDK 17](https://adoptium.net/) and [Maven](https://maven.apache.org/download.cgi) for in-process tests and the visits Maven graft client

The **visits** image is built with the customers registry GUID (`GRAFT_GUID`). A plain `docker compose up --build` without that GUID does not build visits. Use the scripted flow below or replicate its steps.

From `apps/spring-petclinic/microservices/src`:

```bash
./run-e2e.sh
```

On Windows:

```powershell
./run-e2e.ps1
```

The script starts `customers` and `vets`, reads the customers Maven GUID, builds and starts `visits`, writes `ClinicWeb/.npmrc` from `http://localhost:8096/npm`, runs `npm test`, then runs `VisitEndToEndTest` in `visits`.

Manual stack (same ports as the script):

| Service | Call port | Vision (gg 1.4.9, port 80 in container) |
| --- | --- | --- |
| customers | 8092 (`ws://localhost:8092/ws`) | http://localhost:8092 |
| vets | 8094 | http://localhost:8094 |
| visits | 8096 | http://localhost:8096 |

After customers is up:

```bash
curl http://localhost:8092/maven
export GRAFT_GUID=<guid-from-maven-url>
docker compose build visits && docker compose up -d visits
curl http://localhost:8096/npm
```

In-process Spring tests do not go through the graft:

```bash
mvn test
```

Run that in `src/customers`, `src/vets`, and `src/visits`. In `visits`, without `-Dgraft.guid`, only `VisitRepositoryTest` runs.

## Try it

The feed at `grft.dev` serves grafts only. Copy the npm install command from the visits Vision page (package name follows Maven coordinates, typically `@graft/maven-groupid_org-springframework-samples_artifactid_petclinic-visits@1.0.0`).

From `src/ClinicWeb`, point npm at the current registry (replace `YOUR_GUID`):

```bash
echo '@graft:registry=https://grft.dev/YOUR_GUID__free' > .npmrc
npm install
npm run dev
```

Open the URL Vite prints (typically [http://localhost:5174](http://localhost:5174)). [`src/ClinicWeb/src/main.js`](src/ClinicWeb/src/main.js) calls [`configureGraft()`](src/ClinicWeb/src/graftSetup.js) and runs [`runVisitsScenario()`](src/ClinicWeb/src/visitsScenario.js):

```javascript
import { configureGraft } from "./graftSetup.js";
import { Visits } from "@graft/maven-groupid_org-springframework-samples_artifactid_petclinic-visits";

configureGraft(); // GraftConfig.host + GraftConfig.stateless = false

const visits = await Visits.read(7);
```

Expected output once calls complete:

```text
Visit: 2013-01-01 rabies shot, owner Jean Coleman
```

That line needs `Visits.read` (visits DB + customers graft). The scripted e2e run reaches it with **stateful** npm and websocket hosts.

This sample sets **`GraftConfig.stateless = false`** on purpose (workaround for defect #3 in `petclinic-graftcode-defects.md` — DTO getters with `stateless: true`). **`VisitLine[]`** with stateful npm matches the monolith (Vitest PASS, 2026-10-12).

The visits host nests a **JVM customers graft client** inside gg. On JDK 17+, the SDK still validates `{user.dir}/resources/maven/.../petclinic-customers` on the **`java.class.path` system property** even when `host` is `ws://` (defect #1). The sample adds that directory in `CustomersGraftBootstrap` (static classpath setup) before the first `Customers` call. gg 1.4.9 hosts JVM with a minimal `java.class.path` (main visits JAR + hypertube core), so manifest `Class-Path` alone is not enough in the container.

The customers and visits hosts include a **documented sample workaround** for gg binding `NOPLoggerFactory` before Spring Boot starts (`preferLogbackOverGatewayNop()`). That is not a product fix; see `petclinic-graftcode-defects.md` §2. The visits image uses a Maven **thin JAR + `target/dependency/`** layout so Spring Boot starts inside gg (same pattern as customers/vets).

Optional CLI check (Maven graft client to customers, not the browser path): from `src/visits` with `-Dgraft.guid` from customers Vision, `mvn -q compile exec:java -Dgraft.guid=YOUR_GUID` runs [`Main.java`](src/visits/src/graft/java/org/springframework/samples/petclinic/visits/Main.java).

Stop the containers with `docker compose down`.

## Graft

```text
Customers.getOwner(...)
Customers.getOwnerForPet(...)
Vets.getVet(...)
Visits.read(...)
```

Imports and `GraftConfig` come from the generated graft. Copy the current snippet from Vision if package names differ. The npm graft uses **camelCase** method names.
