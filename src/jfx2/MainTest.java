package jfx2;


import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicInteger;

public class MainTest extends Application {
    Service a = new AService();
    Service b = new BService();
    AtomicInteger finished = new AtomicInteger(0);

    public static void main(String[] args) {
        launch();
    }

    private void run() {
        EventHandler<WorkerStateEvent> h = event -> {
            if (finished.addAndGet(1) == 2) {
                System.out.println("Done");
            }
        };
        a.setOnSucceeded(h);
        b.setOnSucceeded(h);
        a.cancel();
        a.reset();
        a.start();
        b.cancel();
        b.reset();
        b.start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //Parent root = FXMLLoader.load(getClass().getResource("jfx2/main.fxml"));
        primaryStage.setTitle("Marble game");
        //primaryStage.setScene(new Scene());
        //primaryStage.show();

        run();
    }

    private class AService extends Service {
        @Override
        protected Task createTask() {
            return new Task() {
                @Override
                protected Object call() throws Exception {
                    synchronized (AService.this) {
                        AService.this.wait(1000);
                        System.out.println("A finished");
                    }
                    return null;
                }
            };
        }
    }

    private class BService extends Service {
        @Override
        protected Task createTask() {
            return new Task() {
                @Override
                protected Object call() throws Exception {
                    synchronized (BService.this) {
                        BService.this.wait(3000);
                        System.out.println("B finished");
                    }
                    return null;
                }
            };
        }
    }
}
