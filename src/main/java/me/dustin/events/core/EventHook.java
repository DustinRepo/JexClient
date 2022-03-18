package me.dustin.events.core;

public interface EventHook<T> {

	/*
	 * This is the method that gets called to run all of the code in each field you make for your event
	 */
	void invoke(T event);
	
}
