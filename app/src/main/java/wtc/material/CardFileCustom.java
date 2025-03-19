package wtc.material;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.modern.chating.R;
import com.modern.chating.file.FileNameExtractor;

import java.io.File;

public class CardFileCustom extends FrameLayout {
    private ImageView iconFiles;
    private TextView text_file_name;
    private TextView text_file_size;

    public CardFileCustom(Context context) {
        super(context);
        init();
    }

    public CardFileCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CardFileCustom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_card_file, this);
        iconFiles = findViewById(R.id.icon_file);
        text_file_name = findViewById(R.id.text_file_name);
        text_file_size = findViewById(R.id.text_file_size);
    }

    public void fileName(String fileName) {
        text_file_name.setText(fileName);
    }

    public void setFile(File file) {
        try {
            if (file.exists()) {
                setName(file.getAbsolutePath());
                setSize(file);
                formatFile(getFileExtension(file));
            } else {
                text_file_name.setText("File not found");
                text_file_size.setText("-");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf(".");
        return (lastIndex != -1) ? name.substring(lastIndex + 1).toLowerCase() : "";
    }

    public void formatFile(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            iconFiles.setImageResource(R.drawable.ic_file); // Default jika tidak ada ekstensi
            return;
        }

        // Ambil ekstensi dari fileName
        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();

        if (extension.endsWith(".pdf")) {
            iconFiles.setImageResource(R.drawable.pdf);
        } else if (extension.endsWith(".doc") || extension.endsWith(".docx")) {
            iconFiles.setImageResource(R.drawable.ic_docx);
        } else if (extension.endsWith(".xls") || extension.endsWith(".xlsx")) {
            iconFiles.setImageResource(R.drawable.ic_xls);
        } else if (extension.endsWith(".apk")) {
            iconFiles.setImageResource(R.drawable.apk);
        } else if (extension.endsWith(".mp3")) {
            iconFiles.setImageResource(R.drawable.ic_mp3);
        } else {
            iconFiles.setImageResource(R.drawable.ic_file); // Default icon
        }
    }


    public void setSize(File file) {
        if (file.exists()) {
            long fileSize = file.length();
            text_file_size.setText(formatSize(fileSize));
        } else {
            text_file_size.setText("-");
        }
    }

    public void setSize(String size) {
        text_file_size.setText(size);
    }

    private String formatSize(long size) {
        double kb = size / 1024.0;
        double mb = kb / 1024.0;
        double gb = mb / 1024.0;

        if (gb >= 1) return String.format("%.2f GB", gb);
        if (mb >= 1) return String.format("%.2f MB", mb);
        if (kb >= 1) return String.format("%.2f KB", kb);
        return size + " B";
    }

    public void setName(String fileUrl) {
        String fileName = FileNameExtractor.getFileNameFromUrl(fileUrl);
        text_file_name.setText(fileName);
    }
}
