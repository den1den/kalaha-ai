package jfx2;

import javafx.concurrent.Service;

public abstract class RerunnableService<V> extends Service<V> {
    public void restart() {
        cancel();
        reset();
        start();
    }
}
