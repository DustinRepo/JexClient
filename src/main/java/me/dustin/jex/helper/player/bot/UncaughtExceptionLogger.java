package me.dustin.jex.helper.player.bot;

import org.apache.logging.log4j.Logger;

import java.lang.Thread.UncaughtExceptionHandler;

public class UncaughtExceptionLogger implements UncaughtExceptionHandler {
    private final Logger logger;

    public UncaughtExceptionLogger(Logger logger) {
        this.logger = logger;
    }

    public void uncaughtException(Thread thread, Throwable throwable) {
        this.logger.error("Caught previously unhandled exception :", throwable);
    }
}
