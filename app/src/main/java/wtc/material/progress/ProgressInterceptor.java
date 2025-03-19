package wtc.material.progress;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import wtc.material.progress.ProgressResponseBody;

public class ProgressInterceptor implements Interceptor {

    public interface ProgressListener {
        void onProgress(long bytesRead, long totalBytes);
    }

    private static final ConcurrentHashMap<String, ProgressListener> LISTENERS = new ConcurrentHashMap<>();

    public static void addListener(String url, ProgressListener listener) {
        if (url == null || listener == null) {
            Log.e("ProgressInterceptor", "URL atau Listener tidak boleh null");
            return;
        }
        Log.d("ProgressInterceptor", "Menambahkan listener untuk: " + url);
        LISTENERS.put(url, listener);
    }

    public static void removeListener(String url) {
        if (url != null) {
            LISTENERS.remove(url);
            Log.d("ProgressInterceptor", "Menghapus listener untuk: " + url);
        }
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        HttpUrl url = request.url();
        ProgressListener listener = LISTENERS.get(url.toString());

        if (listener == null) {
            return response;
        }

        ResponseBody responseBody = response.body();
        if (responseBody == null || !HttpHeaders.hasBody(response)) {
            return response;
        }

        return response.newBuilder()
                .body(new ProgressResponseBody(responseBody, listener))
                .build();
    }
}
