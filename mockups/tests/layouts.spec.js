const { test, expect } = require("@playwright/test");
const path = require("path");
const file = "file://" + path.join(__dirname, "..", "index.html");

test.describe("phone portrait with SIM", () => {
  test.use({ viewport: { width: 390, height: 844 } });

  test("shows call, messages and emergency", async ({ page }) => {
    await page.goto(`${file}?device=phone&orientation=portrait&sim=1`);
    await expect(page.getByTestId("tile-call")).toBeVisible();
    await expect(page.getByTestId("tile-messages")).toBeVisible();
    await expect(page.getByTestId("emergency")).toBeVisible();
    await expect(page.getByTestId("quick-contacts")).toBeVisible();
    const box = await page.getByTestId("tile-camera").boundingBox();
    expect(box.height).toBeGreaterThan(140);
  });
});

test.describe("tablet landscape without SIM", () => {
  test.use({ viewport: { width: 1280, height: 800 } });

  test("hides calling UI and keeps large home apps", async ({ page }) => {
    await page.goto(`${file}?device=tablet&orientation=landscape&sim=0`);
    await expect(page.getByTestId("tile-call")).toHaveCount(0);
    await expect(page.getByTestId("tile-messages")).toHaveCount(0);
    await expect(page.getByTestId("emergency")).toBeHidden();
    await expect(page.getByTestId("quick-contacts")).toBeHidden();
    await expect(page.getByTestId("tile-camera")).toBeVisible();
    await expect(page.getByTestId("tile-photos")).toBeVisible();
    const camera = await page.getByTestId("tile-camera").boundingBox();
    expect(camera.width).toBeGreaterThan(240);
    expect(camera.height).toBeGreaterThan(140);
  });

  test("apps page uses smaller tiles than home", async ({ page }) => {
    await page.goto(`${file}?device=tablet&orientation=landscape&sim=0&page=apps`);
    const small = await page.getByTestId("app-0").boundingBox();
    await page.goto(`${file}?device=tablet&orientation=landscape&sim=0&page=home`);
    const large = await page.getByTestId("tile-camera").boundingBox();
    expect(large.height).toBeGreaterThan(small.height);
    expect(page.getByTestId("page-numbers")).toBeTruthy();
  });
});

test.describe("phone landscape with SIM", () => {
  test.use({ viewport: { width: 844, height: 390 } });

  test("keeps calling tiles in a wide row", async ({ page }) => {
    await page.goto(`${file}?device=phone&orientation=landscape&sim=1`);
    await expect(page.getByTestId("tile-call")).toBeVisible();
    await expect(page.getByTestId("emergency")).toBeVisible();
  });
});
