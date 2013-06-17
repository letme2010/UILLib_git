/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.nostra13.universalimageloader.core.decode;

import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;

import java.io.IOException;

/**
 * Decodes images to {@link Bitmap}, scales them to needed size
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.8.3
 * @see ImageDecodingInfo
 */
public class UcBaseImageDecoder implements ImageDecoder {

    private static final String TAG = "UcBaseImageDecoder";

    public UcBaseImageDecoder(boolean loggingEnabled) {
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

    private Bitmap getBitmapByImageId(int aImageId) {

        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inSampleSize = this.calculateInSampleSize(512, 384, 145, 120);

        return MediaStore.Images.Thumbnails.getThumbnail(CommonContext.getContentResolver(),
                aImageId, Thumbnails.MINI_KIND, option);
    }

    private Bitmap getThumbnailByImageFilePath(String aImageFilePath) {
        Bitmap bitmap = null;

        try {

            String[] projection = new String[] {
                    MediaStore.Images.Thumbnails._ID, MediaStore.Images.Thumbnails.DATA
            };

            Uri uri = Uri.parse("content://media/external/images/media");

            if (null != uri) {

                Cursor cursor = CommonContext.getContentResolver().query(uri, projection,
                        MediaStore.Images.Thumbnails.DATA + " = ?", new String[] {
                            aImageFilePath
                        }, null);

                if (null != cursor) {

                    if (1 == cursor.getCount()) {

                        cursor.moveToFirst();

                        int imageId = cursor.getInt(0);
                        cursor.close();
                        bitmap = this.getBitmapByImageId(imageId);

                    } else {
                        throw new RuntimeException("image not found");
                    }

                } else {
                    throw new RuntimeException("cursor is null.");
                }

            } else {
                throw new RuntimeException("uri is null.");
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    @Override
    public Bitmap decode(ImageDecodingInfo imageDecodingInfo) throws IOException {

        String imagePath = Scheme.FILE.crop(imageDecodingInfo.getImageKey());

        imagePath = imagePath.substring(0, imagePath.lastIndexOf('_'));

        Bitmap bitmap = this.getThumbnailByImageFilePath(imagePath);

        if (null == bitmap) {
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inSampleSize = this.calculateInSampleSize(512, 384, 145, 120);
            bitmap = BitmapFactory.decodeFile(imagePath, option);
        }

        return bitmap;

    }

}
