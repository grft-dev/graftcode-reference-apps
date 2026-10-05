# Spring Petclinic

Two layouts of the same clinic domain.

| Scenario | Path | Stack | Graftcode use case |
|----------|------|-------|--------------------|
| Monolith | [monolith](monolith/README.md) | Java | Expose backend |
| Microservices | [microservices](microservices/README.md) | Java | Connect services |

The monolith host and consumer are in [monolith](monolith/README.md). A call through the gateway does not complete; the README records why. The microservices variant is not ported yet.
