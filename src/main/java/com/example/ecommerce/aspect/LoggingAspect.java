package com.example.ecommerce.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.example.ecommerce.service..*(..))")
    public Object logServiceMethods(
            ProceedingJoinPoint joinPoint) throws Throwable {

        String methodName =
                joinPoint.getSignature().toShortString();

        long startTime = System.currentTimeMillis();

        System.out.println(
                "START: " + methodName
        );

        try {

            Object result = joinPoint.proceed();

            long executionTime =
                    System.currentTimeMillis() - startTime;

            System.out.println(
                    "SUCCESS: " + methodName +
                            " | Time: " + executionTime + " ms"
            );

            return result;

        } catch (Exception e) {

            long executionTime =
                    System.currentTimeMillis() - startTime;

            System.out.println(
                    "FAILED: " + methodName +
                            " | Time: " + executionTime + " ms" +
                            " | Error: " + e.getMessage()
            );

            throw e;
        }
    }
}