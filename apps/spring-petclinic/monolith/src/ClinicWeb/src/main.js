import { configureGraft, resolveHost } from "./graftSetup.js";
import { runClinicScenario } from "./clinicScenario.js";

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
    runClinicScenario(),
    new Promise((_, reject) => {
      setTimeout(
        () =>
          reject(
            new Error(
              `Graft call timed out after ${GRAFT_TIMEOUT_MS / 1000}s. Start the gateway: docker compose up -d in monolith/src.`,
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
