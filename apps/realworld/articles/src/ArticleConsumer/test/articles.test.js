const assert = require("node:assert/strict");
const test = require("node:test");

const { Articles, GraftConfig } = require(process.env.GRAFT_PACKAGE);

const SEED_TITLE = "How to train your dragon";
const SEED_SLUG = "How-to-train-your-dragon-1";

test.before(() => {
  GraftConfig.host = "ws://localhost:8092/ws";
  GraftConfig.stateless = true;
});

test("first article is the seeded title", async () => {
  const articles = await Articles.listArticles();
  assert.ok(articles.length > 0);
  assert.equal(articles[0].title, SEED_TITLE);
  assert.equal(articles[0].slug, SEED_SLUG);
  assert.equal(articles[0].author, "conduit");
});

test("getArticle returns the seeded article", async () => {
  const article = await Articles.getArticle(SEED_SLUG);
  assert.equal(article.title, SEED_TITLE);
  assert.equal(article.slug, SEED_SLUG);
  assert.equal(article.description, "Ever wonder how?");
  assert.equal(article.body, "It takes a Jacobian.");
  assert.equal(article.tagList.length, 2);
  assert.ok(article.tagList.includes("dragons"));
  assert.ok(article.tagList.includes("training"));
  assert.match(article.createdAt, /^\d{4}-\d{2}-\d{2}T/);
});

test("missing slug keeps the host message", async () => {
  await assert.rejects(
    () => Articles.getArticle("missing-slug"),
    (error) => {
      const text = String(error && error.message ? error.message : error);
      assert.match(text, /not found/);
      return true;
    }
  );
});
