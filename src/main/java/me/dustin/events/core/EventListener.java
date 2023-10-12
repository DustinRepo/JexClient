package me.dustin.events.core;

import me.dustin.events.core.priority.Priority;
import net.jodah.typetools.TypeResolver;

import java.util.function.Predicate;

public class EventListener<T> implements EventHook<T> {

	/*
	 * Event class
	 */
	private final Class<T> eventClass;
	
	/*
	 * Hook that actually runs the code
	 */
	private final EventHook<T> hook;

	/*
	 * Filters for the events
	 */
	private final Predicate<T>[] filters;
	
	/*
	 * Priority on time to be ran
	 */
	private final int priority;

	@SafeVarargs
	public EventListener(EventHook<T> hook, Predicate<T>... filters){
		this(hook, Priority.DEFAULT, filters);
	}

	@SafeVarargs
	@SuppressWarnings("unchecked")//intellij doesn't like the cast to Class<T>
	public EventListener(EventHook<T> hook, int priority, Predicate<T>... filters){
		this.hook = hook;
		this.eventClass = (Class<T>) TypeResolver.resolveRawArgument(EventHook.class, hook.getClass());
		this.priority = priority;
		this.filters = filters;
	}

	public Class<T> getEventClass(){
		return eventClass;
	}
	
	public EventHook<T> getHook(){
		return hook;
	}
	
	public int getPriority(){
		return priority;
	}
	
	/*
	 * Runs the code in the field
	 */
	@Override
	public void invoke(T event) {
		try {
			if (filters != null && filters.length > 0) {
				for (Predicate<T> filter : filters) {
					if (!filter.test(event))
						return;
				}
			}
			this.hook.invoke(event);
		} catch (ClassCastException e) {
			return;
		}
	}

}
