package com.apiarymanager.apiaryassistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.*;
import java.util.Enumeration;
import java.util.UUID;

public class ModelDownloadWorker extends Worker {
    private static final String DOWNLOAD_URL = "https://alphacephei.com/vosk/models/vosk-model-small-ru-0.22.zip";
    public static final String nameModel = "vosk-model-small-ru-0.22";
    private static final String ZIP_FILE_NAME = "vosk-model-small-ru-0.22.zip";

    private static final String OUTPUT_DIRECTORY = "model/";

    public ModelDownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d("ModelDownload", "⬇️ Скачивание файла...");
            downloadFile();
            Log.d("ModelDownload", "✅ Скачивание завершено");

            Log.d("ModelDownload", "📦 Распаковка архива...");
            unpackZip();
            Log.d("ModelDownload", "✅ Распаковка завершена");

            Log.d("ModelDownload", "🧹 Удаление ZIP...");
            deleteOriginalZip();
            Log.d("ModelDownload", "✅ ZIP удалён");

            Log.d("ModelDownload", "🆔 Генерация UUID...");
            generateUUIDFile();
            Log.d("ModelDownload", "✅ UUID создан");

            Log.d("ModelDownload", "📍 Установка флага MODEL_INSTALLED");
            saveModelInstalledFlag(true);

            Log.d("ModelDownload", "✅ doWork завершён");
            return Result.success();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ModelDownload", "❌ Ошибка в doWork", e);
            return Result.failure();
        }
    }

    private void downloadFile() throws IOException {
        Log.d("ModelDownload", "➡️ Запуск downloadFile");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(DOWNLOAD_URL).build();
        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) throw new IOException("Ошибка загрузки");

        Log.d("ModelDownload", "📥 Ответ получен, размер: " + response.body().contentLength());

        long totalBytes = response.body().contentLength();
        InputStream inputStream = response.body().byteStream();
        File zipFile = new File(getApplicationContext().getFilesDir(), ZIP_FILE_NAME);
        FileOutputStream outputStream = new FileOutputStream(zipFile);

        Log.d("ModelDownload", "📝 Сохраняем в файл: " + zipFile.getAbsolutePath());

        byte[] buffer = new byte[4 * 1024];
        long downloaded = 0;
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            downloaded += bytesRead;

            int progress = (int) ((downloaded * 100) / totalBytes);
            setProgressAsync(new Data.Builder().putInt("progress", progress).build());
        }

        outputStream.close();
        inputStream.close();
        Log.d("ModelDownload", "✅ Загрузка файла завершена");

    }

    private void unpackZip() throws Exception {
        Log.d("ModelDownload", "➡️ Начало распаковки");

        File zipFile = new File(getApplicationContext().getFilesDir(), ZIP_FILE_NAME);
        File destDirectory = new File(getApplicationContext().getFilesDir(), OUTPUT_DIRECTORY);

        Log.d("ModelDownload", "📂 Распаковка в: " + destDirectory.getAbsolutePath());

        ZipFile zip = new ZipFile(zipFile);
        Enumeration<?> entries = zip.getEntries();
        int count = 0;
        while (entries.hasMoreElements()) {
            ZipArchiveEntry entry = (ZipArchiveEntry) entries.nextElement();
            File entryDestination = new File(destDirectory, entry.getName());

            if (entry.isDirectory()) {
                entryDestination.mkdirs();
            } else {
                copyInputStreamToFile(zip.getInputStream(entry), entryDestination);
            }
            count++;
        }

        Log.d("ModelDownload", "✅ Распаковано файлов: " + count);
    }

    private void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        byte[] buf = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        inputStream.close();
    }

    private void deleteOriginalZip() {
        File zipFile = new File(getApplicationContext().getFilesDir(), ZIP_FILE_NAME);
        if (zipFile.exists()) {
            zipFile.delete();
        }
    }

    private void saveModelInstalledFlag(boolean installed) {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        preferences.edit().putBoolean("MODEL_INSTALLED", installed).apply();
    }

    public void generateUUIDFile() {
        try {
            File uuidFile = new File(getApplicationContext().getFilesDir() + "/model/" + nameModel, "uuid");

            FileWriter writer = new FileWriter(uuidFile);
            writer.write(UUID.randomUUID().toString());
            writer.close();
            Log.i("UUID", "✅ UUID создан в: " + uuidFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e("UUID", "❌ Ошибка создания UUID: ", e);
        }
    }

}