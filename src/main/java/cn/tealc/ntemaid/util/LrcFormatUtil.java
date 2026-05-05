package cn.tealc.ntemaid.util;

import cn.tealc.ntemaid.model.game.music.LrcBean;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * description: LrcFormatUtil <br>
 * date: 2021/4/22 18:27 <br>
 * author: Leck <br>
 * version: 1.0 <br>
 */
public class LrcFormatUtil {
    /*===============================以下是LRC转换=======================================*/
    private static Pattern pattern=Pattern.compile("((\\[\\d{2,3}:\\d{2}\\.\\d{2,3}\\])+)(.+)");;
    private static Pattern timePattern = Pattern.compile("\\[(\\d{2,3}):(\\d\\d)\\.(\\d{2,3})\\]");
    public List<LrcBean> getLocalLrcList(String filepath) {

        String lrcRow = getFileRow(filepath);
        if (lrcRow==null){
            return null;
        }
        //解析歌词
        List<LrcBean> lrcBeans = new ArrayList<>();//存放歌词
        String[] split = lrcRow.split("\\n");//分割
        if (split.length == 1) split = lrcRow.split("\\\\n");
        for (String s : split) {
            List<LrcBean> list = parseLine(s);
            if (list != null && !list.isEmpty()) {
                lrcBeans.addAll(list);
            }
        }
        //按照时间排序
        Collections.sort(lrcBeans, (lyrBean, t1) -> (int)
                (lyrBean.getLongTime() - t1.getLongTime()));
        return  lrcBeans;
    }



    public static List<LrcBean> getLrcListFromFile(File file) {
        String lrcRow = getFileRow(file);
        if (lrcRow==null){
            return null;
        }
        //解析歌词
        List<LrcBean> lrcBeans = new ArrayList<>();//存放歌词
        String[] split = lrcRow.split("\\n");//分割
        if (split.length == 1) split = lrcRow.split("\\\\n");
        for (String s : split) {
            List<LrcBean> list = parseLine(s);
            if (list != null && !list.isEmpty()) {
                lrcBeans.addAll(list);
            }
        }
        //按照时间排序
        Collections.sort(lrcBeans, (lyrBean, t1) -> (int)
                (lyrBean.getLongTime() - t1.getLongTime()));
        return  lrcBeans;
    }



    //解析每一行的歌词
    private static List<LrcBean> parseLine(String s) {
        if (s.isEmpty()) {
            return null;
        }
        // 去除空格
        s = s.trim();
        // 正则表达式，判断s中是否有[00:00.60]或[00:00.600]格式的片段

        Matcher lineMatcher=pattern.matcher(s);
        // 如果没有，返回null
        if (!lineMatcher.find()) {
            return null;
        }
        // 得到时间标签
        String times = lineMatcher.group(1);
        // 得到歌词文本内容
        String text = lineMatcher.group(3);

        Pattern.compile("(「.*」)").matcher(text);
        List<LrcBean> entryList = new ArrayList<>();

        Matcher timeMatcher = timePattern.matcher(times);
        while (timeMatcher.find()) {
            long min = Long.parseLong(timeMatcher.group(1));// 分
            long sec = Long.parseLong(timeMatcher.group(2));// 秒
            long mil = Long.parseLong(timeMatcher.group(3));// 毫秒
            // 转换为long型时间
            int scale_mil=mil>100?1:10;//如果毫秒是3位数则乘以1，反正则乘以10
            // 转换为long型时间
            long time =
                    min * 60000 +
                            sec * 1000 +
                            mil * scale_mil;
            // 最终解析得到一个list
            if (text.contains("汉化")) break;
            entryList.add(new LrcBean(time, times, text));
        }
        return entryList;
    }



    /*===============================以下是VTT转换=======================================*/
    public static List<LrcBean> getVttListFromFile(File file){
        String vttRow = getFileRow(file);
        if (vttRow==null){
            return null;
        }

        List<LrcBean> lrcBeans = new ArrayList<>();//存放歌词
        String[] lines = vttRow.split("\r\n\r\n");

        for (int i = 1,length=lines.length; i <  length; i++) {
            String line = lines[i];
            if(line.contains("-->")){
                String[] split = line.split("\r\n");
                String time = split[1].split(" --> ")[0];
                //String text=split[2];
                lrcBeans.add(new LrcBean(vttTimeParse(time),time,split[2]));
            }
        }

        return lrcBeans;
    }


    /**
     * @description: vtt格式时间转换
     * @name: vttTimeParse
     * @author: Leck
     * @param:	time
     * @return  long
     * @date:   2023/9/10
     */

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
        File file=new File(filepath);
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

