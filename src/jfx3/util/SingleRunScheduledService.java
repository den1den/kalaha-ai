package jfx3.util;

public abstract class SingleRunScheduledService<V> extends ScheduledService<V> {
    @Override
    protected void succeeded() {
        cancel();
        super.succeeded();
    }
}
