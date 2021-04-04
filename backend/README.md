# Back-End Server


## Run Locally

```
cd web
bash deploy.sh
cd ../backend
rm -rf target && mvn package && env ENABLE_CORS=true CLIENT_ID=526954726512-b2j1mck4ackqtkt0i75aaroabnkq9072.apps.googleusercontent.com java -jar ./target/backend-1.0-SNAPSHOT-jar-with-dependencies.jar`
```
