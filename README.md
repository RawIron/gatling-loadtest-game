## Load Test a simple Game API
load test an API of a game back-end using Gatling 2.1

API spec is done with Swagger. Load tests are written using Gatling.

### Run the load tests

the project should compile without any errors
```
mvn compile
```

run a load test
```
mvn gatling:execute -Dgatling.simulationClass='loadtest.user.ConnectSimulation'
```
