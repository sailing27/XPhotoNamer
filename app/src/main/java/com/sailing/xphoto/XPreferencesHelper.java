package com.sailing.xphoto;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 设置读取工具类
 * Created by sailing on 15-3-28.
 */
public class XPreferencesHelper {
    private static Logger logger = LoggerFactory.getLogger(XPreferencesHelper.class);
    private SharedPreferences mySharedPreferences = null;

    public XPreferencesHelper(Activity activity){
        if(null == activity) {
            throw new IllegalArgumentException("activity is null.");
        }

        mySharedPreferences =  PreferenceManager.getDefaultSharedPreferences(activity);

        if (null == mySharedPreferences) {
            logger.error("Can not get getSharedPreferences.");
            throw new IllegalArgumentException("mySharedPreferences is null.");
        }

        logger.info("XPreferencesHelper ok.");
    }

    public Set<String> getStringSet(String key){
        return mySharedPreferences.getStringSet(key, new HashSet<String>());
    }

    public String getString(String key) {
        return mySharedPreferences.getString(key, null);
    }

    public boolean getBoolean(String key) {
        return mySharedPreferences.getBoolean(key, false);
    }


    /**
     * 读取初始值
     */
    public List<Map<String, Object>> readFolderInfo() {
        Set<String> folderSet = mySharedPreferences.getStringSet(XConst.KEY_FOLDER_LIST, new HashSet<String>());
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (String folder : folderSet) {
            logger.info("-->" + folder);
            File f = new File(folder);

            Map<String, Object> map = new HashMap<>();
            map.put(XConst.KEY_COLUME_LISTVIEW_ABSOLUTEPATH, f.getAbsolutePath());
            dataList.add(map);
        }
        return dataList;
    }


    /**
     * 保存用户选择的目录
     * @param path
     */
    public void saveFolderInfo(String path){
        Set<String> folderSet = mySharedPreferences.getStringSet(XConst.KEY_FOLDER_LIST, new HashSet<String>());
        folderSet.add(path);

        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.remove(XConst.PREFRENCES_KEY_NAME);
        editor.putStringSet(XConst.KEY_FOLDER_LIST, folderSet);

        //提交当前数据
        editor.apply();
        logger.info("Save folder:" + path);
    }


    /**
     * 保存用户选择的目录
     * @param path
     */
    public void removeFolderInfo(String path){
        Set<String> folderSet = mySharedPreferences.getStringSet(XConst.KEY_FOLDER_LIST, new HashSet<String>());
        folderSet.remove(path);

        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.remove(XConst.PREFRENCES_KEY_NAME);
        editor.putStringSet(XConst.KEY_FOLDER_LIST, folderSet);

        //提交当前数据
        editor.apply();
        logger.info("Remove folder:" + path);
    }
}
