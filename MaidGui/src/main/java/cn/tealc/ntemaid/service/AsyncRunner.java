package cn.tealc.ntemaid.service;

import com.google.inject.Singleton;
import javafx.application.Platform;

@Singleton
public class AsyncRunner {

    public void runBackground(Runnable task) {
        Thread.startVirtualThread(task);
    }

    public void runOnUI(Runnable task) {
        Platform.runLater(task);
    }
}
