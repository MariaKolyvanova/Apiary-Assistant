package com.apiarymanager.apiaryassistant;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Assistant {
    private static final String TAG = "VoskManager";
    private SpeechService speechService;
    private Model model;
    private boolean isListening = false;
    public static List<String> text = new ArrayList<>();
    Report report;
    String value = "";
    private boolean recordingStarted = false;
    String keyword;

    public Assistant(Report report) {
        this.report = report;
    }

    public interface OnModelReadyCallback {
        void onModelReady();
    }

    public void initModel(Context context, OnModelReadyCallback callback) {
        if (model != null) {
            callback.onModelReady();
            return;
        }

        File modelDir = new File(context.getFilesDir(), "model/vosk-model-small-ru-0.22");

        new Thread(() -> {
            try {
                model = new Model(modelDir.getAbsolutePath());
                Log.d(TAG, "Модель успешно загружена");

            } catch (IOException e) {
                Log.e(TAG, "Ошибка загрузки модели", e);
            }
        }).start();
    }

    public void toggleRecognition(TextView resultView) {
        if (model == null || !isModelLoaded()) {
            Log.e(TAG, "Модель ещё не загружена");
            return;
        }

        if (!isListening) {
            startRecognition(resultView);
        } else {
            stopRecognition();
        }
    }

    private boolean isModelLoaded() {
        return model != null;
    }

    private void startRecognition(TextView resultView) {
        try {
            Recognizer recognizer = new Recognizer(model, 16000.0f);
            speechService = new SpeechService(recognizer, 16000.0f);
            speechService.startListening(new RecognitionListener() {
                @Override
                public void onPartialResult(String hypothesis) {
                }

                @Override
                public void onResult(String hypothesis) {
                    rec(hypothesis.toString());
                    resultView.setText(hypothesis.toString());

                }

                @Override
                public void onFinalResult(String hypothesis) {
                    Log.d(TAG, "Final: " + hypothesis);
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Ошибка распознавания", e);
                }

                @Override
                public void onTimeout() {
                    Log.d(TAG, "Распознавание завершено по таймауту");
                }
            });

            isListening = true;
            Log.d(TAG, "Распознавание запущено");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка запуска распознавания", e);
        }
    }

    public void stopRecognition() {
        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
            speechService = null;
            isListening = false;
            Log.d(TAG, "Распознавание остановлено");
        }
    }

    public void rec(String hypothesis) {
        System.out.println(">>> Входящее сообщение: " + hypothesis);

        if (hypothesis.contains(report.newRow)) {
            String[] parts = hypothesis.split(report.newRow, 2);

            if (recordingStarted) {
                if (!value.trim().isEmpty()) {
                    saveBufferedValue();
                }
            }

            report.newRow();
            System.out.println("➡️ Переход на новую строку");

            if (parts.length > 1) {
                rec(parts[1].trim());
            }
            return;
        }

        if (hypothesis.equalsIgnoreCase(report.stopping)) {
            if (recordingStarted) {
                saveBufferedValue();
            }
            return;
        }

        if (!recordingStarted) {
            keyword = report.checkKeyword(hypothesis);
            if (keyword != null) {
                System.out.println("✅ Найдено ключевое слово: " + keyword);
                recordingStarted = true;

                String remainingText = hypothesis.substring(keyword.length()).trim();

                if (remainingText.contains(report.stopping)) {
                    String[] parts = remainingText.split(report.stopping, 2);
                    String valuePart = parts[0].trim();
                    if (!valuePart.isEmpty()) {
                        value = valuePart;
                        saveBufferedValue();
                    } else {
                        System.out.println("❗ Стоп без значения после ключа");
                        recordingStarted = false;
                    }

                    if (parts.length > 1) {
                        rec(parts[1].trim());
                    }
                } else {
                    if (!remainingText.isEmpty()) {
                        value = remainingText;
                        System.out.println("Начинаем запись: " + value);
                    } else {
                        value = "";
                        System.out.println("Ожидание ввода значения...");
                    }
                }
            } else {
                System.out.println("❌ Не найдено ключевое слово");
            }
        } else {
            if (hypothesis.contains(report.stopping)) {
                String[] parts = hypothesis.split(report.stopping, 2);
                String valuePart = parts[0].trim();
                if (!valuePart.isEmpty()) {
                    value += " " + valuePart;
                }
                saveBufferedValue();

                if (parts.length > 1) {
                    rec(parts[1].trim());
                }
            } else {
                value += " " + hypothesis.trim();
                System.out.println("Продолжаем запись: " + value);
            }
        }
    }

    private void saveBufferedValue() {
        if (!value.trim().isEmpty()) {
            try {
                report.writeToReport(keyword, value.trim());
                System.out.println("➡️ Данные записаны: [" + keyword + "] => [" + value.trim() + "]");
            } catch (IOException e) {
                System.err.println("Ошибка записи данных: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ Буфер пустой, ничего не записано");
        }
        recordingStarted = false;
        value = "";
    }


}
