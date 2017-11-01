package com.github.qzagarese.dockerunit.exception;

import lombok.Getter;

public class ContainerException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5756816334592174406L;

	@Getter
	private final String containerId;
	
	public ContainerException(String containerId, Throwable cause) {
		super(cause.getMessage(), cause);
		this.containerId = containerId;
	}
	
	
}
