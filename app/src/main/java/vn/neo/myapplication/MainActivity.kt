package vn.neo.myapplication

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.observers.DisposableCompletableObserver
import kotlinx.android.synthetic.main.activity_main.*
import net.posprinter.utils.BitmapToByteData
import net.posprinter.utils.DataForSendToPrinterPos80
import timber.log.Timber
import java.io.IOException
import java.net.Socket
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    var i = 0
    var progess: ProgressDialog? = null
    lateinit var printingWebView: PrintingWebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        printingWebView = PrintingWebView.getInstance()
        initWebView(this)
        Completable.complete().delay(3, TimeUnit.SECONDS)
                .andThen(preLoadHtml("pay-template", "{}"))
                .subscribe()
        progess = ProgressDialog(this)
        progess?.setTitle("Đang in vui lòng chờ...")
        btnPrint.setOnClickListener {
            progess?.show()
            // val bm = BitmapFactory.decodeResource(resources, R.drawable.icon_splash)
            val ticket = "{\n" +
                    "\t\t\t\"TICKET\":\" PHIẾU \",\n" +
                    "\t\t\t\"TICKET_ID\": \"Số hóa đơn\",\n" +
                    "\t\t    \"TICKET_NAME\": \"Tên hóa đơn\",\n" +
                    "\t\t    \"POS_NAME\": \"POS\",\n" +
                    "\t\t    \"EMPLOYEE\": \"Nhân viên\",\n" +
                    "\t\t    \"TICKET_NOTE\": \"Chú thích\",\n" +
                    "\t\t    \"CREATE_DATE\": \"Ngày\",\n" +
                    "\t\t    \"PAT_DATE\": \"Ngày thanh toán\",\n" +
                    "\t\t    \"TOTAL_PRODUCT_PRICE\": \"Tiền hàng\",\n" +
                    "\t\t    \"TOTAL_DISCOUNT\": \"Khuyến mại\",\n" +
                    "\t\t    \"TAX_AMOUNT\": \"Thuế\",\n" +
                    "\t\t    \"TOTAL_AMOUNT\": \"Tổng thanh toán\",\n" +
                    "\t\t    \"MONEY\": \"Khách trả\",\n" +
                    "\t\t    \"MONEY_BACK\": \"Trả lại\",\n" +
                    "\t\t    \"CUSTOMER_NAME\": \"Khách hàng\",\n" +
                    "\t\t    \"CUSTOMER_EMAIL\": \"Email\",\n" +
                    "\t\t    \"image\":\"http://dzodzo.com.vn/Upload/item_image/camep.jpg\",\n" +
                    "\t\t    \"header\":\"Công ty Cổ phần Đầu tư Phát triển Công nghệ và Truyền thông - NEO Tòa CT2, Tràng An Complex, Số 1 Phùng Chí Kiên, Nghĩa Đô, Cầu Giấy, Hà nội\",\n" +
                    "\t\t    \"footer\":\"Chúc bạn một ngày tốt lành\",\n" +
                    "\t\t    \"ticket_id\": \"123\",\n" +
                    "\t\t    \"ticket_name\": \"TICKET-15:40\",\n" +
                    "\t\t    \"employee\":\"Longnv\",\n" +
                    "\t\t    \"pos_name\":\"Phùng Chí Kiên- Hà Nội\",\n" +
                    "\t\t    \"ticket_note\": \"Hãy giữ lại hóa đơn\",\n" +
                    "\t\t    \"create_date\": \"11/1/2018 11:30:32\",\n" +
                    "\t\t    \"pay_date\": \"11/1/2018 11:30:32\",\n" +
                    "\t\t    \"list_product_ticket\": [\n" +
                    "\t\t        {\n" +
                    "\t\t            \"product_name\": \"Bánh mì\",\n" +
                    "\t\t            \"product_price\": \"${i}\",\n" +
                    "\t\t            \"number\": \"10\",\n" +
                    "\t\t            \"total_price\": \"100,000\"\n" +
                    "\t\t        },\n" +
                    "\t\t        {\n" +
                    "\t\t            \"product_name\": \"Bánh mì\",\n" +
                    "\t\t            \"product_price\": \"10,000\",\n" +
                    "\t\t            \"number\": \"10\",\n" +
                    "\t\t            \"total_price\": \"100,000\"\n" +
                    "\t\t        },\n" +
                    "\t\t        {\n" +
                    "\t\t            \"product_name\": \"Bánh mì\",\n" +
                    "\t\t            \"product_price\": \"10,000\",\n" +
                    "\t\t            \"number\": \"10\",\n" +
                    "\t\t            \"total_price\": \"100,000\"\n" +
                    "\t\t        },\n" +
                    "\t\t        {\n" +
                    "\t\t            \"product_name\": \"Bánh mì\",\n" +
                    "\t\t            \"product_price\": \"10,000\",\n" +
                    "\t\t            \"number\": \"10\",\n" +
                    "\t\t            \"total_price\": \"100,000\"\n" +
                    "\t\t        },\n" +
                    "\t\t        {\n" +
                    "\t\t            \"product_name\": \"Bánh mì\",\n" +
                    "\t\t            \"product_price\": \"10,000\",\n" +
                    "\t\t            \"number\": \"10\",\n" +
                    "\t\t            \"total_price\": \"100,000\"\n" +
                    "\t\t        },\n" +
                    "\t\t        {\n" +
                    "\t\t            \"product_name\": \"Bánh mì\",\n" +
                    "\t\t            \"product_price\": \"10,000\",\n" +
                    "\t\t            \"number\": \"10\",\n" +
                    "\t\t            \"total_price\": \"100,000\"\n" +
                    "\t\t        },\n" +
                    "\t\t        {\n" +
                    "\t\t             \"product_name\": \"Bánh trứng\",\n" +
                    "\t\t            \"product_price\": \"10,000\",\n" +
                    "\t\t            \"number\": 10,\n" +
                    "\t\t            \"total_price\": \"100,000\"\n" +
                    "\t\t        }\n" +
                    "\t\t    ],\n" +
                    "\t\t    \"total_product_price\":\"200,000\",\n" +
                    "\t\t     \"total_discount\":\"10,000\",\n" +
                    "\t\t     \"tax_amount\":\"5,000\",\n" +
                    "\t\t     \"total_amount\":\"195,000\",\n" +
                    "\t\t     \"money\": \"200,000\",\n" +
                    "\t\t     \"money_back\": \"5,000\",\n" +
                    "\t\t     \"is_show_customer\": true,\n" +
                    "\t\t     \"customer_name\":\"Nguyễn Văn Long\",\n" +
                    "\t\t     \"customer_email\":\"longnv@neo.vn\"\n" +
                    "\t\t    }"
            i++
            addHtml("pay-template", ticket, progess)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : DisposableCompletableObserver() {
                        override fun onComplete() {

                        }

                        override fun onError(@NonNull e: Throwable) {

                        }
                    })
