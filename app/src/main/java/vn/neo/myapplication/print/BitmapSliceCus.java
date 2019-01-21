package vn.neo.myapplication.print;

/**
 * Bitmap slice.
 *
 * @author feiq
 */

import android.graphics.Bitmap;

/**
 * Bitmap slice.
 */
class BitmapSliceCus {
    public BitmapSliceCus(int index, Bitmap bitmap) {
        this.index = index;
        this.bitmap = bitmap;
    }

    /**
     * Slice index.
     */
    public int index;

    /**
     * Bitmap slice data.
     */
    public Bitmap bitmap;
}
