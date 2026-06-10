package cn.tealc.ntemaid.util;


import cn.tealc.ntemaid.base.Config;


public class LanguageManager {

    private static final String separate = "#";
    public static String getString(String key){
        return Config.language.getString(key);
    }

    public static String[] getStringArray(String key){
        String string = getString(key);
        return string.split(separate);
    }


}