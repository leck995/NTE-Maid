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
    private static final String JAR_PATH = "H:\\Projects\\Java\\owner\\NTEMaid\\MaidGui\\target\\NTEMaid-1.2.4.jar";
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
        String latestVersion = "1.2.4";
        String latestName = "正式版";
        String latestDescription = """
                若无法自动更新，请前往发布页或者群手动下载最新版程序。
                
                更新日志：
                1. 重新适配国际服，解决一系列问题
                2. 主页新增塔吉多角色面板，默认显示当前账号的角色数据
                3. 新增通用抽卡抓取窗口，降低出现问题的可能性
                4. 修复塔吉多接口无法使用问题
                5. 修复通行证无法自动跳转的问题
                
                
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
                "https://github.com/leck995/NTE-Maid/releases/download/1.2.4/NTEMaid-1.2.4.jar",
                "https://release.999758.xyz/nte/NTEMaid-1.2.4.zip",
                "https://cdn.999758.xyz/nte/NTEMaid-1.2.4.zip"
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