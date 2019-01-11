package org.springframework.init.aspects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class SpringFactoriesInterceptor {

	private static Log logger = LogFactory.getLog(SpringFactoriesInterceptor.class);

	@Around("execution(* org.springframework.core.io.support.SpringFactoriesLoader.loadFactories(..)) && args(factoryClass, classLoader)")
	public Object classes(ProceedingJoinPoint joinPoint, Class<?> factoryClass, ClassLoader classLoader) throws Throwable {
		return proceed(joinPoint);
	}

	@Around("execution(* org.springframework.core.io.support.SpringFactoriesLoader.loadFactoryNames(..)) && args(factoryClass, classLoader)")
	public Object names(ProceedingJoinPoint joinPoint,  Class<?> factoryClass, ClassLoader classLoader) throws Throwable {
		return proceed(joinPoint);
	}

	private Object proceed(ProceedingJoinPoint joinPoint) {
		try {
			Object result = joinPoint.proceed();
			logger.info(joinPoint.getSourceLocation().getWithinType() + ", " + joinPoint.toShortString() + ": " + result);
			return result;
		}
		catch (Throwable t) {
			logger.error(joinPoint.toShortString() + ": " + t);
			return null;
		}
	}

}
