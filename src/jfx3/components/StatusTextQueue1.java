package jfx3.components;

import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class StatusTextQueue1 extends Service implements StatusTextQueue {
    private final static String QUEUE_BASE = "QUEUE_BASE";
    private final StringProperty text;
    private final AtomicReference<String> baseText = new AtomicReference<>();
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>(5);

    public StatusTextQueue1(StringProperty text) {
        this.text = text;
        baseText.set(text.get());
    }

    private void processQueue() {
        try {
            while (true) {
                String s = messageQueue.poll();
                while (s == null || QUEUE_BASE.equals(s)) {
                    s = baseText.get();
                    if (s == null) {
                        return;
                    }
                    text.set(s);
                    s = messageQueue.take();
                }
                text.set(s + " ...");
                synchronized (this) {
                    wait(1000);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setText(String text) {
        assert text != null;
        baseText.set(text);
        add(QUEUE_BASE);
    }

    @Override
    public void queue(String s) {
        assert !QUEUE_BASE.equals(s);
        add(s);
    }

    private void add(String s) {
        if (messageQueue.contains(s)) {
            return;
        }
        if (messageQueue.remainingCapacity() > 1) {
            messageQueue.offer(s);
        } else if (messageQueue.remainingCapacity() == 1) {
            try {
                messageQueue.put(s);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            messageQueue.poll();
            messageQueue.add(s);
        }
    }

    public void resetQueue(String s) {
        assert !QUEUE_BASE.equals(s);
        if (!messageQueue.contains(s)) {
            messageQueue.add(s);
        } else {
            messageQueue.add(s);
            if (text.get().equals(s)) {
                notify();
            }
        }
    }

    public void close() {
        baseText.set(null);
        messageQueue.clear();
        messageQueue.add(QUEUE_BASE);
    }

    @Override
    protected Task createTask() {
        return new Task() {
            @Override
            protected Object call() {
                processQueue();
                return null;
            }
        };
    }
}
