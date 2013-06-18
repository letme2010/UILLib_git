
package com.nostra13.universalimageloader.core.decode;

import android.content.ContentResolver;
import android.content.Context;

public class CommonContext {

    private static ContentResolver sContentResolver;

    private static Context sContext;

    public static ContentResolver getContentResolver() {
        return sContentResolver;
    }

    public static void setContentResolver(ContentResolver aContentResolver) {
        sContentResolver = aContentResolver;
    }

    public static void setContext(Context aContext) {
        sContext = aContext;
    }

    public static Context getContext() {
        return sContext;
    }

}
