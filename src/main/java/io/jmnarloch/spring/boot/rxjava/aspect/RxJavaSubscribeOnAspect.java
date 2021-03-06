/**
 * Copyright (c) 2015-2016 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jmnarloch.spring.boot.rxjava.aspect;

import io.jmnarloch.spring.boot.rxjava.annotation.Scheduler;
import io.jmnarloch.spring.boot.rxjava.annotation.SubscribeOn;
import io.jmnarloch.spring.boot.rxjava.metadata.AnnotationInspector;
import io.jmnarloch.spring.boot.rxjava.metadata.MetadataExtractor;
import io.jmnarloch.spring.boot.rxjava.subscribable.Subscribables;
import io.jmnarloch.spring.boot.rxjava.utils.RxJavaUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.schedulers.Schedulers;

import static io.jmnarloch.spring.boot.rxjava.utils.AopUtils.getJointPointMethod;
import static io.jmnarloch.spring.boot.rxjava.utils.AopUtils.isNull;

/**
 * The aspect that subscribes the return value of method annotated with {@link SubscribeOn}.
 *
 * @author Jakub Narloch
 * @see SubscribeOn
 */
@Aspect
public class RxJavaSubscribeOnAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(RxJavaSubscribeOnAspect.class);

    @Pointcut("@annotation(io.jmnarloch.spring.boot.rxjava.annotation.SubscribeOn)")
    public void rxjavaSubscribeOnPointcut() {
    }

    @Around("rxjavaSubscribeOnPointcut()")
    public Object subscribeOnAdvice(ProceedingJoinPoint joinPoint) throws Throwable {

        try {
            final AnnotationInspector<SubscribeOn> inspector = MetadataExtractor.extractFrom(
                    getJointPointMethod(joinPoint)
            ).subscribeOn();

            if(!isValidMethodDefinition(inspector)) {
                return skip(joinPoint);
            }
            return process(inspector, joinPoint);
        } catch (Throwable exc) {
            throw exc;
        }
    }

    private Object skip(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    private Object process(AnnotationInspector<SubscribeOn> inspector, ProceedingJoinPoint joinPoint) throws Throwable {
        final Object result = joinPoint.proceed();
        if (isNull(result)) {
            return result;
        }
        return subscribeOn(inspector, result);
    }

    private Object subscribeOn(AnnotationInspector<SubscribeOn> inspector, Object result) {
        final rx.Scheduler scheduler = getScheduler(
                inspector.getAnnotation().value()
        );
        return Subscribables.toSubscribable(result)
                .subscribeOn(scheduler)
                .unwrap();
    }

    private boolean isValidMethodDefinition(AnnotationInspector<SubscribeOn> inspector) {
        if (!inspector.isAnnotationPresent()) {
            LOGGER.warn("The @SubscribeOn annotation wasn't present");
            return false;
        }

        final Class<?> returnType = inspector.getMethod().getReturnType();
        if (!RxJavaUtils.isRxJavaType(returnType)) {
            LOGGER.warn("The @SubscribeOn annotated method return type has to be either Observable or Single");
            return false;
        }

        return true;
    }

    private rx.Scheduler getScheduler(Scheduler value) {
        switch (value) {
            case IMMEDIATE:
                return Schedulers.immediate();
            case TRAMPOLINE:
                return Schedulers.trampoline();
            case NEW_THREAD:
                return Schedulers.newThread();
            case COMPUTATION:
                return Schedulers.computation();
            case IO:
                return Schedulers.io();
            default:
                throw new IllegalStateException("The Scheduler definition could not be mapped");
        }
    }
}
