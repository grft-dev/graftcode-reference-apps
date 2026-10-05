# Spring Petclinic — Graftcode

Original project:
https://github.com/spring-projects/spring-petclinic

This variant is not ported yet. There is no host, gateway, or consumer in this folder.

## What was changed

Nothing yet. The port will add Graftcode Gateway and expose the clinic as a graft.

## Original architecture

Spring Petclinic is one Java process. Owners, pets, visits, and vets share one application, one relational database, and a server-rendered UI.

## Graftcode architecture

The intended sample keeps that single process. Callers reach owners, pets, and visits through Graftcode Gateway instead of the original HTTP UI. Source will live in `src/` when the port is added.

## Run

Not available until the port lands in this folder.

## Try it

Not available until the port lands in this folder.

## Graft

```text
Clinic.getOwner(...)
Clinic.getPets(...)
Clinic.getVisits(...)
```
