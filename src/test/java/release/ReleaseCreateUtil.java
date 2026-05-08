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
    private static final String JAR_PATH = "D:\\Code\\Reposities\\JavaFX\\NTEMaid\\target\\NTEMaid-1.0-SNAPSHOT.jar";
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
        String latestVersion = "1.0.0";
        String latestName = "正式版1.0.0";
        String latestDescription = """
                若无法自动更新，请前往发布页或者群手动下载最新版程序。
                
                更新日志：
                1. 重新设计窗口装饰，可自由拖动与响应系统事件
                2. 修复角色查看数据不刷新的问题
                3. 其他修复与优化
                
                """;
        boolean latestForce = false; //是否是强制更新
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String dateTime = format.format(date);
        File latestFile = new File(JAR_PATH);
        String latestMd5 = DigestUtils.md5Hex(new FileInputStream(latestFile));
        //https://api.github.com/repos/leck995/WutheringWavesTool/releases/latest
        //如果有github,则第一个放github下载链接
        String[] latestUrls = {"https://nte-update.999758.xyz/NTEMaid-1.0.0.jar"};
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