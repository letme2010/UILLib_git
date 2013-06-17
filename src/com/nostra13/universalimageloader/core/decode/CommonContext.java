
package com.nostra13.universalimageloader.core.decode;

import android.content.ContentResolver;

public class CommonContext {

    private static ContentResolver sContentResolver;

    public static ContentResolver getContentResolver() {
        return sContentResolver;
    }

    public static void setContentResolver(ContentResolver aContentResolver) {
        sContentResolver = aContentResolver;
    }

}
