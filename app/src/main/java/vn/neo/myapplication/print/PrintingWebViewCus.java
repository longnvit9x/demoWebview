package vn.neo.myapplication.print;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;

import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * PrintingWebViewCus
 * Create and load WebView
 * Created by guosx on 2017/5/5.
 */
public class PrintingWebViewCus {

    private static PrintingWebViewCus instance = null;

    private String indexUrl = "file:///android_asset/print-template/index.html";

    private boolean loadingFinished = true;

    private Context context;

    /**
     * Page load delay time, ms.
     */
    private int PAGE_LOAD_DELAY_TIME = 50;

    /**
     * Html content rendered timeout, unit ms.
     */
    private int HTML_CONTENT_LOAD_TIMEOUT = 2000;

    /**
     * Page loaded retry times.
     */
    private int PAGE_LOADED_RETRY_TIMES = 100;

    private CountDownLatch loadingFinishedLatch = new CountDownLatch(1);

    private WebView webView = null;

    public static PrintingWebViewCus getInstance() {
        if (instance == null) {
            return instance = new PrintingWebViewCus();
        } else {
            return instance;
        }
    }

    /**
     * Capture web view.
     *
     * @return
     */
    @Nullable
    public Single<Bitmap> captureWebView() {
        final Long[] startTime = new Long[1];
        Single<Bitmap> capture = Single.fromCallable(() -> {
            startTime[0] = Calendar.getInstance().getTimeInMillis();
            webView.measure(View.MeasureSpec.makeMeasureSpec(
                    View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            webView.layout(0, 0, webView.getMeasuredWidth(),
                    webView.getMeasuredHeight());
            webView.setDrawingCacheEnabled(true);
            webView.buildDrawingCache();
            Bitmap  b = Bitmap.createBitmap( webView.getWidth(),
                    webView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas( b );

            webView.draw( c );
//            float scale = webView.getScale();
//            Bitmap bm = Bitmap.createBitmap(webView.getWidth(), (int) (webView.getContentHeight() * scale + 0.5), Bitmap.Config.ARGB_8888);
//            Canvas cv = new Canvas(bm);
//            webView.draw(cv);
//            int width = webView.getWidth();
//            int height = webView.getContentHeight();
//
//            if (width <= 0 || height <= 10) {
//                throw new PageNotLoadedExceptionCus();
//            }
//
//            // WebView screenshot
//            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//
//            Canvas canvas = new Canvas(bitmap);
//            webView.draw(canvas);

            return b;
        });

        return capture.subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(failures -> failures.zipWith(Flowable.range(1, PAGE_LOADED_RETRY_TIMES), (err, attempt) -> attempt < PAGE_LOADED_RETRY_TIMES ?
                        Flowable.timer(PAGE_LOAD_DELAY_TIME, TimeUnit.MILLISECONDS) :
                        Flowable.error(err)).flatMap(x -> x))
                .flatMap(bitmap -> clearPrintingContent().toSingleDefault(bitmap))
                .doOnSuccess(bitmap -> Timber.d("PrintingWebViewCus.captureWebView[" + (Calendar.getInstance().getTimeInMillis() - startTime[0]) + "]ms"));
    }


    /**
     * Wait until content rendered.
     *
     * @return
     */
    private Completable utilContentRendered() {
        return Completable.create(e -> {
            ViewTreeObserver viewTreeObserver = webView.getViewTreeObserver();

            viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    int height = webView.getMeasuredHeight();
                    if (height > 8) {
                        e.onComplete();
                        webView.getViewTreeObserver().removeOnPreDrawListener(this);
                    }
                    return false;
                }
            });
        });
    }

    /**
     * Load printing template content.
     *
     * @param templateId templateId. d. The value should be exactly same with the id in index.html
     */
    public Completable loadPrintingContent(final String templateId, String json) {
        final Long[] startTime = new Long[1];

        String loadContentScript = "window.loadContent('" + templateId + "', " + json + ")";

        return Single.just(loadContentScript)
                // // await util the page has been loaded
                .doOnSuccess(value -> {
                    startTime[0] = Calendar.getInstance().getTimeInMillis();
                    this.awaitPageLoaded();
                })
                .flatMapMaybe(this::evaluateJavascript)
                .flatMapCompletable((value) -> Completable.complete())
                .doOnComplete(() -> {
                    Timber.d("PrintingWebViewCus.loadPrintingContent[" + (Calendar.getInstance().getTimeInMillis() - startTime[0]) + "]ms");
                });
    }

