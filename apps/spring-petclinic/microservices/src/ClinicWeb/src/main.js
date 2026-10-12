import { configureGraft, resolveHost } from "./graftSetup.js";
import { runVisitsScenario } from "./visitsScenario.js";

const output = document.querySelector("#output");
const GRAFT_TIMEOUT_MS = 60_000;

function show(text) {
  output.textContent = text;
}

function formatError(error) {
  if (error instanceof Error) {
    return error.stack ?? error.message;
  }
  return String(error);
}

window.addEventListener("unhandledrejection", (event) => {
  show(formatError(event.reason));
});

try {
  configureGraft();
  const host = resolveHost();
  show(`Connecting to ${host}…`);

  const lines = await Promise.race([
    runVisitsScenario(),
    new Promise((_, reject) => {
      setTimeout(
        () =>
          reject(
            new Error(
              `Graft call timed out after ${GRAFT_TIMEOUT_MS / 1000}s. Start the stack: run-e2e.ps1 or docker compose in microservices/src (visits needs GRAFT_GUID).`,
            ),
          ),
        GRAFT_TIMEOUT_MS,
      );
    }),
  ]);

  show(lines.join("\n"));
} catch (error) {
  show(formatError(error));
}
