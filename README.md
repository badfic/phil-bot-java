![](https://img.shields.io/discord/740999022340341791)

A multipurpose discord bot for "The Swamp", a community discord server (join at [https://discord.gg/sdykqyx](https://discord.gg/sdykqyx))

---

# Building The Project

Start up postgres before running

```
docker run --rm --name postgres -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres:15-alpine
```

For the java app, the following env vars are required for full functionality
```
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD

OWNER_ID
GUILD_ID
PHIL_BOT_TOKEN

DISCORD_CLIENT_ID
DISCORD_CLIENT_SECRET

HOSTNAME

AO3_SUMMARY_API_KEY
```

For error handling, a sentry.io account can be setup and the following env var is required
```
SENTRY_URL
```

For log management, a logflare.app account can be setup and the following env vars are required
```
LOGFLARE_URL
LOGFLARE_API_KEY
```

---

IntelliJ IDEA product subscription provided by JetBrains: https://www.jetbrains.com/community/opensource
