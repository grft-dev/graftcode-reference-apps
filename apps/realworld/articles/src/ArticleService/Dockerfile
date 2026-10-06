FROM node:22

WORKDIR /usr/app

COPY . /usr/app/

RUN npm ci \
 && npx prisma generate \
 && npx tsc

RUN apt-get update && apt-get install -y wget \
 && ARCH=$(dpkg --print-architecture) \
 && wget -q -O /usr/app/gg.deb "https://github.com/grft-dev/graftcode-gateway/releases/latest/download/gg_linux_${ARCH}.deb" \
 && dpkg -i /usr/app/gg.deb && rm /usr/app/gg.deb \
 && apt-get clean && rm -rf /var/lib/apt/lists/*

EXPOSE 80
EXPOSE 81

RUN chmod +x /usr/app/entrypoint.sh

CMD ["/usr/app/entrypoint.sh"]
