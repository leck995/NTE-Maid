package release;

import cn.tealc.ntemaid.model.system.realease.Release;
import cn.tealc.ntemaid.model.system.realease.ReleaseList;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @description: 用于生成发布新版本的json
 * @author: Leck
 * @create: 2024-12-22 15:20
 */
public class ReleaseCreateUtil {
    private static final String JAR_PATH = "D:\\Code\\Reposities\\JavaFX\\NTEMaid\\MaidGui\\target\\NTEMaid-1.2.0.jar";
    private static final String RELEASE_FILE="release/release.json";
    public static void main(String[] args) throws IOException {
        Release latestRelease = latestRelease();
        ReleaseList releaseList = new ReleaseList(null, latestRelease);
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(RELEASE_FILE);
        if (!file.getParentFile().exists()){
            file.getParentFile().mkdir();
        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, releaseList);
    }

    private static Release latestRelease() throws IOException {
        String latestVersion = "1.2.0";
        String latestName = "正式版";
        String latestDescription = """
                若无法自动更新，请前往发布页或者群手动下载最新版程序。
                
                更新日志：
                1. 新增B服国际服抽卡分析
                2. 新增角色自动前行
                3. 新增粉爪大劫案按F持续拾取
                4. 车载音乐支持在游戏内播放器静音播放的状态下播放（播放器右上角进行开关）
                5. 车载音乐快捷键新增取消监听功能
                6. 新增截图功能
                7. 其他优化
                
                """;
        boolean latestForce = false; //是否是强制更新
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String dateTime = format.format(date);
        File latestFile = new File(JAR_PATH);
        String latestMd5 = DigestUtils.md5Hex(new FileInputStream(latestFile));
        //https://api.github.com/repos/leck995/WutheringWavesTool/releases/latest
        //如果有github,则第一个放github下载链接
        String[] latestUrls = {
                "https://release.999758.xyz/nte/NTEMaid-1.2.0.zip",
                "https://cdn.999758.xyz/nte/NTEMaid-1.2.0.zip"
        };
        String warning = "更新警告";//强制更新警告
        boolean isPre = false;
        Release latestRelease = new Release(latestName, latestVersion, latestDescription, dateTime, latestForce, latestUrls, latestMd5, warning, isPre);
        return latestRelease;
    }


    /**
     * 预览版本
     *
     * @return {@link Release }
     */
    private static Release preRelease() {
        return null;
    }
}