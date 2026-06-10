package cn.tealc.ntemaid.ui.game.manage;

import cn.tealc.ntemaid.util.GameResourcesManager;
import cn.tealc.ntemaid.util.crypto.HTCryptoUtils;
import de.saxsys.mvvmfx.ViewModel;
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
//        if (hasDbFile()) {
//            GameSettingDao gameSettingDao = new GameSettingDao();
//            Pair<String, String> customFrameRate = gameSettingDao.getSettingValueByKey("CustomFrameRate");
//            if (customFrameRate != null) {
//                fps.set(customFrameRate.getValue());
//            }
//        }else {
//            NotificationManager.message(new MessageInfo(MessageType.WARNING,LanguageManager.getString("ui.game_manager.advance.fps.message02")));
//        }

        loadEngineConfig();
    }


    public void loadEngineConfig() {
        Optional<File> optional = GameResourcesManager.getGameEngineIni();
        optional.ifPresent(file -> {
            try {
                Path filePath = file.toPath();

                // 1. 改为读取所有行
                List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

                // 2. 调用专门处理多行解密的处理器
                HTCryptoUtils.HTLineProcessor.DecryptResult result = HTCryptoUtils.HTLineProcessor.decryptLines(lines);

                // 3. 判断是否解密成功（至少解密了一个块）
                if (result.decryptedBlocks() > 0) {
                    String decryptedText = result.text();
                    System.out.println("解密成功内容：\n" + decryptedText);

                    engineConfigRow.set(decryptedText);

                    // 获取解密时使用的第一个密钥（如果有的话）
                    if (!result.keysUsed().isEmpty()) {
                        engineConfigKey = result.keysUsed().iterator().next();
                    }
                } else {
                    // 如果没有解密块，可能是明文文件，直接显示原内容
                    String content = String.join("\n", lines);
                    System.out.println("未检测到加密块，原始内容：\n" + content);
                    engineConfigRow.set(content);
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