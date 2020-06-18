package com.santander.efx.exception;

public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = 6769798308099674356L;

	public ServiceException() {
		super("Some error occurred");
		printStackTrace();
	}

}
