const { Articles, GraftConfig } = require(process.env.GRAFT_PACKAGE);

const SEED_TITLE = "How to train your dragon";
const SEED_SLUG = "How-to-train-your-dragon-1";

function graftHost() {
  GraftConfig.host = "ws://localhost:8092/ws";
  GraftConfig.stateless = true;
}

async function main() {
  graftHost();
  const articles = await Articles.listArticles();
  const first = articles[0];
  console.log("Getting first article: " + first.title);

  const article = await Articles.getArticle(SEED_SLUG);
  console.log("Getting " + SEED_SLUG + ": " + article.title);

  try {
    await Articles.getArticle("missing-slug");
    console.log("Getting missing-slug: call returned without an error");
  } catch (error) {
    console.log("Getting missing-slug: " + error.message);
  }
}

main().then(
  () => process.exit(0),
  (error) => {
    console.error(error);
    process.exit(1);
  }
);
