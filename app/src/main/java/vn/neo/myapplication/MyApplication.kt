package vn.neo.myapplication

import android.app.Application
import android.content.Context
import android.support.v7.app.AppCompatDelegate
import vn.neo.myapplication.print.PrinterUtils


class MyApplication : Application() {
    companion object {
        private val TAG = "ArkeSdkDemoApplication"
        private val SDK_ACTION = "com.usdk.apiservice"
    }


    override fun onCreate() {
        super.onCreate()
       // AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        // load webView before print template
    }

    fun getContext(): Context {
        return applicationContext
    }
}