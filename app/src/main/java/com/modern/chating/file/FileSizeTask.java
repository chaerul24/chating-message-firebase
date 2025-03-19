package com.modern.chating.file;

import android.os.AsyncTask;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileSizeTask extends AsyncTask<String, Void, Long> {
    private FileSizeListener listener;

    public interface FileSizeListener {
        void onSizeFetched(long size);
    }

    public FileSizeTask(FileSizeListener listener) {
        this.listener = listener;
    }

    @Override
    protected Long doInBackground(String... urls) {
        try {
            URL url = new URL(urls[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.connect();
            long fileSize = conn.getContentLengthLong();
            conn.disconnect();
            return fileSize;
        } catch (Exception e) {
            e.printStackTrace();
            return -1L;
        }
    }

    @Override
    protected void onPostExecute(Long size) {
        if (listener != null) {
            listener.onSizeFetched(size);
        }
    }

    public static class FileSizeConverter {
        public String formatFileSize(long sizeInBytes) {
            if (sizeInBytes < 1024) {
                return sizeInBytes + " B";
            } else if (sizeInBytes < 1024 * 1024) {
                return String.format("%.2f KB", sizeInBytes / 1024.0);
            } else if (sizeInBytes < 1024 * 1024 * 1024) {
                return String.format("%.2f MB", sizeInBytes / (1024.0 * 1024.0));
            } else {
                return String.format("%.2f GB", sizeInBytes / (1024.0 * 1024.0 * 1024.0));
            }
        }

        public void main(String[] args) {
            long fileSize = 1774262; // Contoh ukuran file dalam byte
            System.out.println("Ukuran file: " + formatFileSize(fileSize));
        }
    }

}
