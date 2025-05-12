package com.apiarymanager.apiaryassistant.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.apiarymanager.apiaryassistant.App;
import com.apiarymanager.apiaryassistant.Assistant;
import com.apiarymanager.apiaryassistant.ModelDownloadWorker;
import com.apiarymanager.apiaryassistant.OnClickListener;
import com.apiarymanager.apiaryassistant.R;

import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.Arrays;

public class AssistantActivity extends AppCompatActivity {

    private Assistant assistant;
    ImageButton record;
    Button toEnd;
    TextView status, hint;
    TextView result, nameMyApiary;
    private boolean isPaused;
    String resultText;
    App app;
    ConstraintLayout noReport;
    Boolean permission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_assistant);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        record = findViewById(R.id.record);
        status = findViewById(R.id.status);
        toEnd = findViewById(R.id.to_end);
        hint = findViewById(R.id.hint);
        result = findViewById(R.id.result);
        noReport = findViewById(R.id.noReport);
        app = (App) getApplication();
        nameMyApiary = findViewById(R.id.nameMyApiary);

        status.setText(getString(R.string.permission));
        status.setTextColor(getColor(R.color.red));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            permission = false;

        } else {
            status.setText(getString(R.string.to_start));
            status.setTextColor(getColor(R.color.brown));
            Toast.makeText(this, "Все хорошо голос", Toast.LENGTH_SHORT).show();
            permission = true;
        }

        if (!permission) {
            record.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            });
        } else if (app.getActiveReport() == null) {
            record.setOnClickListener(v -> {
                noReport();
            });
        } else {
            nameMyApiary.setText(app.getActiveReport().nameReport);
        }

        File modelDir = new File(getFilesDir(), "model/" + ModelDownloadWorker.nameModel);
        Log.d("AssistantActivity", "Модель существует? " + modelDir.exists());
        Log.d("AssistantActivity", "Содержимое: " + Arrays.toString(modelDir.list()));

        assistant = new Assistant(app.getActiveReport());
        assistant.initModel(this, new Assistant.OnModelReadyCallback() {
            @Override
            public void onModelReady() {
                record.setEnabled(true);
            }
        });

        OnClickListener.initMenu(this, R.id.two);

        if (app.getActiveReport() != null) {
            record.setOnClickListener(v -> {
                toRecord();
            });
        }

        findViewById(R.id.to_end).setOnClickListener(v -> {
            toEnd();
        });
    }

    public void noReport() {
        noReport.setVisibility(View.VISIBLE);
        noReport.postDelayed(() -> noReport.setVisibility(View.GONE), 3000);
    }

    private void toRecord() {
        String currentText = status.getText().toString();
        if (currentText.equals(getString(R.string.to_start))) {
            assistant.toggleRecognition(result);
            isPaused = false;
            updateState("started");
        } else if (currentText.equals(getString(R.string.started))) {
            assistant.stopRecognition();
            isPaused = true;
            updateState("pause");
        } else if (currentText.equals(getString(R.string.pause))) {
            assistant.toggleRecognition(result);
            updateState("started");
        } else if (currentText.equals(getString(R.string.completed))) {
            updateState("to_start");
        }
    }

    private void updateState(String state) {
        switch (state) {
            case "started":
                status.setText(getString(R.string.started));
                record.setImageResource(R.drawable.start);
                toEnd.setVisibility(View.VISIBLE);
                hint.setVisibility(View.VISIBLE);
                hint.setText(getString(R.string.to_pause));
                break;

            case "pause":
                status.setText(getString(R.string.pause));
                record.setImageResource(R.drawable.pause);
                hint.setText(getString(R.string.resume));
                break;

            case "completed":
                status.setText(getString(R.string.completed));
                record.setImageResource(R.drawable.complete);
                toEnd.setVisibility(View.GONE);
                hint.setVisibility(View.GONE);

                break;

            case "to_start":
                status.setText(getString(R.string.to_start));
                record.setImageResource(R.drawable.record);
                break;
        }
    }

    private void toEnd() {
        assistant.stopRecognition();
        isPaused = false;
        updateState("completed");
        record.postDelayed(() -> updateState("to_start"), 3000);
        resultText = TextUtils.join("\n", Assistant.text);
        Assistant.text.clear();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение на запись получено", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Разрешение на запись не предоставлено", Toast.LENGTH_SHORT).show();
            }
        }
    }
}