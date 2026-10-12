/**
 * Logs getResultType() for getPets after invokeStaticMethod.execute().
 * Usage: node scripts/probe-array-type.mjs false|true
 */
import { Clinic, GraftConfig } from "@graft/maven-groupid_org-springframework-samples_artifactid_petclinic-clinic";

const statelessArg = process.argv[2];
if (statelessArg !== "true" && statelessArg !== "false") {
  console.error("Usage: node scripts/probe-array-type.mjs true|false");
  process.exit(1);
}

GraftConfig.host = process.env.GRAFT_HOST ?? "ws://localhost:8090/ws";
GraftConfig.stateless = statelessArg === "true";

const ic = await Clinic.type.invokeStaticMethod("getPets", 1).execute();
const resultType = await ic.getResultType();
console.log(
  JSON.stringify({
    stateless: GraftConfig.stateless,
    resultType,
  }),
);
process.exit(0);
