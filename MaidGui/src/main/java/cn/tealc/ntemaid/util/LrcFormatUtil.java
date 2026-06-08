package cn.tealc.ntemaid.util;

import cn.tealc.ntemaid.model.game.music.LrcBean;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * description: LrcFormatUtil <br>
 * date: 2021/4/22 18:27 <br>
 * author: Leck <br>
 * version: 1.1 (Modified)
 */
public class LrcFormatUtil {
    private static final Pattern pattern = Pattern.compile("((\\[\\d{2,3}:\\d{2}\\.\\d{2,3}\\])+)(.+)");
    private static final Pattern timePattern = Pattern.compile("\\[(\\d{2,3}):(\\d\\d)\\.(\\d{2,3})\\]");

    public List<LrcBean> getLocalLrcList(String filepath) {
        String lrcRow = getFileRow(filepath);
        if (lrcRow == null) return null;
        return parseLrcString(lrcRow);
    }

    public static List<LrcBean> getLrcListFromFile(File file) {
        String lrcRow = getFileRow(file);
        if (lrcRow == null) return null;
        return parseLrcString(lrcRow);
    }

    /**
     * 核心解析方法：支持同时间戳单行翻译和多行翻译
     */
    private static List<LrcBean> parseLrcString(String lrcRow) {
        // 使用 LinkedHashMap 维持基本的解析顺序，Key 为 longTime
        Map<Long, LrcBean> lrcMap = new LinkedHashMap<>();

        String[] split = lrcRow.split("\\n");
        if (split.length == 1) split = lrcRow.split("\\\\n");

        for (String s : split) {
            if (s == null || s.trim().isEmpty()) continue;
            s = s.trim();

            Matcher lineMatcher = pattern.matcher(s);
            if (!lineMatcher.find()) continue;

            String times = lineMatcher.group(1);
            String text = lineMatcher.group(3).trim();

            if (text.contains("汉化")) continue;

            // 提取这一行的所有时间标签（支持多时间戳合并行，如 [01:00.00][02:00.00]text）
            Matcher timeMatcher = timePattern.matcher(times);
            while (timeMatcher.find()) {
                long min = Long.parseLong(timeMatcher.group(1));
                long sec = Long.parseLong(timeMatcher.group(2));
                long mil = Long.parseLong(timeMatcher.group(3));

                int scale_mil = mil > 100 ? 1 : 10;
                long time = min * 60000 + sec * 1000 + mil * scale_mil;

                // 【核心改动】检查Map中是否已经存在该时间戳的歌词
                if (lrcMap.containsKey(time)) {
                    LrcBean existBean = lrcMap.get(time);
                    // 如果已经有原文，且没有翻译，则把当前的 text 视作翻译
                    if (existBean.getTransText() == null || existBean.getTransText().isEmpty()) {
                        existBean.setTransText(text);
                    } else {
                        // 如果连翻译都有了，说明可能是多行合并，用换行符追加（备用）
                        existBean.setTransText(existBean.getTransText() + "\n" + text);
                    }
                } else {
                    // 第一次遇到这个时间戳，正常创建
                    LrcBean bean = new LrcBean(time, times, text);
                    lrcMap.put(time, bean);
                }
            }
        }

        // 转换成 List 并排序
        List<LrcBean> lrcBeans = new ArrayList<>(lrcMap.values());
        Collections.sort(lrcBeans, (lyrBean, t1) -> Long.compare(lyrBean.getLongTime(), t1.getLongTime()));

        return lrcBeans;
    }

    /*===============================以下是VTT转换及文件读取（保持原样）=======================================*/
    public static List<LrcBean> getVttListFromFile(File file){
        String vttRow = getFileRow(file);
        if (vttRow==null) return null;
        List<LrcBean> lrcBeans = new ArrayList<>();
        String[] lines = vttRow.split("\r\n\r\n");
        for (int i = 1, length = lines.length; i < length; i++) {
            String line = lines[i];
            if(line.contains("-->")){
                String[] split = line.split("\r\n");
                String time = split[1].split(" --> ")[0];
                lrcBeans.add(new LrcBean(vttTimeParse(time), time, split[2]));
            }
        }
        return lrcBeans;
    }

    public static long vttTimeParse(String time){
        String[] split = time.split(":");
        if (split.length==3){
            long millisecond= Long.parseLong(split[0]) * 3600000 + Long.parseLong(split[1]) * 60000;
            String[] split1 = split[2].split("\\.");
            millisecond+=Long.parseLong(split1[0]) * 1000 + Long.parseLong(split1[1]);
            return millisecond;
        }else if (split.length==2){
            long millisecond= Long.parseLong(split[0]) * 60000;
            String[] split1 = split[1].split("\\.");
            millisecond+=Long.parseLong(split1[0]) * 1000 + Long.parseLong(split1[1]);
            return millisecond;
        }
        return 0;
    }

    private static String getFileRow(String filepath){
        return getFileRow(new File(filepath));
    }

    private static String getFileRow(File file){
        if (file.exists()){
            StringBuilder jsonTxt;
            try {
                BufferedReader br= new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
                String line;
                jsonTxt = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    jsonTxt.append(line);
                    jsonTxt.append(System.lineSeparator());
                }
                br.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return jsonTxt.toString();
        }else{
            return null;
        }
    }
}