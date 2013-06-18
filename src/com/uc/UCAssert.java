
package com.uc;

public class UCAssert {
    public static final void mustOk(boolean aOk) {
        if (!aOk) {
            throw new RuntimeException();
        }
    }

    public static final void mustOk(boolean aOk, String aErrMsg) {
        if (!aOk) {
            throw new RuntimeException(aErrMsg);
        }
    }
}
