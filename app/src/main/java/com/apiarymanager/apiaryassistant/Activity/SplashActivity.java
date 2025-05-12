package com.apiarymanager.apiaryassistant.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;

import com.apiarymanager.apiaryassistant.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Получаем ссылку на TextView
        TextView textView = findViewById(R.id.nameCompany);
        // Устанавливаем начальную прозрачность
        textView.setAlpha(0f);
        // Запускаем анимацию появления текста
        textView.animate().alpha(1f).setDuration(1000);

        if (isModelInstalled()) {
            toActivity(AssistantActivity.class);
        } else {
            toActivity(DownloadActivity.class);
        }
    }

    private boolean isModelInstalled() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return prefs.getBoolean("MODEL_INSTALLED", false);
    }

    public void toActivity(Class classActivity) {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, classActivity));
            finish();
        }, 2000);
    }
}
