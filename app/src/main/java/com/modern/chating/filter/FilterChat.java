package com.modern.chating.filter;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Base64;

public class FilterChat {
    private String message;

    public FilterChat(){}

    public FilterChat(String message) {
        this.message = message;
    }



    // 🔐 Enkripsi ke Base64
    public String getEncryptedMessage() {
        return Base64.encodeToString(message.getBytes(), Base64.DEFAULT);
    }

    // 🔓 Dekripsi dari Base64
    public String getDecryptedMessage() {
        try {
            byte[] decodedBytes = Base64.decode(message, Base64.DEFAULT);
            return new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            return "❌ Gagal mendekripsi teks!";
        }
    }
}
