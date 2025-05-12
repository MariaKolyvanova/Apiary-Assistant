package com.apiarymanager.apiaryassistant.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.apiarymanager.apiaryassistant.ModelDownloadWorker;
import com.apiarymanager.apiaryassistant.R;

public class DownloadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_download);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        beginDownload();
    }

    private void beginDownload() {
        OneTimeWorkRequest downloadWorker =
                new OneTimeWorkRequest.Builder(ModelDownloadWorker.class)
                        .build();

        WorkManager.getInstance(this).enqueue(downloadWorker);

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(downloadWorker.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        if (workInfo.getState().isFinished()) {
                            // Загрузка завершена
                            Intent intent = new Intent(DownloadActivity.this, SplashActivity.class);
                            startActivity(intent);
                            finish();
                        } else if (workInfo.getProgress() != null) {
                            int progress = workInfo.getProgress().getInt("progress", 0);
                            ProgressBar progressBar = findViewById(R.id.progressBar);
                            progressBar.setProgress(progress);
                        }
                    }
                });
    }
}