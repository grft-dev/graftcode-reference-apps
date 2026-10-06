import { createArticle } from '../app/routes/article/article.service';
import { createUser } from '../app/routes/auth/auth.service';
import prisma from './prisma-client';

export const SEED_TITLE = 'How to train your dragon';

const main = async () => {
  const existing = await prisma.article.findFirst({
    where: { title: SEED_TITLE },
    select: { id: true },
  });
  if (existing) {
    return;
  }

  const user = await createUser({
    username: 'conduit',
    email: 'conduit@example.com',
    password: 'password',
    demo: true,
  });

  await createArticle(
    {
      title: SEED_TITLE,
      description: 'Ever wonder how?',
      body: 'It takes a Jacobian.',
      tagList: ['dragons', 'training'],
    },
    user.id,
  );
};

main()
  .then(async () => {
    await prisma.$disconnect();
  })
  .catch(async (error) => {
    console.error(error);
    await prisma.$disconnect();
    process.exit(1);
  });
