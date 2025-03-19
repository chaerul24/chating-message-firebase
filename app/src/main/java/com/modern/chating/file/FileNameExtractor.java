package com.modern.chating.file;

import java.net.URL;

public class FileNameExtractor {
    public static String getFileNameFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            String path = url.getPath();
            return path.substring(path.lastIndexOf("/") + 1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
