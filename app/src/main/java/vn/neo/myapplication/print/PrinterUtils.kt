package vn.neo.myapplication.print

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.AsyncTask
import android.webkit.WebView
import io.reactivex.Completable
import io.reactivex.Observable
import net.posprinter.utils.BitmapToByteData
import net.posprinter.utils.DataForSendToPrinterPos80
import timber.log.Timber
import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.TimeUnit


object PrinterUtils {
    var socket: Socket? = null

    init {
        socket = Socket()
    }

    const val RESULT_SUCCESS = 1
    const val RESULT_ERROR = 2
    const val RESULT_CONNECT_ERROR = 3
    const val RESULT_HOST_ERROR = 4
    fun addHtmlAndPrint(outputStream: OutputStream, templateId: String, json: String, onCallBack: (result: Int) -> Unit): Completable {
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
                    }).execute(Pair(outputStream, slices))
                }.toCompletable()
                .doOnComplete { Timber.d("Printer.addHtml[" + (Calendar.getInstance().timeInMillis - startTime[0]) + "]ms") }
    }

    private fun generatePrintingSlices(templateId: String, json: String): Observable<PrintingDataSliceCus> {
        return PrintingWebViewCus.getInstance().loadPrintingContent(templateId, json)
                .delay(300, TimeUnit.MILLISECONDS)
                .andThen(PrintingWebViewCus.getInstance().captureWebView())
                .flatMapObservable(PrintingDataGeneratorCus::generatePrintingSlices)
    }

    fun preLoadHtml(templateId: String, json: String): Completable {
        return PrintingWebViewCus.getInstance().loadPrintingContent(templateId, json)
                .andThen(PrintingWebViewCus.getInstance().captureWebView())
                .toCompletable()
    }

    fun initWebView(context: Context, webview: WebView, indexUrl: String) {
        val startTime = Calendar.getInstance().timeInMillis
        PrintingWebViewCus.getInstance().indexUrl = indexUrl
        PrintingWebViewCus.getInstance().create(context, webview)
        Timber.d("Printer.initWebView[" + (Calendar.getInstance().timeInMillis - startTime) + "]ms")
    }


    class ProcessConnectAndPrinter constructor(private val listener: ListenerProcessPrinter) : AsyncTask<Pair<OutputStream, List<PrintingDataSliceCus>>, String, Int>() {
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

        override fun doInBackground(vararg params: Pair<OutputStream, List<PrintingDataSliceCus>>): Int {
            try {
                val oStream = params[0].first
                oStream.write(DataForSendToPrinterPos80.initializePrinter())
                params[0].second.sortedBy { it.index }.map {
                    DataForSendToPrinterPos80.printRasterBmp(
                            0, it.data, BitmapToByteData.BmpType.Threshold, BitmapToByteData.AlignType.Left, 576)
                }.toList().forEach {
                    oStream.write(it)
                }
                oStream.write(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(66, 1))
                oStream.close()
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
}