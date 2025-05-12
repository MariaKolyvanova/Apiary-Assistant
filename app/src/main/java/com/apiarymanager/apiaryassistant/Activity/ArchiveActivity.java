package com.apiarymanager.apiaryassistant.Activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.apiarymanager.apiaryassistant.App;
import com.apiarymanager.apiaryassistant.OnClickListener;
import com.apiarymanager.apiaryassistant.R;
import com.apiarymanager.apiaryassistant.Report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ArchiveActivity extends AppCompatActivity {
    TextView noReports;
    App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_archive);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        noReports = findViewById(R.id.noReports);
        app = (App) getApplication();

        OnClickListener.initMenu(this, R.id.three);

        scrollBar();
    }

    public void scrollBar() {
        LinearLayout reportsContainer = findViewById(R.id.scroll);
        ScrollView scrollView = findViewById(R.id.scrollView);

        File dir = new File(getFilesDir(), "reports");
        File[] files = dir.listFiles();

        if (files != null && files.length > 0) {
            reportsContainer.removeAllViews();

            for (File file : files) {
                if (file.isFile()) {
                    String nameFile = file.getName().replaceAll("\\..*", "");

                    View reportView = getLayoutInflater().inflate(R.layout.item_pattern, reportsContainer, false);

                    TextView tvDate = reportView.findViewById(R.id.textView4);
                    TextView tvId = reportView.findViewById(R.id.textView5);
                    Button btnDelete = reportView.findViewById(R.id.bSearch);
                    Button btnDownload = reportView.findViewById(R.id.bSearch2);
                    Button toReturn = reportView.findViewById(R.id.toReturn);

                    tvDate.setText(nameFile);
                    tvId.setText("");

                    btnDelete.setOnClickListener(v -> {
                        if (app.getActiveReport() != null && file.compareTo(app.getActiveReport().fileReport) == 0) {
                            app.setActiveReport(null);
                        }
                        if (file.delete()) {
                            Toast.makeText(this, "Удалён " + nameFile, Toast.LENGTH_SHORT).show();
                            reportsContainer.removeView(reportView);
                            if (!(dir.listFiles() != null && dir.listFiles().length > 0)) {
                                noReports.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(this, "Не удалось удалить " + nameFile, Toast.LENGTH_SHORT).show();
                        }
                    });

                    btnDownload.setOnClickListener(v -> {
                        downloadsFile(this, nameFile);
                    });

                    toReturn.setOnClickListener(v -> {
                        Report report;
                        try {
                            report = new Report(file);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        app.setActiveReport(report);
                        Intent intent = new Intent(this, AssistantActivity.class);
                        startActivity(intent);
                    });

                    reportsContainer.addView(reportView);
                }
            }

            noReports.setVisibility(View.GONE);
        } else {
            noReports.setVisibility(View.VISIBLE);
        }
    }

    public static void downloadsFile(Context context, String name) {
        File sourceFile = new File(context.getFilesDir(), "reports/" + name + ".xlsx");

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    try (OutputStream out = context.getContentResolver().openOutputStream(uri); InputStream in = new FileInputStream(sourceFile)) {
                        copyFile(in, out);
                    }
                } else {
                    throw new IOException("Не удалось создать URI для загрузки");
                }
            } else {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File outFile = new File(downloadsDir, name);

                try (InputStream in = new FileInputStream(sourceFile); OutputStream out = new FileOutputStream(outFile)) {
                    copyFile(in, out);
                }
            }

            Toast.makeText(context, "Файл сохранён в Загрузки", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Ошибка при сохранении: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
    }
}
