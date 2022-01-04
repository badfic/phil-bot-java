![](https://img.shields.io/discord/740999022340341791)

A multipurpose discord bot for "The Swamp", a community discord server (join at [https://discord.gg/sdykqyx](https://discord.gg/sdykqyx))

---

# Building The Project

Start up postgres and owncast before running

```
docker run --rm --name postgres -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres:12
docker run --rm --name owncast -p 8085:8080 -p 1935:1935 -d gabekangas/owncast:0.0.5
```

For the java app, the following env vars are required for full functionality
```
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD

OWNER_ID
PHIL_BOT_TOKEN
BEHRAD_BOT_TOKEN
KEANU_BOT_TOKEN
ANTONIA_BOT_TOKEN
JOHN_BOT_TOKEN
TIMEOUT_CHANNEL_ID

DISCORD_CLIENT_ID
DISCORD_CLIENT_SECRET

HOSTNAME

HONEYBADGER_API_KEY

TUMBLR_CONSUMER_KEY
TUMBLR_CONSUMER_SECRET
TUMBLR_OAUTH_TOKEN
TUMBLR_OAUTH_SECRET

OWNCAST_INSTANCE
```

The following JVM argument is now required for the tumblr command to work correctly
```
--add-opens java.base/java.io=ALL-UNNAMED
```

---

IntelliJ IDEA product subscription provided by JetBrains: https://www.jetbrains.com/community/opensource
