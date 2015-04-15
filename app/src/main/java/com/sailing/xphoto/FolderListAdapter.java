package com.sailing.xphoto;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sailing on 15
    public FolderListAdapter(Context context, List<Map<String, Object>> dataList data, int resource, String[] from, int[] to){
        supper(context, dataList, res-4-1.
                */
public class FolderListAdapter extends BaseAdapter {
    private static Logger logger = LoggerFactory.getLogger(FolderListAdapter.class);

    /**数据*/
    private List < Map < String, Object >> data = null;

    /**视图容器*/
    private LayoutInflater listContainer = null;

    /**配置工具类*/
    XPreferencesHelper preferencesHelper = null;

    /**
     *
     * @param context
     * @param helper
     */
    public FolderListAdapter(Context context, XPreferencesHelper helper){
        this.preferencesHelper = helper;
        listContainer = LayoutInflater.from(context);

        data = helper.readFolderInfo();
    }

    /**
     * 增加目录。
     * @param path
     */
    public void addPath(String path){
        if(null == path || null == data) {
            logger.error("path or data is null.");
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put(XConst.KEY_COLUME_LISTVIEW_ABSOLUTEPATH, path);
        data.add(map);

        notifyDataSetChanged();
    }


    /**
     * 删除目录
     * @param position
     * @return
     */
    public String remove(int position) {
        if(null == data || position > data.size()) {
            logger.error("position error:" + position);
            return null;
        }

        String path = (String)((data.remove(position)).get(XConst.KEY_COLUME_LISTVIEW_ABSOLUTEPATH));
        preferencesHelper.removeFolderInfo(path);

        logger.info("Remove:" + path + " @ position:" + position);
        notifyDataSetChanged();
        return path;
    }


    /**
     * 更新进度。
     * @param position
     * @param progress
     */
    public void updateProgress(int position, int progress) {
        if(null == data || position > data.size()) {
            logger.error("position error:" + position);
            return;
        }
        logger.info("Update progress:" + progress + "@" + position);

        Map<String, Object> one = data.get(position);

        one.put(XConst.KEY_COLUME_LISTVIEW_PROGRESS, progress);

        notifyDataSetChanged();

    }


    @Override
    public int getCount() {
        if(null !=data){
            return data.size();
        }else{
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if(null == data) {
            return null;
        }else{
            return data.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        XViewHolder holder = null;

        if(convertView == null){
            convertView = listContainer.inflate(R.layout.folder_list_item, null);

            holder = new XViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.imageItem);
            holder.image.setImageResource(R.drawable.folder);
            holder.folder = (TextView) convertView.findViewById(R.id.folder);
            holder.title = (TextView) convertView.findViewById(R.id.file_name);
            holder.progress = (ProgressBar) convertView.findViewById(R.id.progressBar);
            convertView.setTag(holder);
        }else{
            //减少findviewbyid()
            holder = (XViewHolder)convertView.getTag();
        }

        String filePath = (String) data.get(position).get(XConst.KEY_COLUME_LISTVIEW_ABSOLUTEPATH);

        if(null == filePath) {
            logger.error("filePath is null.");
            return convertView;
        }

        File f = new File(filePath);
        String fileName = f.getName();
        String folder = f.getAbsolutePath();

        Integer progress = (Integer)data.get(position).get(XConst.KEY_COLUME_LISTVIEW_PROGRESS);

        holder.folder.setText(folder);
        holder.title.setText(fileName);

        if (null != progress && progress < 100) {
            logger.info("Update progress " + progress + "@" + fileName);
            holder.progress.setVisibility(View.VISIBLE);
        }else {
            holder.progress.setVisibility(View.GONE);

        }
        return convertView;
    }


    private final  class XViewHolder{
        public ImageView image;
        public TextView title;
        public TextView folder;
        public ProgressBar progress;
    }
}
