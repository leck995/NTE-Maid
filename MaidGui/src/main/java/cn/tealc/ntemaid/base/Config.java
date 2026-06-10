package cn.tealc.ntemaid.base;

import cn.tealc.ntemaid.util.LanguageManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;


public class Config {
    private static final Logger log = LoggerFactory.getLogger(Config.class);
    private static volatile Setting setting;
    public static ResourceBundle language;

    public static Setting getSetting() {
        return setting;
    }

    static void setSetting(Setting s) {
        setting = s;
    }

    public static String appTitle;

    static {
        ObjectMapper mapper = new ObjectMapper();
        File settingFile = new File("settings.json");
        if (settingFile.exists()) {
            try {
                setting = mapper.readValue(settingFile, Setting.class);
            } catch (IOException e) {
                log.error("读取设置文件失败，将使用默认设置", e);
                setting = new Setting();
            }
        }
        if (setting == null) {
            setting = new Setting();
        }
        language = ResourceBundle.getBundle("cn/tealc/ntemaid/language/local", Locale.SIMPLIFIED_CHINESE);
        appTitle = LanguageManager.getString("app.title");
    }


    public static synchronized void save() {
        File file = new File("settings.json");
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, setting);
            log.info("配置文件已保存");
        } catch (IOException e) {
            log.error("保存配置文件失败", e);
        }
    }
}