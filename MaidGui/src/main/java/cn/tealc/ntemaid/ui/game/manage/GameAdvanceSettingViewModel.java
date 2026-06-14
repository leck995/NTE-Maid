package cn.tealc.ntemaid.ui.game.manage;

import cn.tealc.ntemaid.util.GameResourcesManager;
import cn.tealc.ntemaid.util.crypto.HTCryptoUtils;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;


public class GameAdvanceSettingViewModel implements ViewModel {
    private final SimpleStringProperty fps = new SimpleStringProperty();

    private final SimpleStringProperty engineConfigRow = new SimpleStringProperty();
    private HTCryptoUtils.KeyId engineConfigKey = null;

    public GameAdvanceSettingViewModel() {
        Thread.startVirtualThread(this::loadEngineConfig);
    }


    public void loadEngineConfig() {
        Optional<File> optional = GameResourcesManager.getGameEngineIni();
        optional.ifPresent(file -> {
            try {
                Path filePath = file.toPath();
                List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
                HTCryptoUtils.HTLineProcessor.DecryptResult result = HTCryptoUtils.HTLineProcessor.decryptLines(lines);

                if (result.decryptedBlocks() > 0) {
                    String decryptedText = result.text();
                    HTCryptoUtils.KeyId key = result.keysUsed().isEmpty() ? null : result.keysUsed().iterator().next();
                    Platform.runLater(() -> {
                        engineConfigRow.set(decryptedText);
                        engineConfigKey = key;
                    });
                } else {
                    String content = String.join("\n", lines);
                    Platform.runLater(() -> engineConfigRow.set(content));
                }
            } catch (IOException e) {
                System.err.println("读取文件失败: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }


    public void saveEngineConfig() {
        if (engineConfigKey != null) {
            Optional<File> optional = GameResourcesManager.getGameEngineIni();
            optional.ifPresent(file -> {
                try {
                    String encryptLines = HTCryptoUtils.HTLineProcessor.encryptLines(engineConfigRow.get(), engineConfigKey);
                    Files.writeString(file.toPath(), encryptLines);
                } catch (IOException | GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
            });
        }

    }


    /**
     * @return void
     * @description: 修改帧率，此处value必须为0，1，2，3
     * @param: value
     * @date: 2024/10/19
     */
    public void setFps(String value) {
//        boolean exist = hasDbFile();
//        if (exist) {
//            GameSettingDao gameSettingDao = new GameSettingDao();
//            boolean customFrameRate = gameSettingDao.updateSettingValueByKey("CustomFrameRate", value);
//            if (!customFrameRate) {
//                MvvmFX.getNotificationCenter().publish(NotificationKey.MESSAGE,new MessageInfo(MessageType.ERROR, LanguageManager.getString("ui.game_manager.advance.fps.message01")));
//            }
//        }else {
//            MvvmFX.getNotificationCenter().publish(NotificationKey.MESSAGE,new MessageInfo(MessageType.ERROR, LanguageManager.getString("ui.game_manager.advance.fps.message02")));
//        }

    }

    public boolean hasDbFile() {
//        File gameDB = GameResourcesManager.getGameDB();
//        return gameDB != null;

        return true;
    }


    public String getFps() {
        return fps.get();
    }

    public SimpleStringProperty fpsProperty() {
        return fps;
    }

    public String getEngineConfigRow() {
        return engineConfigRow.get();
    }

    public SimpleStringProperty engineConfigRowProperty() {
        return engineConfigRow;
    }
}