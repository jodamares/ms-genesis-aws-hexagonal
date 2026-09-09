package com.tdp.ms.shared.infrastructure.bus;

import com.tdp.ms.shared.domain.bus.command.Command;
import com.tdp.ms.shared.domain.bus.command.CommandBus;
import com.tdp.ms.shared.domain.bus.command.CommandHandler;
import java.util.HashMap;
import java.util.Map;
import org.springframework.aop.template.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class InMemoryCommandBus implements CommandBus {
    private final Map<Class<? extends Command>, CommandHandler<Command>> handlers = new HashMap<>();

    @SuppressWarnings("unchecked")
    public InMemoryCommandBus(ApplicationContext context) {
        String[] beanNames = context.getBeanNamesForType(CommandHandler.class);
        for (String beanName : beanNames) {
            CommandHandler<Command> handler = (CommandHandler<Command>) context.getBean(beanName);
            handlers.put(resolveCommandType(handler), handler);
        }
    }

    @Override
    public Mono<Void> dispatch(Command command) {
        CommandHandler<Command> handler = handlers.get(command.getClass());
        if (handler == null) {
            return Mono.error(new IllegalStateException("No CommandHandler registered for " + command.getClass().getName()));
        }
        return handler.handle(command);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Command> resolveCommandType(CommandHandler<?> handler) {
        Class<?> targetClass = AopUtils.getTargetClass(handler);
        ResolvableType type = ResolvableType.forClass(targetClass).as(CommandHandler.class);
        Class<?> resolved = type.getGeneric(0).resolve();
        if (resolved == null || !Command.class.isAssignableFrom(resolved)) {
            throw new IllegalStateException("Cannot resolve Command type for handler " + targetClass.getName());
        }
        return (Class<? extends Command>) resolved;
    }
}
