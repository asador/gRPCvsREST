package dev.rnd.grpc.server.controller;

import com.google.protobuf.Empty;

import dev.rnd.grpc.system.CpuUsageProto;
import dev.rnd.grpc.system.Number;
import dev.rnd.grpc.system.SystemGrpcServiceGrpc.SystemGrpcServiceImplBase;
import dev.rnd.util.CpuUsage;
import dev.rnd.util.CpuUsageCalculator;
import io.grpc.stub.StreamObserver;

public class SystemGrpcController extends SystemGrpcServiceImplBase  {

	private CpuUsageCalculator cpuUsageCalculator;
	
	public SystemGrpcController(int cpuTimeSampleInterval) {
		cpuUsageCalculator = new CpuUsageCalculator(cpuTimeSampleInterval);
	}

	@Override
	public void startCpuUsageMonitor(Empty request, StreamObserver<Empty> responseObserver) {
		cpuUsageCalculator.start();
		responseObserver.onNext(Empty.newBuilder().build());
		responseObserver.onCompleted();
	}

	@Override
	public void getThreadCount(Empty request, StreamObserver<Number> responseObserver) {
		Number number = Number.newBuilder().setNum(CpuUsageCalculator.getThreadCount()).build();
		
		responseObserver.onNext(number);
		responseObserver.onCompleted();
	}

	@Override
	public void stopAndGetCpuUsage(Empty request, StreamObserver<CpuUsageProto> responseObserver) {
		CpuUsage cpuUsage = cpuUsageCalculator.stopAndGetCpuUsage();
		CpuUsageProto cpuProto = CpuUsageProto.newBuilder()
				.setDuration(cpuUsage.getDurationMillis())
				.setTotalCpuTime(cpuUsage.getTotalCpuTimeMillis())
				.setUtilization(cpuUsage.getCpuUtilizationPercentage())
				.build();
		
		responseObserver.onNext(cpuProto);
		responseObserver.onCompleted();
	}
	
}
