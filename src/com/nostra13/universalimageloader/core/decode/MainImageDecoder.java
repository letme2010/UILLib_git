
package com.nostra13.universalimageloader.core.decode;

import java.io.IOException;

import android.graphics.Bitmap;

public class MainImageDecoder implements ImageDecoder {

    private UcBaseImageDecoder mUcBaseImageDecoder;

    private BaseImageDecoder mBaseImageDecoder;

    private ImageDecoder getUcBaseImageDecoder() {
        if (null == this.mUcBaseImageDecoder) {
            this.mUcBaseImageDecoder = new UcBaseImageDecoder(false);
        }
        return this.mUcBaseImageDecoder;
    }

    private ImageDecoder getBaseImageDecoder() {
        if (null == this.mBaseImageDecoder) {
            this.mBaseImageDecoder = new BaseImageDecoder();
        }
        return this.mBaseImageDecoder;
    }

    @Override
    public Bitmap decode(ImageDecodingInfo aImageDecodingInfo) throws IOException {

        if (null == aImageDecodingInfo) {
            return null;
        }

        String imageKey = aImageDecodingInfo.getImageKey();

        if (null == imageKey) {
            return null;
        }

        if (imageKey.toLowerCase().startsWith("http") || imageKey.toLowerCase().startsWith("https")) {
            return this.getBaseImageDecoder().decode(aImageDecodingInfo);
        } else {
            return this.getUcBaseImageDecoder().decode(aImageDecodingInfo);
        }

    }

}