    /**
     * Evaluate Javascript in webView.
     *
     * @param javascript
     * @return
     */
    Maybe<Object> evaluateJavascript(String javascript) {
        return Maybe.create(e -> {
            // Load template Page
            Long startTime = Calendar.getInstance().getTimeInMillis();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webView.evaluateJavascript(javascript, value -> {
                    if (value == null || "null".equals(value)) {
                        e.onComplete();
                    } else {
                        e.onSuccess(JSON.parse(value));
                    }

                    Timber.d("PrintingWebViewCus.evaluateJavascript[" + (Calendar.getInstance().getTimeInMillis() - startTime) + "]ms");
                });
            }
        }).subscribeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Init a web view for printing.
     * <p>
     * Maybe you can invoke when app starts to save print time.
     */
    public void create(Context context, WebView web) {
        this.context = context;
        this.webView= web;
        //createWebView(context,webView);
//        this.webView.setLayoutParams(new LinearLayout.LayoutParams(576, LinearLayout.LayoutParams.WRAP_CONTENT));
        // init web view
        initWebView();

        // load index page
       webView.loadUrl(indexUrl);
        webView.setDrawingCacheEnabled(true);
    }

    /**
     * Init web view.
     */
    private void initWebView() {
        webView.setInitialScale(100);

        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        webView.getSettings().setAllowContentAccess(true);
        // add


        // TODO process finish
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view, WebResourceRequest request) {

                setLoadingFinished(false);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    webView.loadUrl(request.getUrl().toString());
                }
                return true;
            }

            @Override
            public void onPageStarted(
                    WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                setLoadingFinished(false);
            }















            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:AndroidFunction.resize(document.body.scrollHeight)");
                setLoadingFinished(true);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Timber.d(cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId());
                return true;
            }
        });
        webView.addJavascriptInterface(new WebAppInterface(),"AndroidFunction");
    }

    public class WebAppInterface {

        @JavascriptInterface
        public void resize(final float height) {
            float webViewHeight = (height * context.getResources().getDisplayMetrics().density);
            //webViewHeight is the actual height of the WebView in pixels as per device screen density
        }
    }
    /**
     * Create a new WebView
     *
     * @return WebView
     */
    private void createWebView(Context context, WebView webView) {
//        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//
//        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT);
//        params.gravity = Gravity.TOP | Gravity.LEFT;
//        params.x = 0;
//        params.y = 0;
//        params.width = 576;
//        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

//        LinearLayout linearLayout = new LinearLayout(context);
//        linearLayout.setLayoutParams(new RelativeLayout.LayoutParams(576, RelativeLayout.LayoutParams.WRAP_CONTENT));

        this.webView = webView;
        this.webView.setLayoutParams(new LinearLayout.LayoutParams(576, LinearLayout.LayoutParams.WRAP_CONTENT));
//        this.webView.setVisibility(View.INVISIBLE);
       // linearLayout.addView(this.webView);

        //windowManager.addView(linearLayout, params);
    }

    /**
     * loadBlank
     * WebView should load blank page after load template page finish.
     * Because if no load blank page, the next time load template will get and print the previous page.
     */
    private Completable clearPrintingContent() {
        return evaluateJavascript("window.clearContent();")
                .flatMapCompletable(value -> Completable.complete());
    }

    WebView getWebView() {
        return webView;
    }

    public String getIndexUrl() {
        return indexUrl;
    }

    public void setIndexUrl(String indexUrl) {
        this.indexUrl = indexUrl;
    }

    private void setLoadingFinished(boolean finished) {
        this.loadingFinished = finished;
        if (this.loadingFinished) {
            loadingFinishedLatch.countDown();
        }
    }

    /**
     * Wait the current page loaded.
     *
     * @throws InterruptedException
     */
    public void awaitPageLoaded() throws InterruptedException {
        this.loadingFinishedLatch.await();
    }

    /**
     * Wait current page loaded.
     *
     * @param time
     * @param unit
     * @throws InterruptedException
     */
    public void awaitPageLoaded(int time, TimeUnit unit) throws InterruptedException {
        this.loadingFinishedLatch.await(time, unit);
    }

    public Bitmap screenshot() {
        try {
            float scale = webView.getScale();
            int height = (int) (webView.getContentHeight() * scale + 0.5);
            Bitmap bitmap = Bitmap.createBitmap(webView.getWidth(), height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            webView.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