//            if (loadingFinished) {
            //webView.loadUrl("javascript:loadContent('pay-template', $ticket)")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    webView.evaluateJavascript(" loadContent('pay-template', $ticket)") {
//                        webView.measure(View.MeasureSpec.makeMeasureSpec(
//                                View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
//                                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
//                        webView.layout(0, 0, webView.measuredWidth,
//                                webView.measuredHeight)
//                        val bm = Bitmap.createBitmap(webView.width,
//                                webView.height, Bitmap.Config.ARGB_8888)
//                        val bigCanvas = Canvas(bm)
//                        val paint = Paint()
//                        val iHeight = bm.height
//                        bigCanvas.drawBitmap(bm, 0f, iHeight.toFloat(), paint)
//                        webView.draw(bigCanvas)
                //   }
            }
        }
//            Handler().postDelayed({
//                webView.measure(View.MeasureSpec.makeMeasureSpec(
//                        View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
//                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
//                webView.layout(0, 0, webView.measuredWidth,
//                        webView.measuredHeight)
//                val bm = Bitmap.createBitmap(webView.width,
//                        webView.height, Bitmap.Config.ARGB_8888)
//                val bigCanvas = Canvas(bm)
//                val paint = Paint()
//                val iHeight = bm.height
//                bigCanvas.drawBitmap(bm, 0f, iHeight.toFloat(), paint)
//                webView.draw(bigCanvas)
//                ProcessConnectAndPrinter(object : ProcessConnectAndPrinter.ListenerProcessPrinter {
//                    override fun onPostDone(result: Int) {
//                        if (progess?.isShowing == true) {
//                            progess?.hide()
//                        }
//                        if (result == 1) {
//                            Toast.makeText(this@MainActivity, "In thành công", Toast.LENGTH_LONG).show()
//                        } else {
//                            Toast.makeText(this@MainActivity, "In thất bại", Toast.LENGTH_LONG).show()
//                        }
//                    }
//                }).execute(Pair("10.252.10.208", bm))
//            }, 2000)
    }


    fun addHtml(templateId: String, json: String, progress: ProgressDialog?): Completable {
        val startTime = LongArray(1)
        return Completable.complete()
                .doOnComplete { startTime[0] = Calendar.getInstance().timeInMillis }
                .andThen(generatePrintingSlices(templateId, json))
                .toList()
                .doOnSuccess { slices ->
                    ProcessConnectAndPrinter(object : ProcessConnectAndPrinter.ListenerProcessPrinter {
                        override fun onPostDone(result: Int) {
                            if (progress?.isShowing == true) {
                                progress.hide()
                            }
//                            if (result == 1) {
//                                Toast.makeText(this@MainActivity, "In thành công", Toast.LENGTH_LONG).show()
//                            } else {
//                                Toast.makeText(this@MainActivity, "In thất bại", Toast.LENGTH_LONG).show()
//                            }
                        }
                    }).execute(Pair("10.252.10.208", slices))
                    //                    addImages(images)
                    //                    feedLine(6)
                }.toCompletable()
                .doOnComplete { Timber.d("Printer.addHtml[" + (Calendar.getInstance().timeInMillis - startTime[0]) + "]ms") }
    }

    private fun generatePrintingSlices(templateId: String, json: String): Observable<Bitmap> {
        return printingWebView.loadPrintingContent(templateId, json)
                .andThen(printingWebView.captureWebView())
                .flatMapObservable {
                    Observable.just(it)
                }
    }

    fun preLoadHtml(templateId: String, json: String): Completable {
        return PrintingWebView.getInstance().loadPrintingContent(templateId, json)
                .andThen(PrintingWebView.getInstance().captureWebView())
                .toCompletable()
    }

    fun initWebView(context: Context) {
        val startTime = Calendar.getInstance().timeInMillis

        PrintingWebView.getInstance().create(context)

        Timber.d("Printer.initWebView[" + (Calendar.getInstance().timeInMillis - startTime) + "]ms")
    }

