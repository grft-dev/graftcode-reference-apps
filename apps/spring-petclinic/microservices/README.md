# Spring Petclinic Microservices — Graftcode

Original project:
https://github.com/spring-petclinic/spring-petclinic-microservices

This variant is not ported yet. There is no host, gateway, or consumer in this folder.

## What was changed

Nothing yet. The port will connect the clinic services with grafts and leave Spring Cloud out of the sample.

## Original architecture

The same clinic data is split across Spring Cloud services, including customers, vets, and visits, plus service discovery, configuration, and an edge gateway.

## Graftcode architecture

The intended sample keeps the customers, visits, and vets split. A visit reads the owner through a graft. Those three services will live in `customers/`, `visits/`, and `vets/` when the port is added. Spring Cloud stays in the original project.

## Run

Not available until the port lands in this folder.

## Try it

Not available until the port lands in this folder.

## Graft

Target services: `customers`, `visits`, `vets`. Method names land with the port.
