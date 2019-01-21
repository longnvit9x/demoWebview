package vn.neo.myapplication.print;

/**
 * Printing data slice.
 *
 * @author feiq
 */

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

public class PrintingDataSlice implements Comparable<PrintingDataSlice> {
    public PrintingDataSlice(int index, Bitmap data) {
        this.index = index;
        this.data = data;
    }

    /**
     * Slice index.
     */
    public int index;

    /**
     * Bitmap slice data.
     */
    public Bitmap data;

    @Override
    public int compareTo(@NonNull PrintingDataSlice target) {
        return ((Integer) this.index).compareTo(target.index);

    }
}
