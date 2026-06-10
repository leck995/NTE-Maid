package cn.tealc.ntemaid.ui.system.update;


import cn.tealc.ntemaid.base.AppConstants;
import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.model.system.realease.Release;
import cn.tealc.ntemaid.thread.system.update.AppUpdateDownloadTask;
import cn.tealc.ntemaid.util.LanguageManager;
import cn.tealc.teafx.utils.ResponseBody;
import cn.tealc.teafx.utils.message.MessageInfo;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @description:
 * @author: Leck
 * @create: 2024-10-27 23:45
 */
public class UpdateViewModel implements ViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateViewModel.class);
    private final SimpleStringProperty version = new SimpleStringProperty();
    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleStringProperty description = new SimpleStringProperty();
    private final SimpleStringProperty type = new SimpleStringProperty();
    private final SimpleStringProperty dateTime = new SimpleStringProperty();
    private final SimpleDoubleProperty progressValue = new SimpleDoubleProperty();
    private final SimpleStringProperty progressLabel = new SimpleStringProperty();
    private final SimpleStringProperty packageSize = new SimpleStringProperty();
    private final SimpleBooleanProperty force = new SimpleBooleanProperty();
    private final SimpleStringProperty forceLabel = new SimpleStringProperty();
    private final SimpleBooleanProperty downloading = new SimpleBooleanProperty(false);
    private final Release release;

    public UpdateViewModel(Release release) {
        this.release = release;
        version.set(String.format("V%s -> V%s", AppConstants.VERSION, release.getVersion()));
        name.set(release.getName());
        description.set(release.getDescription());
        dateTime.set(release.getDate());
        type.set(release.isPre() ? LanguageManager.getString("ui.update.left.grid.type.key02") : LanguageManager.getString("ui.update.left.grid.type.key01"));
        forceLabel.set(release.isForce() ? LanguageManager.getString("ui.update.left.grid.force.key01") : LanguageManager.getString("ui.update.left.grid.force.key02"));
    }


    public void downloadZip() {
        progressValue.unbind();
        progressLabel.unbind();
        packageSize.unbind();
        AppUpdateDownloadTask task = new AppUpdateDownloadTask(release);
        progressValue.bind(task.progressProperty());
        progressLabel.bind(task.progressProperty().multiply(100).asString("%.2f%%"));
        packageSize.bind(task.titleProperty());
        downloading.set(true);
        task.setOnSucceeded(event -> {
            downloading.set(false);
            ResponseBody<Boolean> value = task.getValue();
            if (value.getCode() == 200) {
                NotificationManager.message(MessageInfo.success("更新完成，若重启失败请手动重启"));
                restart();
            } else {
                NotificationManager.message(MessageInfo.warning(value.getMsg()));
            }

        });
        Thread.startVirtualThread(task);
    }


    private void restart() {
        Optional<String> exePath = ProcessHandle.current().info().command();
        if (exePath.isEmpty()) {
            LOG.error("无法获取当前进程路径");
            return;
        }
        try {
            Path bat = Path.of(System.getProperty("java.io.tmpdir"), "ntemaid_restart.bat");
            String script = "@echo off\r\ntimeout /t 2 /nobreak >nul\r\nstart \"\" \"" + exePath.get() + "\"\r\ndel \"%~f0\"";
            Files.writeString(bat, script);
            new ProcessBuilder("cmd", "/c", "start", "/min", "", bat.toString()).start();
            NotificationManager.publish(NotificationKey.APP_EXIT);
            Platform.exit();
        } catch (IOException e) {
            LOG.error("重启失败", e);
        }
    }

    public void setSkipVersion() {
        Config.getSetting().setSkipVersion(release.getVersion());
    }

    public String getVersion() {
        return version.get();
    }

    public SimpleStringProperty versionProperty() {
        return version;
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public String getType() {
        return type.get();
    }

    public SimpleStringProperty typeProperty() {
        return type;
    }

    public String getDateTime() {
        return dateTime.get();
    }

    public SimpleStringProperty dateTimeProperty() {
        return dateTime;
    }

    public double getProgressValue() {
        return progressValue.get();
    }

    public SimpleDoubleProperty progressValueProperty() {
        return progressValue;
    }

    public String getProgressLabel() {
        return progressLabel.get();
    }

    public SimpleStringProperty progressLabelProperty() {
        return progressLabel;
    }

    public String getPackageSize() {
        return packageSize.get();
    }

    public SimpleStringProperty packageSizeProperty() {
        return packageSize;
    }

    public boolean isForce() {
        return force.get();
    }

    public SimpleBooleanProperty forceProperty() {
        return force;
    }

    public boolean isDownloading() {
        return downloading.get();
    }

    public SimpleBooleanProperty downloadingProperty() {
        return downloading;
    }

    public String getForceLabel() {
        return forceLabel.get();
    }

    public SimpleStringProperty forceLabelProperty() {
        return forceLabel;
    }
}