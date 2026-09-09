package com.tdp.ms.shared.infrastructure.bus;

import com.tdp.ms.shared.domain.bus.query.Query;
import com.tdp.ms.shared.domain.bus.query.QueryBus;
import com.tdp.ms.shared.domain.bus.query.QueryHandler;
import com.tdp.ms.shared.domain.bus.query.Response;
import java.util.HashMap;
import java.util.Map;
import org.springframework.aop.template.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class InMemoryQueryBus implements QueryBus {
    private final Map<Class<? extends Query>, QueryHandler<Query, Response>> handlers = new HashMap<>();

    @SuppressWarnings("unchecked")
    public InMemoryQueryBus(ApplicationContext context) {
        String[] beanNames = context.getBeanNamesForType(QueryHandler.class);
        for (String beanName : beanNames) {
            QueryHandler<Query, Response> handler = (QueryHandler<Query, Response>) context.getBean(beanName);
            handlers.put(resolveQueryType(handler), handler);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R extends Response> Mono<R> ask(Query query) {
        QueryHandler<Query, Response> handler = handlers.get(query.getClass());
        if (handler == null) {
            return Mono.error(new IllegalStateException("No QueryHandler registered for " + query.getClass().getName()));
        }
        return (Mono<R>) handler.handle(query);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Query> resolveQueryType(QueryHandler<?, ?> handler) {
        Class<?> targetClass = AopUtils.getTargetClass(handler);
        ResolvableType type = ResolvableType.forClass(targetClass).as(QueryHandler.class);
        Class<?> resolved = type.getGeneric(0).resolve();
        if (resolved == null || !Query.class.isAssignableFrom(resolved)) {
            throw new IllegalStateException("Cannot resolve Query type for handler " + targetClass.getName());
        }
        return (Class<? extends Query>) resolved;
    }
}
