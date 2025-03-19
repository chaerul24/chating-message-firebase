package wtc.material;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
public class CountingRequestBody extends RequestBody {
    private final RequestBody delegate;
    private final ProgressListener listener;

    public interface ProgressListener {
        void onProgress(long bytesWritten, long contentLength);
    }

    public CountingRequestBody(RequestBody delegate, ProgressListener listener) {
        this.delegate = delegate;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return delegate.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        BufferedSink countingSink = Okio.buffer(new ForwardingSink(sink) {
            long bytesWritten = 0L;
            long contentLength = contentLength();

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                bytesWritten += byteCount;
                listener.onProgress(bytesWritten, contentLength);
            }
        });

        delegate.writeTo(countingSink);
        countingSink.flush();
    }
}
