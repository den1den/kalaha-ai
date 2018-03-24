package jfx3.opponent;

import javafx.util.Duration;
import jfx3.util.SingleRunScheduledService;

import java.io.Closeable;

abstract class ConnectionService<O extends Closeable>
    extends SingleRunScheduledService<ConnectionService.Result<O>>
    implements Closeable {

    O newObj = null;
    private O lastObj = null;
    private boolean disconnectObj = false;

    ConnectionService() {
        setPeriod(Duration.seconds(1));
        setMaximumCumulativePeriod(Duration.seconds(3));
        setBackoffStrategy(EXPONENTIAL_BACKOFF_STRATEGY);
        valueProperty().addListener(this::objUpdate);
    }

    public void disconnect() {
        cancel();
        disconnectObj = true;
        restart();
    }

    void setNextObj(O newObj) {
        this.newObj = newObj;
    }

    void onUpdate(O newObject) {
    }

    @Override
    public void close() {
        cancel();
        disconnectObj = true;
        newObj = null;
        restart();
    }

    @Override
    protected javafx.concurrent.Task<Result<O>> createTask() {
        return createTaskImpl();
    }

    protected abstract Task createTaskImpl();

    private void objUpdate(Object observable, Result<O> oldValue, Result<O> newValue) {
        System.out.println("ConnectionService.objUpdate oldValue = [" + oldValue + "], " +
            "newValue = [" + newValue + "]");
        if (newValue == null) {
            return;
        }
        if (newValue.object != null) {
            lastObj = newValue.object;
            newObj = null;
            System.out.println("ConnectionService.objUpdate: lastObj <- newObject");
            onUpdate(lastObj);
        }
    }

    static class Result<Obj> extends jfx3.opponent.Result {
        final private Obj object;

        Result(Obj object) {
            super(-1);
            this.object = object;
        }

        Result(Obj object, int result) {
            super(result);
            this.object = object;
        }

        Result(int result) {
            super(result);
            this.object = null;
        }
    }

    abstract class Task extends javafx.concurrent.Task<Result<O>> {
        private final O newObj = ConnectionService.this.newObj;
        private final boolean disconnectObj = ConnectionService.this.disconnectObj;
        private O object = ConnectionService.this.lastObj;
        private boolean clientChanged = false;

        {
            assert !disconnectObj || object != null;
        }

        @Override
        protected Result<O> call() throws Exception {
            if (disconnectObj) {
                object.close();
                object = null;
                clientChanged = true;
            }
            if (newObj != null) {
                // Setup a new object connection
                if (object != null) {
                    object.close();
                }
                object = newObj;
                clientChanged = true;
            }
            if (clientChanged) {
                System.out.println("Task.call.clientChanged");
                updateValue(new Result<>(object));
            }
            if (object == null) {
                return null;
            }
            System.out.println("Task.callImpl");
            return callImpl(object);
        }

        Result<O> sendResult(int result) {
            if (clientChanged) {
                return new Result<>(object, result);
            } else {
                return new Result<>(result);
            }
        }

        protected abstract Result<O> callImpl(O object) throws Exception;
    }
}
