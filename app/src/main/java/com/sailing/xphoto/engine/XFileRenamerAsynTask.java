package com.sailing.xphoto.engine;

import android.os.AsyncTask;
import android.widget.Button;
import android.widget.Toast;

import com.sailing.xphoto.MainActivity;
import com.sailing.xphoto.R;
import com.sailing.xphoto.XConst;
import com.sailing.xphoto.XPreferencesHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件整理异步任务实现。
 * Created by sailing on 15-3-25.
 */
public class XFileRenamerAsynTask extends AsyncTask<Void, Integer, String> {
    private static Logger logger = LoggerFactory.getLogger(XFileRenamerAsynTask.class);

    /**文件名中的时间格式。*/
    private static final String FILE_NAME_FORMAT = "yyyy-MM-dd_HHmmss";

    /**目录格式*/
    private static final String FOLDER_NAME_FORMAT = "yyyy-MM-dd";

    /**目录格式*/
    private static SimpleDateFormat folderFormatter = new SimpleDateFormat(FOLDER_NAME_FORMAT);

    /**文件格式*/
    private static SimpleDateFormat fileFormatter = new SimpleDateFormat(FILE_NAME_FORMAT);

    /**主Activity*/
    private MainActivity activity = null;

    /**Prefernces工具类*/
    private XPreferencesHelper preferencesHelper = null;

    /**开始按钮*/
    private Button startBt = null;

    /**文件名匹配规则*/
    private String fileNameMather = null;

    /**目录名匹配规则*/
    private String folderNameMather = null;

    /**目标目录路径*/
    private String targetFolder = null;

    /**目录文件名前缀*/
    private String targetFilePreName = null;

    /**是否递归子目录*/
    private boolean isRecursive = false;

    /**是创建日期目录*/
    private boolean isCreateDateFolder = false;

    /**
     *
     * @param activity
     * @param helper
     */
    public XFileRenamerAsynTask(MainActivity activity, XPreferencesHelper helper) {
        this.activity = activity;
        this.preferencesHelper =helper;

        startBt = (Button) activity.findViewById(R.id.start_button);
    }

    @Override
    /**
     * 在后台运行并处理后台操作
     */
    protected String doInBackground(Void... params) {
        //禁用按钮，避免多次点击*/
        if(null != startBt) {
            startBt.setClickable(false);
        }

        Set<String> folderSet = preferencesHelper.getStringSet(XConst.KEY_FOLDER_LIST);
        return renameAll(folderSet);
    }

    /**
     * 更新进度。
     * @param progress 完成度
     */
    protected void onProgressUpdate(Integer... progress) {
        if (null == progress || progress.length < 2) {
            logger.error("progress is null.");
        }

        //ListView中的条目位置
        int idx = progress[0];

        //进度
        int p = progress[1];

        activity.updateProgress(idx, p);

    }

