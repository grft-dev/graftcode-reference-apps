# Architecture

Each application follows one hierarchy:

```text
original project
    → variant / scenario
        → service / module
```

That is the folder layout under `apps/`:

```text
apps/<project>/<variant>/<service>/
```

Examples:

- `apps/eshop/catalog/src` — eShop, catalog scenario, host and consumer
- `apps/spring-petclinic/monolith` — one process that exposes the clinic
- `apps/spring-petclinic/microservices` — the same clinic split into customers, visits, and vets

Group by the original project and the scenario, not by language or framework.
