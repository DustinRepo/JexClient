package me.dustin.jex.helper.misc;

public class Timer {

    private long currentMS = 0L;
    private long lastMS = -1L;

    public void update() {
        currentMS = System.currentTimeMillis();
    }

    public void reset() {
        lastMS = System.currentTimeMillis();
    }

    public boolean hasPassed(long MS) {
        update();
        return currentMS >= lastMS + MS;
    }

    public long getPassed() {
        update();
        return currentMS - lastMS;
    }

    public long getCurrentMS() {
        return currentMS;
    }

    public long getLastMS() {
        return lastMS;
    }
}
