package org.darvasr.springaopdemo.poc.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.darvasr.springaopdemo.poc.support.ExceptionHandlerRegistry;
import org.darvasr.springaopdemo.poc.support.ExceptionHandlerRegistry.HandlerMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Central exception-handling aspect. It intercepts POC service-layer method calls
 * with an {@code @Around} advice and dispatches the thrown exception to the
 * {@code @Handles} handler selected by {@link ExceptionHandlerRegistry}, then adapts
 * the result to the original method's declared return type.
 *
 * <p>Not HTTP-bound: it works on any Spring bean under the pointcut and does not
 * reference a controller or an HTTP {@code Response} object.
 */
@Aspect
@Component
public class ExceptionHandlingAspect {

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlingAspect.class);

    private final ExceptionHandlerRegistry registry;

    public ExceptionHandlingAspect(ExceptionHandlerRegistry registry) {
        this.registry = registry;
    }

    @Around("execution(* org.darvasr.springaopdemo.poc.service..*(..))")
    public Object handle(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = signature.getReturnType();

        if (isReactive(returnType)) {
            return handleReactive(joinPoint, returnType);
        }
        return handleSynchronous(joinPoint, signature, returnType);
    }

    private Object handleSynchronous(ProceedingJoinPoint joinPoint, MethodSignature signature, Class<?> returnType)
            throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            HandlerMethod handler = resolveOrThrow(ex, signature);
            Object result = invokeHandler(handler, ex);
            if (returnType == void.class || returnType == Void.class) {
                return null;
            }
            if (result != null && !returnType.isInstance(result)) {
                String message = "Handler (%s) result (%s) is not assignment-compatible with the method return type (%s)"
                        .formatted(handler.method(), result.getClass().getName(), returnType.getName());
                log.error(message);
                throw new IllegalStateException(message);
            }
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    private Object handleReactive(ProceedingJoinPoint joinPoint, Class<?> returnType) throws Throwable {
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            // Synchronous throw during stream assembly.
            return reactiveFallback(ex, returnType, joinPoint).orElseThrow(() -> sneaky(ex));
        }

        if (result instanceof Mono<?> mono) {
            return ((Mono<Object>) mono).onErrorResume(err -> reactiveFallback(err, returnType, joinPoint)
                    .map(Mono::from)
                    .orElseGet(() -> Mono.error(err)));
        }
        if (result instanceof Flux<?> flux) {
            return ((Flux<Object>) flux).onErrorResume(err -> reactiveFallback(err, returnType, joinPoint)
                    .map(Flux::from)
                    .orElseGet(() -> Flux.error(err)));
        }
        return result;
    }

    /**
     * Produces a reactive fallback stream for the exception, or empty if there is no
     * matching handler.
     */
    private Optional<org.reactivestreams.Publisher<Object>> reactiveFallback(
            Throwable err, Class<?> returnType, ProceedingJoinPoint joinPoint) {
        Optional<HandlerMethod> handler = registry.resolve(throwableType(err));
        if (handler.isEmpty()) {
            return Optional.empty();
        }
        log.info("Handled reactive exception: {} -> {}", joinPoint.getSignature(), handler.get().method());
        Object handlerResult = invokeHandlerUnchecked(handler.get(), err);
        return Optional.of(toPublisher(handlerResult, returnType));
    }

    @SuppressWarnings("unchecked")
    private org.reactivestreams.Publisher<Object> toPublisher(Object handlerResult, Class<?> returnType) {
        boolean wantFlux = Flux.class.isAssignableFrom(returnType);
        if (handlerResult instanceof Mono<?> mono) {
            return wantFlux ? ((Mono<Object>) mono).flux() : (Mono<Object>) mono;
        }
        if (handlerResult instanceof Flux<?> flux) {
            return wantFlux ? (Flux<Object>) flux : ((Flux<Object>) flux).next();
        }
        if (handlerResult == null) {
            return wantFlux ? Flux.empty() : Mono.empty();
        }
        return wantFlux ? Flux.just(handlerResult) : Mono.just(handlerResult);
    }

    private HandlerMethod resolveOrThrow(Throwable ex, MethodSignature signature) throws Throwable {
        Optional<HandlerMethod> handler = registry.resolve(throwableType(ex));
        if (handler.isEmpty()) {
            // Selective handling: rethrow an exception that has no handler unchanged.
            throw ex;
        }
        log.info("Handled exception: {} -> {}", signature, handler.get().method());
        return handler.get();
    }

    private Object invokeHandler(HandlerMethod handler, Throwable ex) throws Throwable {
        Method method = handler.method();
        method.setAccessible(true);
        try {
            return method.invoke(handler.bean(), ex);
        } catch (InvocationTargetException ite) {
            throw ite.getCause() != null ? ite.getCause() : ite;
        }
    }

    private Object invokeHandlerUnchecked(HandlerMethod handler, Throwable ex) {
        try {
            return invokeHandler(handler, ex);
        } catch (Throwable t) {
            throw sneaky(t);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Throwable> throwableType(Throwable ex) {
        return (Class<? extends Throwable>) ex.getClass();
    }

    private boolean isReactive(Class<?> returnType) {
        return Mono.class.isAssignableFrom(returnType) || Flux.class.isAssignableFrom(returnType);
    }

    private RuntimeException sneaky(Throwable t) {
        if (t instanceof RuntimeException re) {
            return re;
        }
        return new RuntimeException(t);
    }
}
