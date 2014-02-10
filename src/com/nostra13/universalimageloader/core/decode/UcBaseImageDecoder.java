
package com.nostra13.universalimageloader.core.decode;

import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;
import com.uc.UCAssert;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UcBaseImageDecoder implements ImageDecoder {

    private static final byte MEDIA_TYPE_IMAGE = 1;

    private static final byte MEDIA_TYPE_VIDEO = 2;

    private static final List<QueryInfo> sQueryList = new ArrayList<QueryInfo>();

    private static final int MINI_KIND_THUMBNAIL_IMAGE_WIDTH = 512;

    private static final int MINI_KIND_THUMBNAIL_IMAGE_HEIGHT = 384;

    static {
        sQueryList.add(new QueryInfo("content://media/external/images/media",
                MediaStore.Images.Thumbnails._ID, MediaStore.Images.Thumbnails.DATA,
                MEDIA_TYPE_IMAGE));

        // sQueryList
        // .add(new QueryInfo("content://media/external/video/media",
        // MediaStore.Video.Thumbnails._ID, MediaStore.Video.Thumbnails.DATA,
        // MEDIA_TYPE_VIDEO));
    }

    public UcBaseImageDecoder(boolean loggingEnabled) {
    }

    private class ResultInfo {

        private int mId;

        private byte mMediaType;

        private boolean mSuccess;

        public ResultInfo() {

        }

        public ResultInfo(int aId, byte aMediaType, boolean aSuccess) {
            super();
            mId = aId;
            mMediaType = aMediaType;
            mSuccess = aSuccess;
        }

        public int getId() {
            return mId;
        }

        public void setId(int aId) {
            mId = aId;
        }

        public byte getMediaType() {
            return mMediaType;
        }

        public void setMediaType(byte aMediaType) {
            mMediaType = aMediaType;
        }

        public boolean isSuccess() {
            return mSuccess;
        }

        public void setSuccess(boolean aSuccess) {
            mSuccess = aSuccess;
        }

    }

    private static class QueryInfo {
        private String mURI;

        private String mColumnId;

        private String mColumnData;

        private byte mMediaType;

        public QueryInfo(String aURI, String aColumnId, String aColumnData, byte aMediaType) {
            super();
            mURI = aURI;
            mColumnId = aColumnId;
            mColumnData = aColumnData;
            mMediaType = aMediaType;
        }

        public String getURI() {
            return mURI;
        }

        public void setURI(String aURI) {
            mURI = aURI;
        }

        public String getColumnId() {
            return mColumnId;
        }

        public void setColumnId(String aColumnId) {
            mColumnId = aColumnId;
        }

        public String getColumnData() {
            return mColumnData;
        }

        public void setColumnData(String aColumnData) {
            mColumnData = aColumnData;
        }

        public byte getMediaType() {
            return mMediaType;
        }

        public void setMediaType(byte aMediaType) {
            mMediaType = aMediaType;
        }

    }

    private int calculateInSampleSize(int imgWidth, int imgHeight, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = imgHeight;
        final int width = imgWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float)height / (float)reqHeight);
            final int widthRatio = Math.round((float)width / (float)reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    private Bitmap getBitmap(byte aMediaType, int aId, Options aDecodingOptions) {
        Bitmap ret = null;

        switch (aMediaType) {
            case MEDIA_TYPE_IMAGE: {
                ret = this.getBitmapByImageId(aId, aDecodingOptions);
                break;
            }
            case MEDIA_TYPE_VIDEO: {
                ret = this.getBitmapByVideoId(aId, aDecodingOptions);
                break;
            }
            default: {
                UCAssert.mustOk(false);
                break;
            }
        }

        return ret;
    }

    private Bitmap getBitmapByImageId(int aId, Options aDecodingOptions) {

        return MediaStore.Images.Thumbnails.getThumbnail(CommonContext.getContentResolver(), aId,
                MediaStore.Images.Thumbnails.MINI_KIND, aDecodingOptions);
    }

    private Bitmap getBitmapByVideoId(int aId, Options aDecodingOptions) {

        return MediaStore.Video.Thumbnails.getThumbnail(CommonContext.getContentResolver(), aId,
                MediaStore.Video.Thumbnails.MINI_KIND, aDecodingOptions);
    }

    private ResultInfo getResultInfo(QueryInfo aQueryInfo, String aFilePath) {

        ResultInfo ret = new ResultInfo();
        ret.setMediaType(aQueryInfo.getMediaType());

        try {

            String[] projection = new String[] {
                    aQueryInfo.getColumnId(), aQueryInfo.getColumnData()
            };

            Uri uri = Uri.parse(aQueryInfo.getURI());

            if (null != uri) {

                Cursor cursor = CommonContext.getContentResolver().query(uri, projection,
                        aQueryInfo.getColumnData() + " = ?", new String[] {
                            aFilePath
                        }, null);

                if (null != cursor) {

                    if (1 == cursor.getCount()) {

                        cursor.moveToFirst();

                        int imageId = cursor.getInt(0);

                        ret.setId(imageId);
                        ret.setSuccess(true);
                    }

                    cursor.close();

                } else {
                    throw new RuntimeException("cursor is null.");
                }

            } else {
                throw new RuntimeException("uri is null.");
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return ret;

    }

    private Bitmap getThumbnail(String aFilePath, Options aDecodingOptions) {

        UCAssert.mustOk(null != aFilePath);

        Bitmap ret = null;

        for (QueryInfo queryInfo : sQueryList) {
            ResultInfo resultInfo = this.getResultInfo(queryInfo, aFilePath);

            if (resultInfo.isSuccess()) {
                ret = this.getBitmap(resultInfo.getMediaType(), resultInfo.getId(),
                        aDecodingOptions);
                break;
            }
        }

        return ret;
    }

    private Bitmap getApkIcon(String aApkPath) {

        // UCAssert.mustOk(null != aApkPath);
        // UCAssert.mustOk(aApkPath.toLowerCase(Locale.getDefault()).endsWith(".apk"));

        Bitmap ret = null;

        UCAssert.mustOk(new File(aApkPath).exists());

        PackageInfo packageInfo = CommonContext.getContext().getPackageManager()
                .getPackageArchiveInfo(aApkPath, PackageManager.GET_ACTIVITIES);

        if (packageInfo != null) {
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            if (Build.VERSION.SDK_INT >= 8) {
                appInfo.sourceDir = aApkPath;
                appInfo.publicSourceDir = aApkPath;
            }
            Drawable icon = appInfo.loadIcon(CommonContext.getContext().getPackageManager());
            ret = ((BitmapDrawable)icon).getBitmap();
        }

        UCAssert.mustOk(null != ret);

        return ret;
    }

    /**
     * @param aExp such as:"145x120".
     * @return array of width and height.
     */
    private int[] getRequestSize(String aExp) {
        String[] array = aExp.split("x");

        UCAssert.mustOk(2 == array.length);

        return new int[] {
                Integer.valueOf(array[0]), Integer.valueOf(array[1])
        };

    }

    private int[] getBitmapSize(String aFilePath) {

        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(aFilePath, option);

        return new int[] {
                option.outWidth, option.outHeight
        };

    }

    private int[] getRequestSizeByImageKey(String aImageKey) {

        UCAssert.mustOk(null != aImageKey);

        String oriFilePath = Scheme.FILE.crop(aImageKey);
        String sizeExp = oriFilePath.substring(oriFilePath.lastIndexOf('_') + 1,
                oriFilePath.length());
        int[] requestSize = this.getRequestSize(sizeExp);

        return requestSize;
    }

    @Override
    public Bitmap decode(ImageDecodingInfo imageDecodingInfo) throws IOException {

        String imageKey = imageDecodingInfo.getImageKey();

        if (null == imageKey) {
            return null;
        }

        if (imageKey.startsWith(Scheme.HTTP.name()) || imageKey.startsWith(Scheme.HTTPS.name())) {
            BaseImageDecoder baseDecoder = new BaseImageDecoder();
            return baseDecoder.decode(imageDecodingInfo);
        }

        Bitmap ret = null;
        try {

            String filePath = Scheme.FILE.crop(imageDecodingInfo.getImageKey());
            filePath = filePath.substring(0, filePath.lastIndexOf('_'));

            String filePathL = filePath.toLowerCase(Locale.getDefault());

            if (filePathL.endsWith(".apk")) {
                ret = this.getApkIcon(filePath);
            } else {

                int[] requestSize = this.getRequestSizeByImageKey(imageDecodingInfo.getImageKey());

                imageDecodingInfo.getDecodingOptions().inSampleSize = this.calculateInSampleSize(
                        MINI_KIND_THUMBNAIL_IMAGE_WIDTH, MINI_KIND_THUMBNAIL_IMAGE_HEIGHT,
                        requestSize[0], requestSize[1]);

                ret = this.getThumbnail(filePath, imageDecodingInfo.getDecodingOptions());

                if (null == ret) {
                    if (filePathL.endsWith(".png") || filePathL.endsWith(".jpg")
                            || filePathL.endsWith(".jpeg") || filePathL.endsWith(".gif")
                            || filePathL.endsWith(".tif") || filePathL.endsWith(".bmp")
                            || filePathL.endsWith(".webp")) {

                        int[] bitmapSize = this.getBitmapSize(filePath);

                        imageDecodingInfo.getDecodingOptions().inSampleSize = this
                                .calculateInSampleSize(bitmapSize[0], bitmapSize[1],
                                        requestSize[0], requestSize[1]);

                        ret = BitmapFactory.decodeFile(filePath,
                                imageDecodingInfo.getDecodingOptions());

                    }
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

        return ret;
    }

    // @Override
    // public Bitmap decode(ImageDecodingInfo imageDecodingInfo) throws
    // IOException {
    //
    // String imagePath = Scheme.FILE.crop(imageDecodingInfo.getImageKey());
    //
    // imagePath = imagePath.substring(0, imagePath.lastIndexOf('_'));
    //
    // Bitmap bitmap = this.getThumbnailByImageFilePath(imagePath);
    //
    // if (null == bitmap) {
    // BitmapFactory.Options option = new BitmapFactory.Options();
    // option.inSampleSize = this.calculateInSampleSize(512, 384, 145, 120);
    // bitmap = BitmapFactory.decodeFile(imagePath, option);
    // }
    //
    // return bitmap;
    //
    // }

}
