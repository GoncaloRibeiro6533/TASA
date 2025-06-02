# How to run the project

## Start, scale, and stop containers

- Required Software:
  - [Docker](https://www.docker.com/) (installed and running)
  - [Gradle](https://gradle.org/install/) 
  - [Java 21](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) 

## Deployment Steps

1. First, clone the repository:

```
    git clone https://github.com/GoncaloRibeiro6533/TASA
````
2. Move to the project directory:

``` bash
    cd jvm
```

3. Run the following command to start the containers:

``` bash
  ./gradlew startAll
```

### Server will start on port 8080  


- Credentials of users already registered:
  - username: bob
  - password: Tasa_2025

- In the [docs folder](https://github.com/GoncaloRibeiro6533/TASA/blob/main/docs/Endpoints.postman_collection.json), you can find the Postman collection with all the endpoints of the API and
    the example requests to test the API.

- To stop the containers, you can run the following command in the `scripts` folder:
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
