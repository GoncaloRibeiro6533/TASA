# How to run the project

## Start, scale, and stop containers

- Required Software:
  - [Docker](https://www.docker.com/) (installed and running)
  - [Java 21](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

## Deployment Steps

1. First, clone the repository:

```
    git clone https://github.com/GoncaloRibeiro6533/TASA
````

2.Move to the scripts directory:

``` bash
    cd scripts
```

3. Run the following command to start the services and generate the APK. You can optionally pass the desired port; otherwise, it will default to port 8080.
  During the building, you will be prompted to provide the server URL so the app can communicate with it.
  We suggest using ngrok as a secure and convenient way to expose your local development server to the internet, avoiding the need for manual port forwarding or tunneling. [ngrok](https://ngrok.com/)

``` bash
  ./start.sh [PORT]
```

### Server will start on port 8080  or on PORT

- Credentials of users already registered:
  - username: bob
  - password: Tasa_2025

### To start the backend only: 

  ``` bash
  ./start-backend.sh [PORT]
```

### To generate the APK only:

  ``` bash
  ./generate-apk.sh
```

- In the [docs folder](https://github.com/GoncaloRibeiro6533/TASA/blob/main/docs/Endpoints.postman_collection.json), you can find the Postman collection with all the endpoints of the API and the example requests to test the API.
- The API documentation is available [here](https://github.com/GoncaloRibeiro6533/TASA/blob/main/docs/api-docs.yaml)


- To stop the containers, you can run the following command in the scripts folder:
  
  ```bash
    ./stop.sh
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
