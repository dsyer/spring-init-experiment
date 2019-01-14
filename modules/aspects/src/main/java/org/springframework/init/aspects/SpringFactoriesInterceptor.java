package org.springframework.init.aspects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.init.factory.SpringFactoriesListener;

@Aspect
public class SpringFactoriesInterceptor {

	private static Log logger = LogFactory.getLog(SpringFactoriesInterceptor.class);

	@Around("execution(* org.springframework.core.io.support.SpringFactoriesLoader.loadFactoryNames(..)) && args(factoryClass, classLoader) && !within(org.springframework.init..*)")
	public Object names(ProceedingJoinPoint joinPoint,  Class<?> factoryClass, ClassLoader classLoader) throws Throwable {
		return proceed(joinPoint, factoryClass);
	}

	private Object proceed(ProceedingJoinPoint joinPoint, Class<?> factoryClass) {
		try {
			Object result = SpringFactoriesListener.loadFactoryNames(factoryClass);
			logger.info(joinPoint.getTarget() + ", " + joinPoint.toShortString() + ": " + result);
			return result;
		}
		catch (Throwable t) {
			logger.error(joinPoint.toShortString() + ": " + t);
			return null;
		}
	}

}