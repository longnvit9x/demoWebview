package vn.neo.myapplication

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import net.posprinter.utils.BitmapToByteData
import net.posprinter.utils.DataForSendToPrinterPos80
import java.io.IOException
import java.net.Socket
import java.net.UnknownHostException


class MainActivity : AppCompatActivity() {
    var i = 0
    var progess: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progess = ProgressDialog(this)
        progess?.setTitle("Đang in vui lòng chờ...")
        btnPrint.setOnClickListener {
            progess?.show()
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
            this@MainActivity.startActivityForResult(WebPrintActivity.newIntent(this,"10.252.10.208","pay-template",ticket),REQUEST_CODE_PRINT)
        }
    }

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
                params[0].second.map {
                    val bm1 = convertGreyImg(it)
                    val bm2 = resizeImage(bm1, 572, false)
                    bm2
                }.toList().forEach {
                    oStream.write(DataForSendToPrinterPos80.printRasterBmp(
                            0, it, BitmapToByteData.BmpType.Threshold, BitmapToByteData.AlignType.Left, 576))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_CODE_PRINT->{
                if (resultCode==Activity.RESULT_OK && data!= null){
                    if (progess?.isShowing==true){
                        progess?.hide()
                    }
                    val result= data.getIntExtra(WebPrintActivity.RESULT_PRINT,-1)
                    Toast.makeText(this@MainActivity,result.toString(),Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE_PRINT=10031
    }
}