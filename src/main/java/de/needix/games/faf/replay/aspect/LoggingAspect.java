package de.needix.games.faf.replay.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);

    // Cache for storing and reusing logger instances
    private final ConcurrentHashMap<Class<?>, Logger> loggerCache = new ConcurrentHashMap<>();

    // Pointcut to match all methods in the specified package
    @Pointcut("execution(* de.needix.games.faf..*(..))")
    public void applicationPackagePointcut() {
        // Pointcut method (empty body)
    }

    // Log method entry
    @Before("applicationPackagePointcut()")
    public void logMethodEntry(JoinPoint joinPoint) {
        // Retrieve or compute the logger for the target class using the cache
        Logger logger = loggerCache.computeIfAbsent(
                joinPoint.getTarget().getClass(),
                LoggerFactory::getLogger
        );

        String methodName = joinPoint.getSignature().getName();
        Object[] methodArgs = joinPoint.getArgs();
        logger.trace("Entering method: {} with arguments: {}", methodName, Arrays.toString(methodArgs));
    }

    // Log method exit with return value
    @AfterReturning(pointcut = "applicationPackagePointcut()", returning = "result")
    public void logMethodExit(JoinPoint joinPoint, Object result) {
        // Retrieve or compute the logger for the target class using the cache
        Logger logger = loggerCache.computeIfAbsent(
                joinPoint.getTarget().getClass(),
                LoggerFactory::getLogger
        );

        String methodName = joinPoint.getSignature().getName();
        logger.trace("Exiting method: {} with return: {}", methodName, result);
    }
}