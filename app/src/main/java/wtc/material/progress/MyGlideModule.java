package wtc.material.progress;
import android.content.Context;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import java.io.InputStream;
import okhttp3.OkHttpClient;

@com.bumptech.glide.annotation.GlideModule
public class MyGlideModule extends AppGlideModule {
    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        builder.setMemoryCache(new LruResourceCache(10 * 1024 * 1024)); // 10MB cache
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull com.bumptech.glide.Registry registry) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new ProgressInterceptor())
                .build();

        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
    }
}
