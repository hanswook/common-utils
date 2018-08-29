package com.common.utils.common_utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * description
 *
 * @author created by wuwang on 2016/5/25
 */
public class SPUtil {

    private static SPUtil instance;
    private SharedPreferences sp;
    private final String defaultFlag="wxy";

    private SPUtil(){
    }

    public static SPUtil getInstance(){
        if(instance==null){
            synchronized (SPUtil.class){
                if(instance==null){
                    instance=new SPUtil();
                }
            }
        }
        return instance;
    }

    public void init(Application application){
        sp=application.getSharedPreferences(application.getPackageName(), Context.MODE_PRIVATE);
    }
    //清空数据
    public void clean(){
        if(sp!=null){
            SharedPreferences.Editor editor = sp.edit();
            editor.clear();
            editor.commit();
        }
    }

    public void setFlag(String flag, boolean bool){
        sp.edit().putBoolean(flag,bool).apply();
    }

    public boolean getFlag(String flag, boolean defaultFlag){
        return sp.getBoolean(flag,defaultFlag);
    }

    public void markFirst(){
        setFlag(defaultFlag,false);
    }

    public boolean isFirst(){
        return sp.getBoolean(defaultFlag,true);
    }

    public void setCache(String key, String value){
        sp.edit().putString(key,value).apply();
    }

    public void setCache(String key, long value){
        sp.edit().putLong(key,value).apply();
    }

    public void setCache(String key, int value){
        sp.edit().putInt(key,value).apply();
    }

    public String getCache(String key){
        return sp.getString(key,null);
    }
    public long getCacheLong(String key){
        return sp.getLong(key, System.currentTimeMillis());
    }
    public long getCache(String key, long value){
        return sp.getLong(key,value);
    }

    public int getCache(String key, int value){
        return sp.getInt(key,value);
    }

    public void remove(String key){
        if(sp!=null){

            SharedPreferences.Editor editor = sp.edit();
            editor.remove(key);
            editor.apply();
        }
    }
}
