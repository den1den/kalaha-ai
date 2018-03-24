package jfx3.util;

public abstract class ScheduledService<V> extends javafx.concurrent.ScheduledService<V> {
    @Override
    protected void succeeded() {
        System.out.println(getClass().getSimpleName() + ".succeeded");
        super.succeeded();
    }

    @Override
    protected void failed() {
        System.out.println(getClass().getSimpleName() + ".failed");
        super.failed();
    }

    public void kick() {
        if (!isRunning()) {
            reset();
            start();
        }
    }

    public void close() {
        cancel();
    }
}
