package vn.neo.myapplication.print;

/**
 * Printing data slice.
 *
 * @author feiq
 */

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

public class PrintingDataSliceCus implements Comparable<PrintingDataSliceCus> {
    public PrintingDataSliceCus(int index, Bitmap data) {
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
    public int compareTo(@NonNull PrintingDataSliceCus target) {
        return ((Integer) this.index).compareTo(target.index);

    }
}
