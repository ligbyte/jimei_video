package com.collect_beautiful_video.util;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.File;

public class FileUtil {


    public static String getVideoPath(Context context) {
        String path = ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_DCIM)[0].getAbsolutePath() + "/collect_beautiful_video";
        Log.d("wjy",path);
        File file = new File(path);
        file.deleteOnExit();
        file.mkdirs();

        return path;
    }

    public static boolean isVideoHaveAudioTrack(String path) {
        boolean audioTrack = false;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        String hasAudioStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO);
        if ("yes".equalsIgnoreCase(hasAudioStr)) {
            audioTrack = true;
        } else {
            audioTrack = false;
        }

        return audioTrack;
    }

}
