package com.github.qzagarese.dockerunit.internal.lifecycle;

import java.lang.reflect.Method;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.github.qzagarese.dockerunit.MicrounitRunner;
import com.github.qzagarese.dockerunit.ServiceContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InvokeMicrounitMethod extends Statement {

	private final FrameworkMethod testMethod;
	private final Object target;
	private final MicrounitRunner runner;
	
	@Override
	public void evaluate() throws Throwable {
		Method method = testMethod.getMethod();
		if (method.getParameterCount() == 0) {
			testMethod.invokeExplosively(target);
		} else if (method.getParameterCount() == 1
				&& method.getParameterTypes()[0].isAssignableFrom(ServiceContext.class)) {
			ServiceContext context = runner.getContext(testMethod);
			testMethod.invokeExplosively(target, context);
		} else {
			throw new IllegalArgumentException("Test methods must have either zero arguments or one argument of type "
					+ ServiceContext.class.getName());
		}
	}

}
