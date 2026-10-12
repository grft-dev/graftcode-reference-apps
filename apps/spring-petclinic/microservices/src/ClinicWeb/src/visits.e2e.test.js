import { beforeAll, describe, expect, it } from "vitest";
import { configureGraft } from "./graftSetup.js";
import { seededVisitNamesJeanColeman } from "./visitsScenario.js";

describe("Visits.read through the npm graft", () => {
  beforeAll(() => {
    configureGraft();
  });

  it("seededVisitNamesJeanColeman", async () => {
    const rabies = await seededVisitNamesJeanColeman();
    expect(rabies).not.toBeNull();
    expect(rabies.description).toBe("rabies shot");
    expect(rabies.date).toBe("2013-01-01");
    expect(rabies.ownerFirstName).toBe("Jean");
    expect(rabies.ownerLastName).toBe("Coleman");
  });
});