    /**
     * 完成。
     *
     * @param result 结果
     */
    protected void onPostExecute(String result) {
        //启用按钮。
        if(null != startBt) {
            startBt.setClickable(true);
        }

        logger.info("Result:" + result);
        if (null != result) {
            Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 读取配置
     * @param rid
     * @return
     */
    private String getStrConfig(int rid) {
        return preferencesHelper.getString(activity.getString(rid));
    }

    /**
     * 读取配置
     * @param rid
     * @return
     */
    private boolean getBooleanConfig(int rid) {
        return preferencesHelper.getBoolean(activity.getString(rid));
    }

    /**
     * 匹配目录或文件名
     * @param str
     * @param strPattern
     * @return
     */
    private static boolean match(String str, String strPattern){
        //用*匹配任意字符。
        strPattern = strPattern.replaceAll("\\.", "\\\\.");
        strPattern = strPattern.replaceAll("\\*", ".*");
        strPattern = strPattern.toUpperCase();
        Pattern pattern = Pattern.compile(strPattern);
        Matcher matcher = pattern.matcher(str.toUpperCase());
        boolean result =  matcher.matches();
        logger.info(str + ".match(" + strPattern + ") == " + result);

        return result;
    }


    private String renameAll(Set<String> paths) {
        logger.info("Start:{}", paths);

        folderNameMather =  getStrConfig(R.string.pref_key_filter_folder_rule);
        fileNameMather =  getStrConfig(R.string.pref_key_filter_file_rule);
        targetFolder = getStrConfig(R.string.pref_key_move_dist_folder);
        targetFilePreName = getStrConfig(R.string.pref_key_dist_file_prename);
        isRecursive = getBooleanConfig(R.string.pref_key_is_recursive);
        isCreateDateFolder = getBooleanConfig(R.string.pref_key_is_create_date_folder);

        if(null == fileNameMather || fileNameMather.isEmpty()) {
            fileNameMather = "*";
        }

        if(null == folderNameMather || folderNameMather.isEmpty()) {
            folderNameMather = "*";
        }

        int idx = 0;
        int totalFile = 0;
        for (String file : paths) {
            List<String> allFiles = scanAllFile(file);
            logger.info("Folder:" + file + " has " + allFiles.size() + " file.");
            publishProgress(idx,1);
            totalFile += processAllFile(allFiles, file, idx);
            //进度置为100%
            publishProgress(idx, 100);
            idx++;
        }

        return "共处理 "+totalFile+" 个文件！";

    }


    /**
     * 扫描所有满足条件的文件
     * @param d
     * @return
     */
    private List<String> scanAllFile(String d){
        List<String> fileList = new LinkedList<>();
        if (null == d) {
            logger.error("directory is null");
            return fileList;
        }

        File directory = new File(d);

        logger.info("scanAllFile: " + directory.getAbsolutePath());
        logger.info("Config: name:[" + fileNameMather + "]  folder:[" + folderNameMather + "]  targetPath:[" + targetFolder + "] prename[" + targetFilePreName + "]");

        File files[] = directory.listFiles();

        if (files != null) {
            for (File f : files) {
                //递归子目录
                if (f.isDirectory() && !f.getName().equals(".") && !f.getName().equals("..")) {
                    //目录名过滤
                    logger.info("Found folder:" + f.getAbsolutePath());
                    if (match(f.getName(), folderNameMather) && isRecursive) {
                        List<String> subFiles = scanAllFile(f.getAbsolutePath());

                        if (null != subFiles) {
                            fileList.addAll(subFiles);
                        }

                    } else {
                        logger.info("Skip folder:" + f.getName());
                    }

                } else {
                    logger.info("Found file:" + f.getAbsolutePath());
                    //文件名过滤
                    if (match(f.getName(), fileNameMather)) {
                        fileList.add(f.getAbsolutePath());
                    }
                    else {
                        logger.info("Skip file:" + f.getAbsolutePath());
                    }
                }
            }
        }

        return fileList;
    }

    /**
     * 整理一个目录下的所有文件。
     * @param allFiles
     * @param idx
     * @return
     */
    private int processAllFile(List<String> allFiles, String root, int idx) {
        int processFileNum = 0;
        if (null == allFiles || allFiles.size() == 0) {
            logger.info("allFiles is null.");
            return processFileNum;
        }
        int total = allFiles.size();
        int i = 1;

        for (String fName : allFiles) {
            //扫描文件占1%的进度
            int p = (int) (i * 1.0/total * 99);
            logger.info("Progress:" + idx + " : " + p + "%");
            publishProgress(idx, p);
            i++;

            File f = new File(fName);

            Long modifyTime = getFileTime(f);

            String extName = getFileExName(f.getName());
            String newName = getFileName(targetFilePreName, modifyTime, extName);
            String distFilePath = this.targetFolder;
            if (null == distFilePath || distFilePath.isEmpty()) {
                distFilePath = root;
            }
            distFilePath += File.separator;
            if (isCreateDateFolder) {
                distFilePath += folderFormatter.format(modifyTime);
                distFilePath += File.separator;
            }

            distFilePath += newName;

            logger.info("target file:" + distFilePath);

            File newFile = new File(distFilePath);

            //如果目标文件已存在，跳过。
            if (newFile.exists()) {
                logger.error("File already exist:" + newFile.getAbsolutePath());
                continue;
            }

            File newFolder = newFile.getParentFile();

            //目标目录不存在，则创建子目录。
            if (!newFolder.exists()) {
                logger.info("Create new path folder:" + newFolder.getAbsolutePath());
                if (!newFolder.mkdirs()) {
                    logger.error("create folder failed:" + newFolder.getAbsolutePath());
                    return processFileNum;
                }
            }

            //源文件和目标文件相同，跳过。
            if (f.getAbsolutePath().equals(newFile.getAbsolutePath())) {
                logger.info("need not rename or move.");
                continue;
            }
            logger.info(f.getAbsolutePath() + " to " + newFile.getAbsolutePath());
            processFileNum += 1;
            if (!copyFile(f, newFile)) {
                logger.error("Copy file failed:" + f.getAbsolutePath());
            }
        }
        return processFileNum;
    }

    /**
     * 获取文件时间，优先从文件内容中获取。
     * @param f
     * @return
     */
    private long getFileTime(File f){
        //TODO read jpg extinf

        return f.lastModified();
    }


    /**
     * 根据文件时间生成文件名。
     *
     * @param pre
     * @param time
     * @param extName
     * @return
     */
    private static String getFileName(String pre, long time, String extName) {
        Date date = new Date(time);
        StringBuilder sb = new StringBuilder();
        sb.append(pre);
        sb.append(fileFormatter.format(date));
        sb.append(extName);

        return sb.toString();
    }


    /**
     * 获取文件后缀名
     *
     * @param filename
     * @return
     */
    public static String getFileExName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            int len = filename.length();
            if ((dot > -1) && (dot < len)) {
                return filename.substring(dot, len);
            }
        }
        return "";
    }


    // 复制文件
    public static boolean copyFile(File sFile, File tFile) {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            // 新建文件输入流并对它进行缓冲
            inBuff = new BufferedInputStream(new FileInputStream(sFile));

            // 新建文件输出流并对它进行缓冲
            outBuff = new BufferedOutputStream(new FileOutputStream(tFile));

            // 缓冲数组
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流
            outBuff.flush();
        }
        catch(IOException e) {
            logger.error("Move file failed:" , e.getMessage());

            return false;
        }

        finally {
            try {
                // 关闭流
                if (inBuff != null) {
                    inBuff.close();
                }
                if (outBuff != null) {
                    outBuff.close();
                }
            }
            catch(IOException e){
                logger.error("Close file failed:" , e.getMessage());
            }
        }

        return true;
    }

}
