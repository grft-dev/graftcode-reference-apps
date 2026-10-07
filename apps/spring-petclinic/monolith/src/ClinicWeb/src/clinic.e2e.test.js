import { beforeAll, describe, expect, it } from "vitest";
import { configureGraft } from "./graftSetup.js";
import {
  addVisitIsVisibleThroughTheGraft,
  firstOwnerIsGeorge,
  georgeHasLeoTheCat,
  missingOwnerKeepsTheMessage,
  seededVisitIsSamanthasRabiesShot,
} from "./clinicScenario.js";

describe("Clinic through the npm graft", () => {
  beforeAll(() => {
    configureGraft();
  });

  it("firstOwnerIsGeorge", async () => {
    const owner = await firstOwnerIsGeorge();
    expect(owner.firstName).toBe("George");
    expect(owner.lastName).toBe("Franklin");
  });

  it("georgeHasLeoTheCat", async () => {
    const pets = await georgeHasLeoTheCat();
    expect(pets.length).toBe(1);
    expect(pets.name).toBe("Leo");
    expect(pets.type).toBe("cat");
    expect(pets.birthDate).toBe("2010-09-07");
  });

  it("seededVisitIsSamanthasRabiesShot", async () => {
    const visits = await seededVisitIsSamanthasRabiesShot();
    expect(visits.descriptions).toContain("rabies shot");
    expect(visits.dates).toContain("2013-01-01");
  });

  it("addVisitIsVisibleThroughTheGraft", async () => {
    const result = await addVisitIsVisibleThroughTheGraft();
    expect(result.description).toBe("graft checkup");
    expect(result.date).toBe("2026-10-05");
    expect(result.visitDescriptions).toContain("graft checkup");
  });

  it("missingOwnerKeepsTheMessage", async () => {
    const result = await missingOwnerKeepsTheMessage();
    expect(result.thrown).toBe(true);
    expect(result.message).toContain("Owner with id 999 not found.");
  });
});
