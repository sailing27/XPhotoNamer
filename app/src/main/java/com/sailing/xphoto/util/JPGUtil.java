package com.sailing.xphoto.util;

import android.media.ExifInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Timestamp;

/**
 * Created by sailing on 15-4-17.
 */
public class JPGUtil {
    private static Logger logger = LoggerFactory.getLogger(JPGUtil.class);

    public static Long readExifTime(File f) {
        if (f == null) {
            logger.error("f is null.");
            return null;
        }
        try {
            ExifInterface exifInterface = new ExifInterface(f.getAbsolutePath());
            String fileTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);

            //处理2015:04:16 11:49:36的时间格式
            if(!fileTime.contains("-")) {
                fileTime = fileTime.replaceFirst(":", "-");
                fileTime = fileTime.replaceFirst(":", "-");
            }

            logger.info("FileTime from exif:" + fileTime + " @ " + f.getName());

            Timestamp tsm = Timestamp.valueOf(fileTime);
            return tsm.getTime();

        } catch (Exception e) {
            logger.error("readMetadata error:" + f.getName(), e);
        }

        return null;
    }

}
