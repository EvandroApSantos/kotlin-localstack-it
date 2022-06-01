# kotlin-localstack-it

This is an educational project to explain how to create integration tests 
using Avast's docker-compose-plugin and Localstack, which are very useful for projects that relies 
on AWS cloud.

### Context description

In order to simulate an integration with some AWS services, this project will make use of SQS and S3 through Localstack.  
The goal is to read some messages from SQS that contains car's maintenance information and consolidate that information 
in a bucket on S3. Each object in the bucket will represent a car with aggregated maintenance data in it.

### Pre-requirements

To run the project is expected that the following items are available and configured on the environment:

* JDK 17
* Gradle
* Docker
* Docker Compose
* aws-cli

### Running

> **Disclaimer**  
> Some scripts were make for Linux environment (which I am using right now) and you may need to adapt them to your operating system

Building the project:

```shell
gradle clean build
```

Before staring the project you must start Localstack, otherwise some connection errors will arise:

```shell
docker-compose -f infra/docker-compose.yaml up -d
```

If no errors have been thrown so far, just run:

```shell
./gradlew run
```

To send messages to the queue you can use the file `maintenance-example.json` and customize it with data you might think interesting.  
After that, run the script `send-message.sh` to send the file's content to the queue.  
Check if the message was successfully processed on service's logs.  
Also, you can check whether the S3 object contains the maintenance info you have provided in the message. There are many ways to do that, 
but in my opinion using a web browser is the easiest:  
> http://localhost:4566/000000000000/car-maintenance/<car-id>  

Paste this address on your web browser replacing `<car-id>` with the id you have placed on `maintenance-example.json`.  


### Running integration tests

To run integration tests, use the following command:

```shell
gradle clean integrationTests
```
