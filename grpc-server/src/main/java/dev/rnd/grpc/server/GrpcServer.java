package dev.rnd.grpc.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import dev.rnd.grpc.server.controller.EmployeeGrpcController;
import dev.rnd.grpc.server.controller.SystemGrpcController;
import dev.rnd.grpc.server.service.EmployeeService;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

public class GrpcServer 
{
  private static final Logger logger = Logger.getLogger(GrpcServer.class.getName());
  private static final String SAMPLE_DATA_SET = "sampleEmployeeData.csv";
  
  private final Server server;
  private Properties config;

  public GrpcServer(int port) throws IOException {
    this(ServerBuilder.forPort(port));
  }

  public GrpcServer(ServerBuilder<?> serverBuilder) {
  	loadConfigProperties();
  	if ("OFF".equalsIgnoreCase(config.getProperty("java.logging")))
  		Logger.getLogger(GrpcServer.class.getPackageName()).setLevel(Level.OFF);

  	EmployeeService empService = new EmployeeService(Boolean.valueOf(config.getProperty("storeOnCreate")));
  	empService.loadDataSet(SAMPLE_DATA_SET);
  	
    BindableService employeeGrpcService = new EmployeeGrpcController(empService);

    server = serverBuilder
    		.addService(employeeGrpcService)
    		.addService(new SystemGrpcController())
    		.addService(ProtoReflectionService.newInstance()) // for service method discovery by client test tools
        .build();
  }
  
  private void loadConfigProperties() {
  	config = new Properties();
  	try (InputStream input = 
  			GrpcServer.class.getClassLoader().getResourceAsStream("application.properties")) {

			config.load(input);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
  	
  }
  
  /** Start serving requests. */
  public void start() throws IOException {
    server.start();
    System.out.println("Server started, listening on " + server.getPort());
    
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // Use stderr here since the logger may have been reset by its JVM shutdown hook.
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        try {
        	GrpcServer.this.stop();
        } catch (InterruptedException e) {
          e.printStackTrace(System.err);
        }
        System.err.println("*** server shut down");
      }
    });
  }

  /** Stop serving requests and shutdown resources. */
  public void stop() throws InterruptedException {
    if (server != null) {
      server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }
  }

  /**
   * Await termination on the main thread since the grpc library uses daemon threads.
   */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  /**
   * Main method.  This comment makes the linter happy.
   */
  public static void main(String[] args) throws Exception {
  	GrpcServer server = new GrpcServer(8980);
    server.start();
    server.blockUntilShutdown();
  }
}
