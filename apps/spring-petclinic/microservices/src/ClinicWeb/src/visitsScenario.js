import { Visits } from "@graft/maven-groupid_org-springframework-samples_artifactid_petclinic-visits";

export async function runVisitsScenario() {
  const lines = [];
  const visits = await Visits.read(7);

  for (const visit of visits) {
    const date = await visit.getDate();
    const description = await visit.getDescription();
    if (date === "2013-01-01" && description === "rabies shot") {
      const ownerFirstName = await visit.getOwnerFirstName();
      const ownerLastName = await visit.getOwnerLastName();
      lines.push(`Visit: ${date} ${description}, owner ${ownerFirstName} ${ownerLastName}`);
    }
  }

  if (lines.length === 0) {
    lines.push("Visit: seeded row for pet 7 not found.");
  }

  return lines;
}

export async function seededVisitNamesJeanColeman() {
  const visits = await Visits.read(7);
  for (const visit of visits) {
    const date = await visit.getDate();
    const description = await visit.getDescription();
    if (date === "2013-01-01" && description === "rabies shot") {
      return {
        date,
        description,
        ownerFirstName: await visit.getOwnerFirstName(),
        ownerLastName: await visit.getOwnerLastName(),
      };
    }
  }
  return null;
}
