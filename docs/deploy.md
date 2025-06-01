# How to run the project

## Build the docker images

- To run the docker version you should move to jvm folder:

```bash
cd code/jvm
 ```

- Builds the JVM image with ChImp backend HTTP API:

  ```bash
  ./gradlew buildImageJvm
  ```

- Builds the Postgres image for testing:

  ```bash
  ./gradlew buildImagePostgresTest
  ```

- Builds the Nginx image:

  ```bash
  ./gradlew buildImageNginx
  ```

OR

- Builds all images:

  ```bash
  ./gradlew buildImageAll
  ```

## Start, scale, and stop services

### Server will start on port 8080  

- Starts all services:

  ```bash
  ./gradlew allUp
  ```

- Credentials of users already registered:
  - username: bob
  - password: Tasa_2025

- Stops all services:
  
  ```bash
  ./gradlew allDown
  ```

- To scale the dynamic JVM service:
  - First move to host folder:

 ```bash
    cd host
```

- Then run the following command:

```bash
    docker-compose up --scale tasa-jvm=3
```
