package cn.tealc.ntemaid.ui.system;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.model.system.ResponseBody;
import cn.tealc.ntemaid.model.system.realease.Release;
import cn.tealc.ntemaid.thread.system.update.CheckAppVersionTask;
import cn.tealc.ntemaid.util.LanguageManager;
import cn.tealc.ntemaid.util.LocalResourcesManager;
import cn.tealc.teafx.utils.message.MessageInfo;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.SceneLifecycle;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Font;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;


public class SettingViewModel implements ViewModel, SceneLifecycle {
    private static final Logger LOG = LoggerFactory.getLogger(SettingViewModel.class);
    private SimpleBooleanProperty exitWhenGameOver = new SimpleBooleanProperty();
    private SimpleBooleanProperty hideWhenGameStart = new SimpleBooleanProperty();
    private ObservableList<String> fontFamilyList = FXCollections.observableArrayList();
    private SimpleBooleanProperty checkNewVersion = new SimpleBooleanProperty();
    private SimpleBooleanProperty diyHomeBg = new SimpleBooleanProperty();
    private SimpleStringProperty diyHomeBgName = new SimpleStringProperty();
    private SimpleIntegerProperty homeBgType = new SimpleIntegerProperty();
    private SimpleStringProperty homeBgDir = new SimpleStringProperty();
    private ObservableList<Pair<String, Locale>> languages = FXCollections.observableArrayList();

    public SettingViewModel() {
        exitWhenGameOver.bindBidirectional(Config.getSetting().exitWhenGameOverProperty());
        hideWhenGameStart.bindBidirectional(Config.getSetting().hideWhenGameStartProperty());
        fontFamilyList.setAll(Font.getFamilies());
        diyHomeBg.bindBidirectional(Config.getSetting().diyHomeBgProperty());
        diyHomeBgName.bindBidirectional(Config.getSetting().diyHomeBgNameProperty());
        checkNewVersion.bindBidirectional(Config.getSetting().checkNewVersionProperty());
        homeBgType.bindBidirectional(Config.getSetting().diyHomeBgTypeProperty());
        homeBgDir.bindBidirectional(Config.getSetting().diyHomeBgDirProperty());

        diyHomeBgName.addListener((observableValue, s1, s2) -> {
            if (getDiyHomeBgName() != null) {
               NotificationManager.publish(NotificationKey.CHANGE_BG);
            }
        });

        homeBgDir.addListener((observableValue, s1, s2) -> {
            if (getHomeBgDir() != null) {
               NotificationManager.publish(NotificationKey.CHANGE_BG);
            }
        });

        languages.setAll(
                List.of(
                        new Pair<>("简体中文", Locale.CHINA),
                        new Pair<>("English", Locale.ENGLISH)
                ));
    }

    public void changeBackground() {
       NotificationManager.publish(NotificationKey.CHANGE_BG);
    }


    @Override
    public void onViewAdded() {

    }

    @Override
    public void onViewRemoved() {
        Config.save();
    }


    public void setBgFile(File file) {
        try {
            String newName = LocalResourcesManager.setHomeBg(file, diyHomeBgName.get());
            diyHomeBgName.set(newName);
            NotificationManager.publish(NotificationKey.CHANGE_BG);
        } catch (IOException e) {
            LOG.error("IO ERROR", e);
        }
    }

    public void checkVersion() {
        CheckAppVersionTask task = new CheckAppVersionTask(false);
        task.setOnSucceeded(workerStateEvent -> {
            ResponseBody<Release> value = task.getValue();
            if (value.getCode() == 200) {
               NotificationManager.publish(NotificationKey.NOTIFICATION_SHOW_UPDATE, value.getData());
            } else if (value.getCode() == 1) {
               NotificationManager.publish(NotificationKey.MESSAGE, MessageInfo.warning(LanguageManager.getString("ui.setting.about.update.tip01")));
            } else {
               NotificationManager.publish(NotificationKey.MESSAGE, MessageInfo.warning(LanguageManager.getString("ui.main.message.type01")));
            }
        });
        Thread.startVirtualThread(task);
    }


    public void setLanguages(Locale locale) {
        Config.getSetting().setLanguage(locale);
        Config.language = ResourceBundle.getBundle("cn.tealc/ntemaid/language/local", Config.getSetting().getLanguage());
        MvvmFX.setGlobalResourceBundle(Config.language);
    }

    public ObservableList<String> getFontFamilyList() {
        return fontFamilyList;
    }

    public boolean isExitWhenGameOver() {
        return exitWhenGameOver.get();
    }

    public SimpleBooleanProperty exitWhenGameOverProperty() {
        return exitWhenGameOver;
    }

    public void setExitWhenGameOver(boolean exitWhenGameOver) {
        this.exitWhenGameOver.set(exitWhenGameOver);
    }

    public boolean isHideWhenGameStart() {
        return hideWhenGameStart.get();
    }

    public SimpleBooleanProperty hideWhenGameStartProperty() {
        return hideWhenGameStart;
    }

    public boolean isDiyHomeBg() {
        return diyHomeBg.get();
    }

    public SimpleBooleanProperty diyHomeBgProperty() {
        return diyHomeBg;
    }

    public String getDiyHomeBgName() {
        return diyHomeBgName.get();
    }

    public SimpleStringProperty diyHomeBgNameProperty() {
        return diyHomeBgName;
    }

    public boolean isCheckNewVersion() {
        return checkNewVersion.get();
    }

    public SimpleBooleanProperty checkNewVersionProperty() {
        return checkNewVersion;
    }

    public ObservableList<Pair<String, Locale>> getLanguages() {
        return languages;
    }

    public int getHomeBgType() {
        return homeBgType.get();
    }

    public SimpleIntegerProperty homeBgTypeProperty() {
        return homeBgType;
    }

    public void setHomeBgType(int homeBgType) {
        this.homeBgType.set(homeBgType);
    }

    public String getHomeBgDir() {
        return homeBgDir.get();
    }

    public SimpleStringProperty homeBgDirProperty() {
        return homeBgDir;
    }

    public void setHomeBgDir(String homeBgDir) {
        this.homeBgDir.set(homeBgDir);
    }
}