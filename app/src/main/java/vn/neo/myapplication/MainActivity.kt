package vn.neo.myapplication

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


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
            this@MainActivity.startActivityForResult(WebPrintActivity.newIntent(this, "192.168.1.7", "pay-template", ticket), REQUEST_CODE_PRINT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_PRINT -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    if (progess?.isShowing == true) {
                        progess?.hide()
                    }
                    val result = data.getIntExtra(WebPrintActivity.RESULT_PRINT, -1)
                    Toast.makeText(this@MainActivity, result.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE_PRINT = 10031
    }
}