import { getArticle, getArticles } from './app/routes/article/article.service';

export class ArticleView {
  id: number;
  title: string;
  slug: string;
  description: string;
  body: string;
  author: string;
  createdAt: string;
  tagList: string[];
}

function toView(article: {
  id: number;
  title: string;
  slug: string;
  description: string;
  body: string;
  author: { username: string };
  createdAt: Date | string;
  tagList: string[];
}): ArticleView {
  const view = new ArticleView();
  view.id = article.id;
  view.title = article.title;
  view.slug = article.slug;
  view.description = article.description;
  view.body = article.body;
  view.author = article.author.username;
  view.createdAt = new Date(article.createdAt).toISOString();
  view.tagList = article.tagList;
  return view;
}

export class Articles {
  static async listArticles(): Promise<ArticleView[]> {
    const result = await getArticles({});
    return result.articles.map(toView);
  }

  static async getArticle(slug: string): Promise<ArticleView> {
    return toView(await getArticle(slug));
  }
}
