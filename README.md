Start up postgres before running

`docker run --name postgres -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres`

For the java app, the following env vars are required for full functionality
```
OWNER_ID
PHIL_BOT_TOKEN
BEHRAD_BOT_TOKEN
KEANU_BOT_TOKEN
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
SPRING_DATASOURCE_URL
GFYCAT_CLIENT_ID
GFYCAT_CLIENT_SECRET
```