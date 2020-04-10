package com.ilife.home.robot.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by chengjiaping on 2017/7/20.
 */

public class BitmapUtils {
//    public static Bitmap compressBitmap(Context context,int resId,int size) {
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = size;
//        Bitmap bm = BitmapFactory.decodeResource(context.getResources(),resId,options);
//        return bm;
//    }

    public static void cropBitmap(Activity activity, Uri uri, File desFile, int tag) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 3);
        intent.putExtra("aspectY", 4);
        intent.putExtra("outputX", 90);
        intent.putExtra("outputY", 120);
        intent.putExtra("scale", true);
        if (desFile.exists()) {
            boolean isDelete = desFile.delete();
            if (isDelete) {
                try {
                    desFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(desFile));
        intent.putExtra("return-data", false);
        activity.startActivityForResult(intent, tag);// 启动裁剪程序
    }


    public static byte[] bitmapToByte(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        byte[] bytes = out.toByteArray();
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static Bitmap compressBitmap(Activity ac, Uri uri, int imageWidth, int imageHeight) {
        Bitmap bitmap = null;
        try {
            InputStream in = ac.getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            int optionWidth = options.outWidth;
            int optionHeight = options.outHeight;
            int inSampleSize = 1;
            in.close();
            if (optionHeight > imageHeight || optionWidth > imageWidth) {
                final int heightRatio = Math.round((float) optionHeight / (float) imageHeight);
                final int widthRatio = Math.round((float) optionWidth / (float) imageWidth);
                inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            }
            InputStream stream = ac.getContentResolver().openInputStream(uri);
            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeStream(stream, null, options);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            MyLogger.e("compressBitmap e", e.toString());
        }
        return bitmap;
    }

    public static Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(1500, 1500, Bitmap.Config.ARGB_8888);
        if (!bitmap.isMutable()) {
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        }
        return bitmap;
    }
    public static Bitmap decodeBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight){
        final BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeResource(res,resId,options);
        options.inSampleSize=1;
        options.inSampleSize=setInSampleSize(options,reqWidth,reqHeight);
        options.inPreferredConfig= Bitmap.Config.ARGB_4444;
        options.inJustDecodeBounds=false;
        return BitmapFactory.decodeResource(res,resId,options);
    }

    public static int setInSampleSize(BitmapFactory.Options options,int reqWidth,int reqHeight){
        final  int height=options.outHeight;
        final int width=options.outWidth;
        int inSampleSize=1;
        if(height>reqHeight||width>reqHeight){
            final int halfHeght=height/2;
            final int halfWidht=width/2;
            while ((halfHeght/inSampleSize>=reqHeight)&&(halfWidht/inSampleSize>=reqWidth)){
                inSampleSize *=2;
            }
        }
        return inSampleSize;
    }
}
