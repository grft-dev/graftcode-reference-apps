import { Clinic } from "@graft/maven-groupid_org-springframework-samples_artifactid_petclinic-clinic";

export async function runClinicScenario() {
  const lines = [];

  const owner = await Clinic.getOwner(1);
  const firstName = await owner.getFirstName();
  const lastName = await owner.getLastName();
  lines.push(`Getting owner 1: ${firstName} ${lastName}`);

  const pets = await Clinic.getPets(1);
  const petName = await pets[0].getName();
  const petType = await pets[0].getType();
  lines.push(`Pet: ${petName} the ${petType}`);

  const visits = await Clinic.getVisits(6, 7);
  const visitDate = await visits[0].getDate();
  const visitDescription = await visits[0].getDescription();
  lines.push(`Visit: ${visitDate} ${visitDescription}`);

  try {
    await Clinic.getOwner(999);
    lines.push("Getting owner 999: unexpected success");
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    lines.push(`Getting owner 999: ${message}`);
  }

  return lines;
}

export async function firstOwnerIsGeorge() {
  const owner = await Clinic.getOwner(1);
  return {
    firstName: await owner.getFirstName(),
    lastName: await owner.getLastName(),
  };
}

export async function georgeHasLeoTheCat() {
  const pets = await Clinic.getPets(1);
  return {
    length: pets.length,
    name: await pets[0].getName(),
    type: await pets[0].getType(),
    birthDate: await pets[0].getBirthDate(),
  };
}

export async function seededVisitIsSamanthasRabiesShot() {
  const visits = await Clinic.getVisits(6, 7);
  const descriptions = await Promise.all(visits.map((v) => v.getDescription()));
  const dates = await Promise.all(visits.map((v) => v.getDate()));
  return { descriptions, dates };
}

export async function addVisitIsVisibleThroughTheGraft() {
  const created = await Clinic.addVisit(1, 1, "2026-10-05", "graft checkup");
  const description = await created.getDescription();
  const date = await created.getDate();
  const visits = await Clinic.getVisits(1, 1);
  const visitDescriptions = await Promise.all(visits.map((v) => v.getDescription()));
  return { description, date, visitDescriptions };
}

export async function missingOwnerKeepsTheMessage() {
  try {
    await Clinic.getOwner(999);
    return { thrown: false, message: "" };
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    return { thrown: true, message };
  }
}
