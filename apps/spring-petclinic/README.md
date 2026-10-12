# Spring Petclinic

Two layouts of the same clinic domain.

| Scenario | Path | Stack | Graftcode use case |
|----------|------|-------|--------------------|
| Monolith | [monolith](monolith/README.md) | Java | Expose backend |
| Microservices | [microservices](microservices/README.md) | Java + JS | Connect services |

The monolith host and consumer are in [monolith](monolith/README.md). The microservices hosts (`customers`, `vets`, `visits`) and the `ClinicWeb` consumer are in [microservices](microservices/README.md). A call through the gateway may not complete in either layout; each README records why.
