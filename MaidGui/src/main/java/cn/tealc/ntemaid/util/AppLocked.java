package cn.tealc.ntemaid.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @description: 单实例检测。通过文件句柄占用判断是否已有实例运行；
 *               多开时通过本地 Socket IPC 通知已运行实例显示主窗口，随后退出当前实例。
 * @author: Leck
 * @create: 2024-11-24 20:00
 */
public class AppLocked{
    private static final Logger LOG = LoggerFactory.getLogger(AppLocked.class);
    private File file = new File("lock");
    private FileInputStream fis;
    public AppLocked() {
        try {
            if(file.exists()){
                boolean delete = file.delete();
                if(!delete){ //说明被占用了，助手存在,尝试唤起已运行实例
                    LOG.info("检测到多开，尝试唤起已运行实例");
                    // 通知已运行实例显示主窗口（连接失败时降级为弹窗提示）
                    SingleInstanceClient.sendShow();
                    System.exit(0);
                }else {
                    boolean newFile = file.createNewFile();
                    if(!newFile){
                        LOG.info("无法创建lock文件");
                    }
                    fis = new FileInputStream(file);
                }
            }else {
                boolean newFile = file.createNewFile();
                if(!newFile){
                    LOG.info("无法创建lock文件");
                }
                fis = new FileInputStream(file);
            }

            LOG.info("多开检测通过");
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }



    /**
     * @description: 释放文件
     * @param:
     * @return  void
     * @date:   2024/11/24
     */
    public void release(){
        try {
            fis.close();
            file.delete();
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }
}