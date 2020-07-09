package core.mvc.tobe;

import com.google.common.collect.Lists;
import core.annotation.web.ExceptionHandler;
import core.annotation.web.RequestMapping;
import core.mvc.tobe.support.ArgumentResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.util.*;

public class HandlerConverter {

    private static final Logger logger = LoggerFactory.getLogger(HandlerConverter.class);

    private List<ArgumentResolver> argumentResolvers = Lists.newArrayList();

    private static final ParameterNameDiscoverer nameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    public void setArgumentResolvers(List<ArgumentResolver> argumentResolvers) {
        this.argumentResolvers.addAll(argumentResolvers);
    }

    public void addArgumentResolver(ArgumentResolver argumentResolver) {
        this.argumentResolvers.add(argumentResolver);
    }

    public Map<HandlerKey, HandlerExecution> convert(Map<Class<?>, Object> controllers) {
        Map<HandlerKey, HandlerExecution> handlers = new LinkedHashMap<>();
        Set<Class<?>> controllerClazz = controllers.keySet();
        for (Class<?> controller : controllerClazz) {
            Object target = controllers.get(controller);
            addHandlerExecution(handlers, target, controller.getMethods());
        }

        return sortHandlers(handlers);
    }

    public Map<Class<? extends Throwable>, HandlerExecution> convertAdvices(Map<Class<?>, Object> controllerAdvices) {
        Map<Class<? extends Throwable>, HandlerExecution> handlers = new LinkedHashMap<>();

        for (Class<?> controllerAdvice : controllerAdvices.keySet()) {
            Object target = controllerAdvices.get(controllerAdvice);
            addExceptionHandlerExecution(handlers, target, controllerAdvice.getMethods());
        }

        return handlers;
    }

    private void addExceptionHandlerExecution(Map<Class<? extends Throwable>, HandlerExecution> handlers, Object target, Method[] methods) {
        Arrays.stream(methods)
                .filter(method -> method.isAnnotationPresent(ExceptionHandler.class))
                .forEach(method -> {
                    ExceptionHandler exceptionHandler = method.getAnnotation(ExceptionHandler.class);
                    Class<? extends Throwable>[] exceptions = exceptionHandler.value();
                    HandlerExecution handlerExecution = new HandlerExecution(nameDiscoverer, argumentResolvers, target, method);
                    addExceptionHandlerExecution(handlers, exceptions, handlerExecution);
                });
    }

    private void addExceptionHandlerExecution(Map<Class<? extends Throwable>, HandlerExecution> handlers,
                                              Class<? extends Throwable>[] exceptions,
                                              HandlerExecution handlerExecution) {
        Arrays.stream(exceptions)
                .forEach(exception -> {
                    handlers.put(exception, handlerExecution);
                    logger.info("Add - exception: {}, HandlerExecution: {}", exception, handlerExecution);
                });
    }

    private void addHandlerExecution(Map<HandlerKey, HandlerExecution> handlers, final Object target, Method[] methods) {
        Arrays.stream(methods)
                .filter(method -> method.isAnnotationPresent(RequestMapping.class))
                .forEach(method -> {
                    RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                    HandlerKey handlerKey = new HandlerKey(requestMapping.value(), requestMapping.method());
                    HandlerExecution handlerExecution = new HandlerExecution(nameDiscoverer, argumentResolvers, target, method);
                    handlers.put(handlerKey, handlerExecution);
                    logger.info("Add - method: {}, path: {}, HandlerExecution: {}", requestMapping.method(), requestMapping.value(), method.getName());
                });
    }

    private Map<HandlerKey, HandlerExecution> sortHandlers(Map<HandlerKey, HandlerExecution> handlers) {
        Map<HandlerKey, HandlerExecution> sortedHandlers = new LinkedHashMap<>();
        List<HandlerKey> handlerKeys = new ArrayList<>(handlers.keySet());
        Collections.sort(handlerKeys);
        for (HandlerKey handlerKey : handlerKeys) {
            sortedHandlers.put(handlerKey, handlers.get(handlerKey));
        }

        return sortedHandlers;
    }

}
