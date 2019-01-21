package vn.neo.myapplication.print

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.provider.Settings
import android.webkit.WebView
import io.reactivex.Completable
import io.reactivex.Observable
import net.posprinter.utils.BitmapToByteData
import net.posprinter.utils.DataForSendToPrinterPos80
import timber.log.Timber
import java.io.IOException
import java.net.Socket
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.TimeUnit


object PrinterUtils {
    const val RESULT_SUCCESS = 1
    const val RESULT_ERROR = 2
    const val RESULT_CONNECT_ERROR = 3
    const val RESULT_HOST_ERROR = 4
    fun addHtmlAndPrint(ip: String, templateId: String, json: String, onCallBack: (result: Int) -> Unit): Completable {
        val startTime = LongArray(1)
        return Completable.complete()
                .doOnComplete { startTime[0] = Calendar.getInstance().timeInMillis }
                .andThen(generatePrintingSlices(templateId, json))
                .toList()
                .doOnSuccess { slices ->
                    ProcessConnectAndPrinter(object : ProcessConnectAndPrinter.ListenerProcessPrinter {
                        override fun onPostDone(result: Int) {
                            onCallBack.invoke(result)
                        }
                    }).execute(Pair(ip, slices))
                }.toCompletable()
                .doOnComplete { Timber.d("Printer.addHtml[" + (Calendar.getInstance().timeInMillis - startTime[0]) + "]ms") }
    }

    private fun generatePrintingSlices(templateId: String, json: String): Observable<PrintingDataSlice> {
        return PrintingWebViewCus.getInstance().loadPrintingContent(templateId, json)
                .delay(1000, TimeUnit.MILLISECONDS)
                .andThen(PrintingWebViewCus.getInstance().captureWebView())
                .flatMapObservable(PrintingDataGenerator::generatePrintingSlices)
    }

    fun preLoadHtml(templateId: String, json: String): Completable {
        return PrintingWebViewCus.getInstance().loadPrintingContent(templateId, json)
                .andThen(PrintingWebViewCus.getInstance().captureWebView())
                .toCompletable()
    }

    fun initWebView(context: Context,webview: WebView) {
        val startTime = Calendar.getInstance().timeInMillis

        PrintingWebViewCus.getInstance().create(context,webview)
        //PrintingWebViewCus.getInstance().indexUrl="file:///android_asset/print-template-tablet/index.html";
//        // before load page
        Timber.d("Printer.initWebView[" + (Calendar.getInstance().timeInMillis - startTime) + "]ms")
    }


    class ProcessConnectAndPrinter constructor(private val listener: ListenerProcessPrinter) : AsyncTask<Pair<String, List<PrintingDataSlice>>, String, Int>() {
        interface ListenerProcessPrinter {
            fun onPostDone(result: Int)
        }

        private fun resizeImage(bitmap: Bitmap, w: Int, ischecked: Boolean): Bitmap? {

            var resizedBitmap: Bitmap? = null
            val width = bitmap.width
            val height = bitmap.height
            if (width <= w) {
                return bitmap
            }
            resizedBitmap = if (!ischecked) {
                val newHeight = height * w / width

                val scaleWidth = w.toFloat() / width
                val scaleHeight = newHeight.toFloat() / height

                val matrix = Matrix()
                matrix.postScale(scaleWidth, scaleHeight)
                // if you want to rotate the Bitmap
                // matrix.postRotate(45);
                Bitmap.createBitmap(bitmap, 0, 0, width,
                        height, matrix, true)
            } else {
                Bitmap.createBitmap(bitmap, 0, 0, w, height)
            }

            return resizedBitmap
        }

        private fun convertGreyImg(img: Bitmap): Bitmap {
            val width = img.width
            val height = img.height

            val pixels = IntArray(width * height)

            img.getPixels(pixels, 0, width, 0, 0, width, height)


            //The arithmetic average of a grayscale image; a threshold
            var redSum = 0.0
            var greenSum = 0.0
            var blueSun = 0.0
            val total = (width * height).toDouble()

            for (i in 0 until height) {
                for (j in 0 until width) {
                    val grey = pixels[width * i + j]

                    val red = grey and 0x00FF0000 shr 16
                    val green = grey and 0x0000FF00 shr 8
                    val blue = grey and 0x000000FF


                    redSum += red.toDouble()
                    greenSum += green.toDouble()
                    blueSun += blue.toDouble()


                }
            }
            val m = (redSum / total).toInt()

            //Conversion monochrome diagram
            for (i in 0 until height) {
                for (j in 0 until width) {
                    var grey = pixels[width * i + j]

                    val alpha1 = 0xFF shl 24
                    var red = grey and 0x00FF0000 shr 16
                    var green = grey and 0x0000FF00 shr 8
                    var blue = grey and 0x000000FF


                    if (red >= m) {
                        blue = 255
                        green = blue
                        red = green
                    } else {
                        blue = 0
                        green = blue
                        red = green
                    }
                    grey = alpha1 or (red shl 16) or (green shl 8) or blue
                    pixels[width * i + j] = grey


                }
            }
            val mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            mBitmap.setPixels(pixels, 0, width, 0, 0, width, height)


            return mBitmap
        }

        override fun doInBackground(vararg params: Pair<String, List<PrintingDataSlice>>): Int {
            try {
                val sock = Socket(params[0].first, 9100)
                val oStream = sock.getOutputStream()
                oStream.write(DataForSendToPrinterPos80.initializePrinter())
                params[0].second.sortedBy { it.index }.map {
                    val bm1 = convertGreyImg(it.data)
                    val bm2 = resizeImage(bm1, 572, false)
                    oStream.write(DataForSendToPrinterPos80.printRasterBmp(
                            0, bm2, BitmapToByteData.BmpType.Threshold, BitmapToByteData.AlignType.Left, 576))
                }
                oStream.write(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(66, 1))
                oStream.close()
                sock.close()
                return RESULT_SUCCESS
            } catch (e: UnknownHostException) {
                return RESULT_HOST_ERROR
                e.printStackTrace()
            } catch (e: IOException) {
                return RESULT_CONNECT_ERROR
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
                return RESULT_ERROR
            }
        }

        override fun onPostExecute(result: Int) {
            super.onPostExecute(result)
            listener.onPostDone(result)
        }
    }

    var ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469
    fun checkPermission(activity: Activity?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + activity?.packageName))
                activity?.startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
                return false
            } else {
                return true
            }
            return false
        }
        return false
    }
}