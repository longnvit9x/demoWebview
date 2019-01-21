package vn.neo.myapplication.print;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Generate printing slices according to specified big bitmap.
 *
 * @author feiq, guosx
 */
public class PrintingDataGeneratorCus {

    private static final int BITMAP_BLOCK_SIZE = 256;
    private static final int BITMAP_SLICE_INCREASE_SIZE = 0;

    /**
     * Generate printing slices according to bitmap.
     */
    public static Observable<PrintingDataSliceCus> generatePrintingSlices(final Bitmap bitmap) {
        final Long[] startTime = new Long[1];
        Single<List<BitmapSliceCus>> slicesGenerator = Single.fromCallable(() -> {
            startTime[0] = Calendar.getInstance().getTimeInMillis();

            if (bitmap == null) {
                throw new Exception("The printing bitmap is null");
            }

            // Split bitmap and run multiple threads to handle bitmap in order to improve performance
            List<BitmapSliceCus> bitmapSlices = splitBitmap(bitmap, BITMAP_BLOCK_SIZE, BITMAP_SLICE_INCREASE_SIZE);
            recycleBitmap(bitmap);
            return bitmapSlices;
        });

        return slicesGenerator.flatMapObservable(Observable::fromIterable)
                .flatMapSingle(PrintingDataGeneratorCus::generatePrintingSlice)
                .doOnComplete(() -> Timber.d("PrintingDataGeneratorCus.generatePrintingSlices[" + (Calendar.getInstance().getTimeInMillis() - startTime[0]) + "]ms"));
    }


    /**
     * Generate printing source from bitmap.
     * <p>
     * 1. Binarize
     * 2. Create new Bitmap.
     * 3. Generate printing data.
     *
     * @param bitmapSlice bitmapSlice.
     * @return
     */

    private static Single<PrintingDataSliceCus> generatePrintingSlice(BitmapSliceCus bitmapSlice) {
        return Single.fromCallable(() -> {
            long startTime = Calendar.getInstance().getTimeInMillis();

            Bitmap bitmap = bitmapSlice.bitmap;
            int index = bitmapSlice.index;
            Timber.d("PrintingDataGeneratorCus.generatePrintingSlice[" + index + "] generated" + "[" + (Calendar.getInstance().getTimeInMillis() - startTime) + "]ms");

            return new PrintingDataSliceCus(index, bitmap);
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Split the specified bitmap to multi bitmap according the height.
     *
     * @param bitmap        The bitmap to be split
     * @param firstHeight   First bitmap height
     * @param increaseSpace Slice height increase space
     * @return The list of bitmap
     */
    private static List<BitmapSliceCus> splitBitmap(Bitmap bitmap, int firstHeight, int increaseSpace) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int pointY = 0;
        int actualSliceHeight = firstHeight;

        ArrayList<BitmapSliceCus> slices = new ArrayList<>();
        int sliceCount = 1;

        while (pointY < height) {

            if (pointY + actualSliceHeight >= height) {
                actualSliceHeight = height - pointY;
            }

            Bitmap slice = Bitmap.createBitmap(bitmap, 0, pointY, width, actualSliceHeight);
            // add conver RGB and resize 572
            Bitmap bm1 = convertGreyImg(slice);
            Bitmap bm2 = resizeImage(bm1, 572, false);
            //
            slices.add(new BitmapSliceCus(sliceCount++, bm2));

            pointY += actualSliceHeight;
            actualSliceHeight += increaseSpace;
        }

        return slices;
    }

    /**
     * recycleBitmap
     * Avoid OOM
     *
     * @param bitmap The bitmap to be recycle
     */
    private static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    /**
     * Convert bitmap from argb to gray.
     * <p>
     * 二值化转换.
     *
     * @param bmpOriginal Bitmap
     * @param width       Bitmap width
     * @param height      Bitmap height
     * @param graySource  the gray data array generated from bitmap.
     */
    private static void convertArgbToGrayscale(Bitmap bmpOriginal, int width, int height, byte[] graySource, int dataWidth) {
        int pixel;
        int k = 0;
        int B = 0, G = 0, R = 0;
        int threshold = 150; // Gray scale threshold 灰度阈值
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++, k++) {
                // 获取一个像素的颜色值
                // Get pixel's color
                pixel = bmpOriginal.getPixel(y, x);
                // 获取RGB数据
                // Get RGB data
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                // 转换成灰度值
                R = (int) ((float) 0.299 * R + (float) 0.587 * G + (float) 0.114 * B);

                if (R < threshold) {
                    graySource[k] = 0;
                } else {
                    graySource[k] = 1;
                }
            }

            if (dataWidth > width) {
                for (int p = width; p < dataWidth; p++, k++) {
                    graySource[k] = 1;
                }
            }
        }
    }

    /**
     * Convert gray source to bitmap source.
     * <p>
     * Merge 8 bytes from gray source to 1 byte.
     *
     * @param graySource   gray source.
     * @param bitmapSource bitmap source.
     */
    private static void createRawMonochromeData(byte[] graySource, byte[] bitmapSource) {
        int length = 0;
        for (int i = 0; i < graySource.length; i = i + 8) {
            byte first = graySource[i];
            for (int j = 1; j <= 7; j++) {
                first = (byte) ((first << 1) | graySource[i + j]);
            }
            bitmapSource[length] = first;
            length++;
        }
    }

    public static Bitmap convertGreyImg(Bitmap img) {
        int width = img.getWidth();
        int height = img.getHeight();

        int[] pixels = new int[width * height];

        img.getPixels(pixels, 0, width, 0, 0, width, height);


        //The arithmetic average of a grayscale image; a threshold
        double redSum = 0, greenSum = 0, blueSun = 0;
        double total = width * height;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);


                redSum += red;
                greenSum += green;
                blueSun += blue;


            }
        }
        int m = (int) (redSum / total);

        //Conversion monochrome diagram
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int alpha1 = 0xFF << 24;
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);


                if (red >= m) {
                    red = green = blue = 255;
                } else {
                    red = green = blue = 0;
                }
                grey = alpha1 | (red << 16) | (green << 8) | blue;
                pixels[width * i + j] = grey;


            }
        }
        Bitmap mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        mBitmap.setPixels(pixels, 0, width, 0, 0, width, height);


        return mBitmap;
    }


    /*
        use the Matrix compress the bitmap
	 *   */
    public static Bitmap resizeImage(Bitmap bitmap, int w, boolean ischecked) {

        Bitmap BitmapOrg = bitmap;
        Bitmap resizedBitmap = null;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        if (width <= w) {
            return bitmap;
        }
        if (!ischecked) {
            int newWidth = w;
            int newHeight = height * w / width;

            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;

            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            // if you want to rotate the Bitmap
            // matrix.postRotate(45);
            resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                    height, matrix, true);
        } else {
            resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, w, height);
        }

        return resizedBitmap;
    }
}