# RealWorld / Conduit — Graftcode

Original project:
https://github.com/gothinkster/node-express-realworld-example-app

Sources for the article slice are taken from commit `30b68e1e881462b2f4164ea09ab4c4f5699c7b0b`. That repository does not contain a `LICENSE` file. Its `package.json` declares `"license": "MIT"`. This sample keeps that declaration.

The host and the consumer are in this folder. A call through Graftcode Gateway does not complete. Gateway's embedded Node.js exits while loading Prisma, before it publishes an npm install command.

## What was changed

- Kept the article service, its mapper, Prisma, and the PostgreSQL migrations
- Kept `createUser`, used by the seed, including the JWT it signs for that insert
- Added a static `Articles` facade that delegates to `getArticles` and `getArticle` with no user id
- Replaced the upstream random seed with one demo user and one article, still created through `createUser` and `createArticle`
- Hosted the compiled module with Graftcode Gateway
- Added `ArticleConsumer`, which installs the graft and calls it

Accounts, comments, favorites, and article writes stay in the copied service files. They are not exported from the module Gateway loads. Express does not start.

The copied article mapper also returns the numeric `id`. The original mapper omitted it. `ArticleView.createdAt` is an ISO-8601 string. The row still stores `DateTime`.

| Application location | Before | With Graftcode | Developer responsibility removed |
| --- | --- | --- | --- |
| Article list and one article | Express controller and a handwritten HTTP client | `Articles.listArticles`, `Articles.getArticle` | Routes, JSON mapping, and a handwritten client |

## Original architecture

The Node backend is one Express process. Prisma talks to PostgreSQL. A public article list returns articles whose author is marked `demo`. A caller with a JWT can also see that user's own articles. Comments, profiles, and authentication are separate routes.

## Graftcode architecture

`src/ArticleService` is the host. Graftcode Gateway serves calls on container port 80 and Graftcode Vision. PostgreSQL is a separate Compose service. `src/ArticleConsumer` is a separate Node process that installs the graft and calls it.

On the host, `gg` is the process. It loads `dist/index.js`. The first `Articles` call uses the existing `getArticles` / `getArticle` functions. There is no Express server.

The graft surface is static. Outside the host the types are `string`, `number`, `ArticleView`, and arrays. `Date`, Prisma models, and `HttpException` stay inside the host.

Seed, on an empty database:

- user `conduit` (`demo: true`), id `1`
- article title `How to train your dragon`
- slug `How-to-train-your-dragon-1` (`slugify(title)` plus the author id)
- description `Ever wonder how?`
- body `It takes a Jacobian.`
- tags `dragons` and `training`

`getArticles` orders by `createdAt` descending and limits to 10. With one row, that row is the first article.

## Run

Prerequisites:

- [Docker](https://docs.docker.com/get-docker/) with Docker Compose
- [Node.js](https://nodejs.org/) for the consumer

From `apps/realworld/articles/src`:

```bash
docker compose up --build -d
```

Compose publishes port 8092 for calls (`ws://localhost:8092/ws`) and port 8093. Wait until the install snippet is available:

```bash
curl http://localhost:8092/npm
```

The registry GUID changes every time the container starts. Copy the current install command from that response. Do not reuse a GUID from an earlier run.

PostgreSQL data is in the `postgres-data` volume, so the seed survives a container restart. The seed skips when that title is already stored. Stop the containers with `docker compose down`. To delete the database and seed again on the next start:

```bash
docker compose down -v
```

## Try it

From `src/ArticleConsumer`, install the package named in `http://localhost:8092/npm`, then point the process at that package name:

```bash
npm install @graft/npm-PACKAGE --registry https://grft.dev/YOUR_GUID__free
```

`src/ArticleConsumer/main.js` reads `GRAFT_PACKAGE`, points the graft at the local gateway, and prints the seeded title:

```javascript
const { Articles, GraftConfig } = require(process.env.GRAFT_PACKAGE);

GraftConfig.host = "ws://localhost:8092/ws";
GraftConfig.stateless = true;

const articles = await Articles.listArticles();
console.log("Getting first article: " + articles[0].title);
```

```bash
GRAFT_PACKAGE=@graft/npm-PACKAGE node main.js
```

Expected output once a call can complete:

```text
Getting first article: How to train your dragon
Getting How-to-train-your-dragon-1: How to train your dragon
Getting missing-slug: ...not found...
```

That output is not what this gateway returns. The backend process exits while `gg` loads Prisma, so `node main.js` has no gateway to call.

`src/run-e2e.ps1` starts Compose, then reads the package name and registry from `http://localhost:8092/npm`. That request fails: the backend container has already exited, so nothing is listening on port 8092. The consumer tests do not run.

What the container did, on gg 1.4.9:

- The image's Node ran `prisma migrate deploy` and the seed. PostgreSQL has article id `1`, title `How to train your dragon`, slug `How-to-train-your-dragon-1`.
- `gg` then loaded `./package.json` with its embedded Node.js 22.18.0 (`libnode.so.127`).
- Importing the module constructs `PrismaClient`. Prisma 4.16.2 tries to load `libquery_engine-debian-openssl-3.0.x.so.node` and the process throws `undefined symbol: napi_create_promise`.
- The container exits with code 1. `http://localhost:8092/npm` refuses the connection.

The article service and the database were left as they are. The sample does not replace Prisma to get past the embedded runtime.

## Graft

```text
Articles.listArticles()
Articles.getArticle(...)
```

`ArticleView` fields are `id`, `title`, `slug`, `description`, `body`, `author`, `createdAt`, and `tagList`.

Imports and `GraftConfig` come from the graft. The package name is the one in the install command from `http://localhost:8092/npm`, once that process stays up long enough to publish it.
