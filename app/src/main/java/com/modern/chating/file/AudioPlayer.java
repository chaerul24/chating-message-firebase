package com.modern.chating.file;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.SeekBar;

import java.io.IOException;

public class AudioPlayer {
    private String file;
    private int raw;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Runnable updateSeekBar;
    private Context context;
    private boolean isLooping = false;

    // Constructor untuk file dari storage
    public AudioPlayer(Context context, String file) {
        this.context = context;
        this.file = file;
        this.mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); // Pastikan pakai STREAM_MUSIC
            mediaPlayer.setDataSource(file);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Constructor untuk file dari raw resource
    public AudioPlayer(Context context, int raw) {
        this.context = context;
        this.raw = raw;
        this.mediaPlayer = MediaPlayer.create(context, raw);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); // Pastikan pakai STREAM_MUSIC
    }

    // Maksimalkan volume sistem
    public void maximizeSystemVolume() {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0);
    }

    // Ambil durasi audio dalam format MM:SS
    public String getDuration() {
        if (mediaPlayer != null) {
            int duration = mediaPlayer.getDuration() / 1000;
            int minutes = duration / 60;
            int seconds = duration % 60;
            return String.format("%02d:%02d", minutes, seconds);
        }
        return "00:00";
    }

    // Atur seekBar untuk mengikuti posisi audio
    public void seekBar(SeekBar seekBar) {
        if (mediaPlayer == null) return;

        seekBar.setMax(mediaPlayer.getDuration());

        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer.isPlaying()) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    handler.postDelayed(this, 100);
                }
            }
        };

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        mediaPlayer.setOnCompletionListener(mp -> handler.removeCallbacks(updateSeekBar));
    }

    // Fungsi untuk memutar audio
    public void play() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            maximizeSystemVolume(); // Pastikan volume maksimal sebelum play
            mediaPlayer.start();
            handler.post(updateSeekBar);
        }
    }

    // Pause audio
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            handler.removeCallbacks(updateSeekBar);
        }
    }

    // Stop audio
    public void stop() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null; // Hindari memory leak
        }
        handler.removeCallbacks(updateSeekBar);
    }

    // Atur volume media player
    public void setVolume(int volume) {
        if (mediaPlayer != null) {
            float vol = Math.max(0.0f, Math.min(volume / 100f, 1.0f)); // Pastikan dalam range 0.0 - 1.0
            mediaPlayer.setVolume(vol, vol);
        }
    }

    public void setLooping(boolean looping) {
        this.isLooping = looping;
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(looping);
        }
    }
}
