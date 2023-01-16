gRPC vs REST Performance Comparison
--------------------------------------------

This project contains 4 modules to compare performance of gRPC vs. REST calls. The clients and servers contain no business logic and logging is disabled to help focus on the performance difference of gRPC or REST calls between parties. gRPC and REST calls include Java object to protobuf or JSON conversion back and forth as well as the underlying http communications. The tests are on plain http without TLS.

The test scenarios include reading and writing of a single `Employee` object and a list of 10, 100, and 1000 objects. The average size of `Employee` is ~130 and ~310 bytes in protobuf and JSON respectively when serialized on the wire. 

Both gRPC and REST servers are being initialized with 10000 random sample records. This data will be retrieved by clients as individual records or in a batch of different sizes. Clients are also initialized with 2000 sample records used for uploading to the servers.    

## grpc-server ##

**Build:**
```
mvn clean install spring-boot:repackage
```

**Run:**
The following command starts the server on port 8980.
```
java -jar grpc-server-0.0.1-SNAPSHOT.jar
```

**Run with TLS:**
Add the following `-D` option to start the server on port 8980 with TLS. This requires to have server private key and certificate be present in the same folder.
```
-Dtls.enabled=true
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

**Run with TLS:**
Add following `-D` option to connect with TLS. This requires to have CA certificate be present in the same folder.
```
-Dtls.enabled=true
```

## rest-server ##

**Build:**
```
mvn clean install spring-boot:repackage
```

**Run:**
The following command starts the server on port 8080.
```
java -jar rest-server-0.0.1-SNAPSHOT.jar
```

**Run with TLS:**
Add the following `-D` command to start the server on port 8443 with TLS. This requires to have server private key and certificate be present in the same folder.
```
-Dspring.profiles.active=tls
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
**Run with TLS:**
Change the server address to https on port 8443. Add the following `-D` option to enable TLS. This requires to have trust-store file be present in the same folder.
```
-Dspring.profiles.active=tls
```
## Generating Self-Signed Certificates ##

**Build certificates**
The following command generates a self-signed CA certificate (ca-cert.pem), a server certificate (server-cert.pem) and its private key (server-key.pem).
```
gen-cert.sh
```

**Build trust store**
This is to create a trust store containg the self-signed root certificate that was generated in the previous step. This file is required by the rest-client.
```
keytool -import -alias "myCAcert" -file ca-cert.pem -keystore truststore.p12
```

## Test Results and Observations Article ##
Test results are available in the `testResults` folder.

1. [A Study to compare gRPC and REST and when each one performs better](https://www.linkedin.com/pulse/grpc-rest-which-one-performs-better-reza-asadollahi) 
2. [gRPC vs REST performancce comparison over TLS](https://www.linkedin.com/pulse/performance-comparison-grpc-rest-over-tls-reza-asadollahi/)

