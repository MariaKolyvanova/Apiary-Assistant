package com.apiarymanager.apiaryassistant.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.apiarymanager.apiaryassistant.App;
import com.apiarymanager.apiaryassistant.OnClickListener;
import com.apiarymanager.apiaryassistant.R;
import com.apiarymanager.apiaryassistant.Report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReportActivity extends AppCompatActivity {
    EditText apiary_name, apiary_address;
    Button toCreateReport;
    ImageButton toBack;
    Button loadReport, newReport, buttonSave;
    ImageView plusOne, leftArrow, rightArrow;
    ConstraintLayout createReport, thisReport, newOrLoad, hasBeenCreated;
    TextView nameMyApiary, textNumberRow;
    App app;
    List<EditText> currentEditTexts = new ArrayList<>();
    List<String> cells = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        OnClickListener.initMenu(this, R.id.one);

        apiary_name = findViewById(R.id.to_apiary_name);
        apiary_address = findViewById(R.id.to_apiary_address);
        toCreateReport = findViewById(R.id.toCreateReport);
        plusOne = findViewById(R.id.plusOne);
        createReport = findViewById(R.id.createReport);
        thisReport = findViewById(R.id.thisReport);
        loadReport = findViewById(R.id.loadReport);
        newReport = findViewById(R.id.newReport);
        newOrLoad = findViewById(R.id.newOrLoad);
        toBack = findViewById(R.id.toBack);
        nameMyApiary = findViewById(R.id.nameMyApiary);
        hasBeenCreated = findViewById(R.id.hasBeenCreated);
        leftArrow = findViewById(R.id.leftArrow);
        rightArrow = findViewById(R.id.rightArrow);
        textNumberRow = findViewById(R.id.textNumberRow);
        buttonSave = findViewById(R.id.buttonSave);

        toCreateReport.setOnClickListener(v -> {
            try {
                createReport();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        loadReport.setOnClickListener(v -> {
            Intent intent = new Intent(this, ArchiveActivity.class);
            startActivity(intent);
        });

        newReport.setOnClickListener(v -> {
            newOrLoad.setVisibility(View.GONE);
            createReport.setVisibility(View.VISIBLE);
        });

        app = (App) getApplication();
        if (app.getActiveReport() != null) {
            try {
                activePattern();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        toBack.setOnClickListener(v -> {
            NewOrLoad();
        });

        leftArrow.setOnClickListener(v -> {
            generateCell(app.getActiveReport().previousRow());
        });

        rightArrow.setOnClickListener(v -> {
            if (isCurrentRowEmpty()) {
                Toast.makeText(this, "Введите данные перед добавлением новой строки", Toast.LENGTH_SHORT).show();
            } else {
                generateCell(app.getActiveReport().nextRow());
            }
        });

        buttonSave.setOnClickListener(v -> {
            saveCurrentRowData();
        });
    }

    private void saveCurrentRowData() {
        if (app.getActiveReport() == null || currentEditTexts.isEmpty()) return;

        for (int j = 0; j < currentEditTexts.size(); j++) {
            String key = cells.get(j);
            String value = currentEditTexts.get(j).getText().toString();
            app.getActiveReport().writeToCellInRow(app.getActiveReport().getRow(), key, value);
        }

        try {
            app.getActiveReport().saveFile();
            Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
        }
    }


    private boolean isCurrentRowEmpty() {
        for (EditText editText : currentEditTexts) {
            if (!editText.getText().toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void createReport() throws IOException {
        boolean reportIsReady = true;
        if (apiary_name.getText().toString().trim().isEmpty()) {
            apiary_name.setBackground(getDrawable(R.drawable.border_round_edit_error));
            apiary_name.postDelayed(() -> apiary_name.setBackground(getDrawable(R.drawable.border_round_edit)), 1000);
            reportIsReady = false;
        }
        if (apiary_address.getText().toString().trim().isEmpty()) {
            apiary_address.setBackground(getDrawable(R.drawable.border_round_edit_error));
            apiary_address.postDelayed(() -> apiary_address.setBackground(getDrawable(R.drawable.border_round_edit)), 1000);
            reportIsReady = false;
        }

        File dir = new File(getFilesDir(), "reports");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals(apiary_name.getText().toString() + ".xlsx")) {
                    hasBeenCreated.setVisibility(View.VISIBLE);
                    hasBeenCreated.postDelayed(() -> hasBeenCreated.setVisibility(View.GONE), 3000);
                    reportIsReady = false;
                }
            }
        }

        if (reportIsReady) {
            Report report = new Report(this, apiary_name.getText().toString(), apiary_address.getText().toString());
            app.setActiveReport(report);

            Toast.makeText(this, "Новый отчёт создан", Toast.LENGTH_SHORT).show();
            plusOne.setVisibility(View.VISIBLE);
            plusOne.postDelayed(() -> plusOne.setVisibility(View.GONE), 5000);

            activePattern();
        }
    }

    private void generateCell(int index) {
        textNumberRow.setText(getString(R.string.number_row) + " " + app.getActiveReport().getRow());

        LinearLayout scrollLayout = findViewById(R.id.scroll);
        scrollLayout.removeAllViews();

        cells = app.getActiveReport().keys;
        List<String> values;

        int size = app.getActiveReport().sizeRow();
        if (size > 1) {
            values = app.getActiveReport().readReport(index);
        } else {
            values = new ArrayList<>(app.getActiveReport().keys.size());
            for (int i = 0; i < app.getActiveReport().keys.size(); i++) {
                values.add("");
            }
        }

        int i = 0;
        currentEditTexts.clear();
        for (String name : cells) {
            ConstraintLayout itemLayout = (ConstraintLayout) LayoutInflater.from(this).inflate(R.layout.cell, scrollLayout, false);

            TextView columnName = itemLayout.findViewById(R.id.column_name);
            EditText toColumnName = itemLayout.findViewById(R.id.to_column_name);

            columnName.setText(name);
            toColumnName.setText(values.get(i));

            currentEditTexts.add(toColumnName);

            scrollLayout.addView(itemLayout);
            i++;
        }

    }

    public void NewOrLoad() {
        newOrLoad.setVisibility(View.VISIBLE);
        createReport.setVisibility(View.GONE);
        thisReport.setVisibility(View.GONE);
        app.setActiveReport(null);
    }

    public void activePattern() throws IOException {
        generateCell(app.getActiveReport().getRow());
        newOrLoad.setVisibility(View.GONE);
        createReport.setVisibility(View.GONE);
        thisReport.setVisibility(View.VISIBLE);
        nameMyApiary.setText(app.getActiveReport().nameReport);
    }
}
