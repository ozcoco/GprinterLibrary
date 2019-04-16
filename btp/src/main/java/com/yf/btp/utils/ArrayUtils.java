package com.yf.btp.utils;

public class ArrayUtils {

    public static byte[] toPrimitive(Byte[] bytes) {

        if (bytes != null) {

            final int len = bytes.length;

            byte[] dst = new byte[len];

            for (int i = 0; i < len; i++)
                dst[i] = bytes[i];

            return dst;
        }

        return null;
    }

}
