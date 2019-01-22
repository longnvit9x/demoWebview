package vn.neo.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.NestedScrollView
import android.util.DisplayMetrics
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.LinearLayout
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.observers.DisposableCompletableObserver
import vn.neo.myapplication.print.PrinterUtils
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
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val widthPixels = metrics.widthPixels
        val heightPixels = metrics.heightPixels
        val scaleFactor = metrics.density
        val widthDp = widthPixels / scaleFactor
        val heightDp = heightPixels / scaleFactor
        val smallestWidth = Math.min(widthDp, heightDp)
        val widthDpi = metrics.xdpi
        val heightDpi = metrics.ydpi
        val widthInches = widthPixels / widthDpi
        val heightInches = heightPixels / heightDpi
        val diagonalInches = Math.sqrt(
                (widthInches * widthInches + heightInches * heightInches).toDouble())
        val indexUrl = if ((diagonalInches >= 10 && diagonalInches < 11) || (diagonalInches >= 7 && diagonalInches < 8)) {
            "file:///android_asset/print-template-tablet/index.html"
        } else {
            "file:///android_asset/print-template-tablet/index.html"
//            "file:///android_asset/print-template/index.html"
        }
        PrinterUtils.initWebView(this, findViewById(R.id.webview), indexUrl)
        Completable.complete().delay(3, TimeUnit.SECONDS)
                .andThen(PrinterUtils.preLoadHtml("pay-template", "{}"))
                .subscribe {
                    initData()
                }
    }

    private fun initData() {
        val ip = intent.getStringExtra(IP)
        val template = intent.getStringExtra(TEMPLATE)
        val json = intent.getStringExtra(JSON)
        PrinterUtils.addHtmlAndPrint(ip, template, json) {
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
    }

}