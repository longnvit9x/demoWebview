package vn.neo.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.LinearLayout
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.observers.DisposableCompletableObserver
import vn.neo.myapplication.print.PrinterUtils
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class WebPrintActivity : Activity() {
    companion object {
        const val IP = "IP"
        const val TEMPLATE = "TEMPLATE"
        const val JSON = "JSON"
        const val RESULT_PRINT = "RESULT_PRINT"
        fun newIntent(context: Context, ip: String?, tempalte: String?, json: String?): Intent {
            return Intent(context, WebPrintActivity::class.java).apply {
                putExtra(IP, ip)
                putExtra(TEMPLATE, tempalte)
                putExtra(JSON, json)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        findViewById<LinearLayout>(R.id.lnl_container).layoutParams = FrameLayout.LayoutParams(576, FrameLayout.LayoutParams.WRAP_CONTENT)
        findViewById<LinearLayout>(R.id.lnl_content).layoutParams = FrameLayout.LayoutParams(576, FrameLayout.LayoutParams.WRAP_CONTENT)
        findViewById<WebView>(R.id.webview).layoutParams = LinearLayout.LayoutParams(576, LinearLayout.LayoutParams.WRAP_CONTENT)
        PrinterUtils.initWebView(this, findViewById(R.id.webview), "file:///android_asset/print-template-tablet/index.html")
        Completable.complete().delay(3, TimeUnit.SECONDS)
                .andThen(PrinterUtils.preLoadHtml("pay-template", "{}"))
                .subscribe {
                    initData()
                }
    }

    val thread = Thread(Runnable {
        try {
            val sock = Socket()
            sock.connect(InetSocketAddress(ip, 9100), 1000)
            PrinterUtils.addHtmlAndPrint(sock.getOutputStream(), template!!, json!!) {
                sock.close()
                this@WebPrintActivity.setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra(RESULT_PRINT, it)
                })
                this.finish()
            }.observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : DisposableCompletableObserver() {
                        override fun onComplete() {

                        }

                        override fun onError(@NonNull e: Throwable) {

                        }
                    })
        } catch (e: UnknownHostException) {
            this@WebPrintActivity.setResult(Activity.RESULT_OK, Intent().apply {
                putExtra(RESULT_PRINT, PrinterUtils.RESULT_HOST_ERROR)
            })
            this.finish()
        } catch (e: IOException) {
            this@WebPrintActivity.setResult(Activity.RESULT_OK, Intent().apply {
                putExtra(RESULT_PRINT, PrinterUtils.RESULT_CONNECT_ERROR)
            })
            this.finish()
        } catch (e: Exception) {
            this@WebPrintActivity.setResult(Activity.RESULT_OK, Intent().apply {
                putExtra(RESULT_PRINT, PrinterUtils.RESULT_ERROR)
            })
            this.finish()
        }
    })

    var ip: String? = null
    var template: String?= null
    var json: String?= null
    private fun initData() {
        val ip = intent.getStringExtra(IP)
        val template = intent.getStringExtra(TEMPLATE)
        val json = intent.getStringExtra(JSON)
        this.ip = ip
        this.template= template
        this.json= json
        thread.start()
    }

}