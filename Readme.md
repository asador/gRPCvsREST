gRPC vs REST Performance Comparison
--------------------------------------------

This project contains 4 modules to compare performance of gRPC vs. REST calls. The clients and servers contain no business logic and logging is disabled to help focus on the performance difference of gRPC or REST calls between parties. gRPC and REST calls include Java object to protobuf or JSON conversion back and forth as well as the underlying http communications. The tests are on plain http without TLS.

The test scenarios include reading and writing of a single `Employee` object and a list of 10, 100, and 1000 objects. 

## grpc-server ##

**Build:**
```
mvn clean install spring-boot:repackage
```

**Run:**
The following command starts the server on port 8980;
```
java -jar grpc-server-0.0.1-SNAPSHOT.jar
```

## grpc-client ##

This is a test client that calls the gRPC server. When the test is finished, it stores the resutls in a local *csv* file. Configuration parameters should be passed with `-D` system property to overwrite the defaults.

**Build:**
```
mvn clean install spring-boot:repackage
```

**Run:**
```
java -Dgrpc.server.address=<host:8980> -Dtest.numberOfThreads=<default is 1> -Dtest.outputFile=<output file name> -jar grpc-client-0.0.1-SNAPSHOT.jar
```

## rest-server ##

**Build:**
```
mvn clean install spring-boot:repackage
```

**Run:**
The following command starts the server on port 8080;
```
java -jar rest-server-0.0.1-SNAPSHOT.jar
```

## rest-client ##

This is a test client that calls the REST server. When the test is finished, it stores the resutls in a local *csv* file. Configuration parameters should be passed with `-D` system property to overwrite the defaults.

**Build:**
```
mvn clean install spring-boot:repackage
```

**Run:**
```
java -Drest.server.address=http://<host:8080> -Dtest.numberOfThreads=<default is 1> -Dtest.outputFile=<output file name> -jar rest-client-0.0.1-SNAPSHOT.jar
```
