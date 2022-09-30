package dev.rnd.rest.server.service;

public class CreateEmployeeResponse {

	private int employeeId;
	private Status errorStatus;
	
	public int getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(int employeeId) {
		this.employeeId = employeeId;
	}

	public Status getErrorStatus() {
		return errorStatus;
	}

	public void setErrorStatus(Status errorStatus) {
		this.errorStatus = errorStatus;
	}

	class Status {
		int code;
		String message;
	}
}
