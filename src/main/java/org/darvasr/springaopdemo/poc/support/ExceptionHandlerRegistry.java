package org.darvasr.springaopdemo.poc.support;

import jakarta.annotation.PostConstruct;
import org.darvasr.springaopdemo.poc.annotation.GlobalExceptionHandler;
import org.darvasr.springaopdemo.poc.annotation.Handles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Dispatcher: at startup it collects the {@link Handles} methods of the
 * {@link GlobalExceptionHandler} beans, and at runtime selects the most specific
 * (closest ancestor) handler for a given exception type.
 */
@Component
public class ExceptionHandlerRegistry {

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerRegistry.class);

    /**
     * A registered handler method: the containing bean, the method to invoke, and the
     * handled exception type declared by {@link Handles}.
     */
    public record HandlerMethod(Object bean, Method method, Class<? extends Throwable> handledType) {
    }

    private final ApplicationContext applicationContext;
    private final Map<Class<? extends Throwable>, HandlerMethod> handlers = new HashMap<>();

    public ExceptionHandlerRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    void registerHandlers() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(GlobalExceptionHandler.class);
        for (Object bean : beans.values()) {
            Class<?> userClass = ClassUtils.getUserClass(bean);
            for (Method method : userClass.getMethods()) {
                Handles annotation = method.getAnnotation(Handles.class);
                if (annotation == null) {
                    continue;
                }
                Class<? extends Throwable> type = annotation.value();
                HandlerMethod previous = handlers.put(type, new HandlerMethod(bean, method, type));
                if (previous != null) {
                    log.warn("Multiple handlers registered for the same exception type: {} ({} overrides {})",
                            type.getName(), method, previous.method());
                }
            }
        }
        log.info("{} exception handler method(s) registered", handlers.size());
    }

    /**
     * Walks up the superclass chain from the thrown exception type and returns the
     * most specific matching handler.
     *
     * @param thrownType the thrown exception class (not {@code null})
     * @return the most specific {@link HandlerMethod}, or {@link Optional#empty()} if
     * there is no match
     */
    public Optional<HandlerMethod> resolve(Class<? extends Throwable> thrownType) {
        Class<?> current = thrownType;
        while (current != null && Throwable.class.isAssignableFrom(current)) {
            HandlerMethod handler = handlers.get(current);
            if (handler != null) {
                return Optional.of(handler);
            }
            current = current.getSuperclass();
        }
        return Optional.empty();
    }
}
