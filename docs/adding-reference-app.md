# Adding a reference app

1. Add `apps/<project>/README.md` with the original project URL and a table of scenarios.
2. Add `apps/<project>/<variant>/README.md` for each scenario. Use the same sections every time:

   - title (`# <Project> — Graftcode`)
   - `Original project:` and the upstream URL
   - `## What was changed`
   - `## Original architecture`
   - `## Graftcode architecture`
   - `## Run`
   - `## Try it`
   - `## Graft`

3. Put the host, and any consumer, under that variant (`src/`, or one folder per service).
4. Add a row to the catalog table in the repository root `README.md`: application, stack, scenario, Graftcode use case.

A scenario that is only planned still gets the README. Say that the port is not in the repository, and leave `Run` and `Try it` empty of commands.
