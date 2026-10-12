import { GraftConfig } from "@graft/maven-groupid_org-springframework-samples_artifactid_petclinic-visits";

const DEFAULT_HOST = "ws://localhost:8096/ws";

function resolveHost() {
  if (typeof import.meta !== "undefined" && import.meta.env?.VITE_GRAFT_HOST) {
    return import.meta.env.VITE_GRAFT_HOST;
  }
  if (typeof process !== "undefined" && process.env?.GRAFT_HOST) {
    return process.env.GRAFT_HOST;
  }
  return DEFAULT_HOST;
}

export function configureGraft(host) {
  GraftConfig.host = host ?? resolveHost();
  GraftConfig.stateless = false;
}

export { GraftConfig, resolveHost };
