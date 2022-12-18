# System requirements

- Java 11 or newer
- Maven (only if the program runs via `mvn`)

# Running

There are two ways to run the program: from the source code or using executable JAR

## Running from the source code

To use this option the system must have JDK and Maven installed.

1. Clone repo
2. From the project folder execute
```shell
mvn clean install
mvn spring-boot:run
```

## Running using executable JAR

1. Obtain the executable JAR (either get it from author or build it from source)
2. From the JAR folder run
```shell
java -jar cryptospring-0.0.1-SNAPSHOT.jar
```

# Building executable JAR

From the project folder run
```shell
mvn clean package
```
This will create executable JAR in `target/cryptospring-0.0.1-SNAPSHOT.jar`
