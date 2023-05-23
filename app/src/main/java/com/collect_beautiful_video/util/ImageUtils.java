package com.collect_beautiful_video.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.collect_beautiful_video.activity.ManagerVideoActivity;
import com.hjq.toast.ToastUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Created by wbin on 2016/3/22.
 */
public class ImageUtils {


    /**
     * 根据Uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    public static String getRealPathFromUri(Context context, Uri uri) {
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= 19) { // api >= 19
            return getRealPathFromUriAboveApi19(context, uri);
        } else { // api < 19
            return getRealPathFromUriBelowAPI19(context, uri);
        }
    }

    /**
     * 适配api19以下(不包括api19),根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    private static String getRealPathFromUriBelowAPI19(Context context, Uri uri) {
        return getDataColumn(context, uri, null, null);
    }

    /**
     * 适配api19及以上,根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    @SuppressLint("NewApi")
    private static String getRealPathFromUriAboveApi19(Context context, Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            String documentId = DocumentsContract.getDocumentId(uri);
            if (isMediaDocument(uri)) { // MediaProvider
                // 使用':'分割
                String id = documentId.split(":")[1];

                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = {id};
                filePath = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                filePath = getDataColumn(context, contentUri, null, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是 content 类型的 Uri
            filePath = getDataColumn(context, uri, null, null);
        } else if ("file".equals(uri.getScheme())) {
            // 如果是 file 类型的 Uri,直接获取图片对应的路径
            filePath = uri.getPath();
        }
        return filePath;
    }

    /**
     * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
     *
     * @return
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;

        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }


    /**
     * 获取视频 contentValue
     *
     * @param paramFile
     * @param paramLong
     * @return
     */
    public static ContentValues getVideoContentValues(File paramFile, long paramLong) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("title", paramFile.getName());
        localContentValues.put("_display_name", paramFile.getName());
        localContentValues.put("mime_type", "video/mp4");
        localContentValues.put("datetaken", Long.valueOf(paramLong));
        localContentValues.put("date_modified", Long.valueOf(paramLong));
        localContentValues.put("date_added", Long.valueOf(paramLong));
        localContentValues.put("_data", paramFile.getAbsolutePath());
        localContentValues.put("_size", Long.valueOf(paramFile.length()));
        return localContentValues;
    }


    /**
     * 将视频保存到系统图库
     *
     * @param videoFile
     * @param context
     */
    public static boolean saveVideoToSystemAlbum(String videoFile, Context context) {

        try {
            ContentResolver localContentResolver = context.getContentResolver();
            ContentValues localContentValues = getVideoContentValues(new File(videoFile), System.currentTimeMillis());

            Uri localUri = localContentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, localContentValues);

          //  OutputStream out = context.getContentResolver().openOutputStream(localUri);
           // copyFile(videoFile, out);
            copyFileUsingFileChannels(videoFile,localUri.toString());
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri));
            //将该文件扫描到相册
            MediaScannerConnection.scanFile(context, new String[]{videoFile}, null, new MediaScannerConnection.MediaScannerConnectionClient() {
                @Override
                public void onMediaScannerConnected() {

                }

                @Override
                public void onScanCompleted(String path, Uri uri) {
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri));

                }
            });
            ToastUtils.show("视频已经保存到相册");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 拷贝文件
     *
     * @param oldPath
     * @param out
     * @return
     */
    public static boolean copyFile(String oldPath, OutputStream out) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                // 读入原文件
                InputStream inStream = new FileInputStream(oldPath);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    out.write(buffer, 0, byteread);
                }
                inStream.close();
                out.close();
                return true;
            } else {
                Log.w("wjy", String.format("文件(%s)不存在。", oldPath));
            }
        } catch (Exception e) {
            Log.e("wjy", "复制单个文件操作出错");
            e.printStackTrace();
        }

        return false;
    }

    public static void saveImgToLocal(Context context, String url, String fileName, int width, int height) {
        Glide.with(context)
                .asBitmap()
                .load(url)
                .skipMemoryCache(true)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        changeBitmapSize(context, resource, fileName, width, height);
                    }

                });
    }

    /**
     * 复制文件
     *
     * @param source 输入文件
     * @param target 输出文件
     */
    private static void copy(File source, File target) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileInputStream = new FileInputStream(source);
            fileOutputStream = new FileOutputStream(target);
            byte[] buffer = new byte[1024];
            while (fileInputStream.read(buffer) > 0) {
                fileOutputStream.write(buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileInputStream.close();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    static void saveBitmap(String name, Bitmap bm) {
        //如果指定文件夹创建成功，那么我们则需要进行图片存储操作
        File saveFile = new File(name);
        try {
            FileOutputStream saveImgOut = new FileOutputStream(saveFile);
            // compress - 压缩的意思
            bm.compress(Bitmap.CompressFormat.JPEG, 60, saveImgOut);
            //存储完成后需要清除相关的进程
            saveImgOut.flush();
            saveImgOut.close();
            Log.d("Save Bitmap", "The picture is save to your phone!");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public static void changeBitmapSize(Context context, final Bitmap bitmap, String targetSource, int newWidth, int newHeight) {
        Observable.create(new ObservableOnSubscribe<File>() {
                    @Override
                    public void subscribe(ObservableEmitter<File> e) throws Exception {
                        int width = bitmap.getWidth();
                        int height = bitmap.getHeight();
                        Log.e("width", "width:" + width);
                        Log.e("height", "height:" + height);


                        //计算压缩的比率
                        float scaleWidth = ((float) newWidth) / width;
                        float scaleHeight = ((float) newHeight) / height;

                        //获取想要缩放的matrix
                        Matrix matrix = new Matrix();
                        matrix.postScale(scaleWidth, scaleHeight);

                        //获取新的bitmap
                        Bitmap newBitMap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                        Log.e("wjy", "newWidth" + newBitMap.getWidth());
                        Log.e("wjy", "newHeight" + newBitMap.getHeight());
                        saveBitmap(targetSource, newBitMap);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) throws Exception {


                    }

                });

    }


    public static Bitmap rsBlur(Context context, Bitmap source, int radius, String name) {

        if(source != null) {
          Bitmap inputBmp = source;
          //(1)
          RenderScript renderScript = RenderScript.create(context);

          Log.i("wjy", "scale size:" + inputBmp.getWidth() + "*" + inputBmp.getHeight());

          // Allocate memory for Renderscript to work with
          //(2)
          final Allocation input = Allocation.createFromBitmap(renderScript, inputBmp);
          final Allocation output = Allocation.createTyped(renderScript, input.getType());
          //(3)
          // Load up an instance of the specific script that we want to use.
          ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
          //(4)
          scriptIntrinsicBlur.setInput(input);
          //(5)
          // Set the blur radius
          scriptIntrinsicBlur.setRadius(radius);
          //(6)
          // Start the ScriptIntrinisicBlur
          scriptIntrinsicBlur.forEach(output);
          //(7)
          // Copy the output to the blurred bitmap
          output.copyTo(inputBmp);
          //(8)
          renderScript.destroy();
          saveBitmap(name, inputBmp);
          return inputBmp;
        }
        return null;
    }

    public static String getSDCardDCIMFile(String suffix) {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "DCIM" + File.separator + SystemClock.elapsedRealtime() + suffix;
    }

    /**
     * 拷贝文件
     *
     * @param sourcePath 原始文件路径
     * @param targetPath 目标存放文件路径
     */
    public static boolean copyFile(String sourcePath, String targetPath) {

        //文件非空判断
        if (TextUtils.isEmpty(sourcePath) || TextUtils.isEmpty(targetPath)) {
            return false;
        }

        File source = new File(sourcePath);
        File target = new File(targetPath);

        //源文件，和目标文件是同一个文件，并且目标文件存在，直接返回
        if (sourcePath.equals(targetPath) && target.exists()) {
            return false;
        }

        if (!target.exists()) {
            String path = targetPath.substring(0, targetPath.lastIndexOf("/"));
            File s = new File(path);
            s.mkdirs();
        }


        InputStream in = null;
        OutputStream out = null;
        try {
            FileOutputStream targetStream=new FileOutputStream(target);
            in = new BufferedInputStream(new FileInputStream(source));
            out = new BufferedOutputStream(targetStream);
            byte[] buf = new byte[8192];
            int i;
            while ((i = in.read(buf)) != -1) {
                out.write(buf, 0, i);
                out.flush();
            }
            targetStream.getFD().sync();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("wjy", e.getLocalizedMessage());
        } finally {
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;

    }

  public static void copyFileUsingFileChannels(String sourcePath, String targetPath)  {

    if (TextUtils.isEmpty(sourcePath) || TextUtils.isEmpty(targetPath)) {
      return;
    }
    try {
      File source = new File(sourcePath);
      File target = new File(targetPath);
      FileChannel inputChannel = null;
      FileChannel outputChannel = null;
      try {
        inputChannel = new FileInputStream(source).getChannel();
        outputChannel = new FileOutputStream(target).getChannel();
        outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        outputChannel.force(true);
        inputChannel.force(true);
      } finally {
        inputChannel.close();
        outputChannel.close();
      }
    }catch (Exception e){

    }

  }



}

