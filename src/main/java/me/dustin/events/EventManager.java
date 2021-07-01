package me.dustin.events;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.events.exception.MethodNotPrivateException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EventManager {
    private ConcurrentMap<Class, Object> classes = new ConcurrentHashMap<>();
    private ConcurrentMap<Class<? extends Event>, List<Method>> events = new ConcurrentHashMap<>();
    protected boolean privateOnlyMode = false;
    /*
        Adds the class and object to the classes map
        Then adds all Methods using the @EventListener annotation to the events list for later use in the run method
     */
    public void register(Object object) throws MethodNotPrivateException {
        classes.put(object.getClass(), object);
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (privateOnlyMode) {
                if (method.isAccessible()) {
                    throw new MethodNotPrivateException("Method is not private: " + method.getName() + " in class " + method.getDeclaringClass());
                }
            }
            if (!method.isAccessible())
                method.setAccessible(true);
            if (method.isAnnotationPresent(EventListener.class)) {
                EventListener eventListener = method.getAnnotation(EventListener.class);
                for (Class<? extends Event> event : eventListener.events()) {
                    List<Method> list = new ArrayList<>();
                    events.putIfAbsent(event, list);
                    events.get(event).add(method);
                }
            }
        }
    }
    /*
        Removes the class and object from the classes map
        Then removes all methods using @EventListener annotation from the events list
     */
    public void unregister(Object object) {
        classes.remove(object.getClass());
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (!method.isAccessible())
                method.setAccessible(true);
            if (method.isAnnotationPresent(EventListener.class)) {
                EventListener eventListener = method.getAnnotation(EventListener.class);
                for (Class<? extends Event> event : eventListener.events()) {
                    List<Method> list = events.get(event);
                    if (list != null)
                        list.remove(method);
                }
            }
        }
    }

    public boolean alreadyRegistered(Object object) {
        return classes.containsKey(object.getClass());
    }

    /*
        Loops all methods subscribed to the event and invokes them
     */
    public void run(Event event) throws ConcurrentModificationException {
        List<Method> methods = events.get(event.getClass());
        if (methods != null) {
            //I really need a sorting system.
            for (int i = 5; i > 0; i--) {
                int finalI = i;
                methods.forEach(method -> {
                    if (method.getAnnotation(EventListener.class).priority() == finalI)
                        try {
                            method.invoke(classes.get(method.getDeclaringClass()), event);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                });
            }
        }
    }

}
