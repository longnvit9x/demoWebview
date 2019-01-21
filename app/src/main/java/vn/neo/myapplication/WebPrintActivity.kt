package vn.neo.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
        PrinterUtils.initWebView(this, findViewById(R.id.webview))
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