//    lateinit var webView: WebView
//    var loadingFinished = true
//    var redirect = false
//    private fun getWebView(context: Context): WebView {
//        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//
//        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
//                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
//                else
//                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                PixelFormat.TRANSLUCENT)
//        params.gravity = Gravity.TOP or Gravity.LEFT
//        params.x = 0
//        params.y = 0
//        params.width = WindowManager.LayoutParams.MATCH_PARENT
//        params.height = WindowManager.LayoutParams.WRAP_CONTENT
//
//        val linearLayout = LinearLayout(context)
//        linearLayout.layoutParams = RelativeLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
//
//        val webView = WebView(context)
//        webView.layoutParams = LinearLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//        webView.visibility = View.INVISIBLE
//        linearLayout.addView(webView)
//        windowManager.addView(linearLayout, params)
//        webView.settings.javaScriptEnabled = true
//        webView.isDrawingCacheEnabled = true
//        webView.webViewClient = object : WebViewClient() {
//            override fun shouldOverrideUrlLoading(view: WebView?, urlNewString: String?): Boolean {
//                if (!loadingFinished) {
//                    redirect = true
//                }
//                view?.loadUrl(urlNewString)
//                loadingFinished = false
//                return true
//            }
//
//            override fun onPageStarted(view: WebView?, url: String?, facIcon: Bitmap?) {
//                loadingFinished = false
//                //SHOW LOADING IF IT ISNT ALREADY VISIBLE
//            }
//
//            override fun onPageFinished(view: WebView?, url: String?) {
//                if (!redirect) {
//                    loadingFinished = true
//                }
//
//                if (loadingFinished && !redirect) {
//                    //HIDE LOADING IT HAS FINISHED
//                    Toast.makeText(this@MainActivity, "load url done", Toast.LENGTH_LONG).show()
//                } else {
//                    redirect = false
//                }
//
//            }
//
//            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
//                super.onReceivedError(view, request, error)
//                Toast.makeText(this@MainActivity, "load error", Toast.LENGTH_LONG).show()
//            }
//        }
//        if (1 == 1) {
//            webView.loadUrl("file:///android_asset/print-template-tablet/index.html")
//        } else {
//            webView.loadUrl("file:///android_asset/print-template/index.html")
//        }
//        return webView
//    }

    class ProcessConnectAndPrinter constructor(private val listener: ListenerProcessPrinter) : AsyncTask<Pair<String, List<Bitmap>>, String, Int>() {
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

        override fun doInBackground(vararg params: Pair<String, List<Bitmap>>): Int {
            try {
                val sock = Socket(params[0].first, 9100)
                val oStream = sock.getOutputStream()
                oStream.write(DataForSendToPrinterPos80.initializePrinter())
//                oStream.write(DataForSendToPrinterPos80.printRasterBmp(
//                        0, bm2, BitmapToByteData.BmpType.Threshold, BitmapToByteData.AlignType.Left, 576))
                params[0].second.map {
                    val bm1 = convertGreyImg(it)
                    val bm2 = resizeImage(bm1, 572, false)
                    oStream.write(DataForSendToPrinterPos80.printRasterBmp(
                            0, bm2, BitmapToByteData.BmpType.Threshold, BitmapToByteData.AlignType.Left, 576))
                }
                oStream.write(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(66, 1))
                oStream.close()
                sock.close()
                return 1
            } catch (e: UnknownHostException) {
                return 0
                e.printStackTrace()
            } catch (e: IOException) {
                return 0
                e.printStackTrace()
            }
            return 0
        }

        override fun onPostExecute(result: Int) {
            super.onPostExecute(result)
            listener.onPostDone(result)
        }
    }
}