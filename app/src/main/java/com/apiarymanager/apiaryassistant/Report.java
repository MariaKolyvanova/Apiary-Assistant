package com.apiarymanager.apiaryassistant;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Report {
    public String nameReport;
    String address;
    public File fileReport;
    public String nameFileReport;
    Context context;
    Map<String, Point> fields = new LinkedHashMap<>();
    String stopping = "стоп";
    XSSFWorkbook workbook;
    XSSFSheet sheet;
    public List<String> keys;
    public String newRow = "Следующая строка";
    private int currentRow = 1;

    public Report(Context context, String nameReport, String address) throws IOException {
        this.nameReport = nameReport;
        this.address = address;
        this.context = context;
        nameFileReport = "reports/" + nameReport + ".xlsx";
        fileReport = new File(context.getFilesDir(), nameFileReport);
        createReport();
        openBook();
        parseHeaders(sheet);
        newRow();
    }

    public Report(File file) throws IOException {
        fileReport = file;
        openBook();
        parseHeaders(sheet);
        readReport(2);
        newRow();
        nameReport = fileReport.getName().replace(".xlsx", "");
    }

    public void openBook() throws IOException {
        try (FileInputStream fis = new FileInputStream(fileReport)) {
            workbook = new XSSFWorkbook(fis);
        }
        sheet = workbook.getSheetAt(0);
    }

    public void closeBook() throws IOException {
        workbook.close();
    }

    public Map<String, Point> parseHeaders(XSSFSheet sheet) {
        Row headerRow = sheet.getRow(0);
        if (headerRow != null) {
            for (int x = 0; x < headerRow.getPhysicalNumberOfCells(); x++) {
                Cell cell = headerRow.getCell(x);
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    String header = cell.getStringCellValue();
                    fields.put(header.trim(), new Point(0, x));
                }
            }
        }

        keys = new ArrayList<>(fields.keySet());

        return fields;
    }

    public String checkKeyword(String hypothesis) {
        for (String key : fields.keySet()) {
            if (hypothesis.toLowerCase().contains(key.toLowerCase())) {
                return key;
            }
        }
        return null;
    }

    public void newRow() {
        int lastRowNum = sheet.getLastRowNum();
        Row row = sheet.createRow(lastRowNum + 1);
    }

    public int getRow() {
        return currentRow;
    }

    public int nextRow() {
        if (currentRow >= sheet.getLastRowNum()) {
            sheet.createRow(currentRow + 1);
        }
        currentRow++;
        return currentRow;
    }

    public int previousRow() {
        if (currentRow > 1) {
            currentRow--;
        }
        return currentRow;
    }

    public void writeToReport(String key, String value) throws IOException {
        Point point = fields.get(key);
        if (point != null) {
            int lastRowNum = sheet.getLastRowNum();
            int nextRowNum = lastRowNum;

            Row row = sheet.getRow(nextRowNum);
            if (row == null) {
                row = sheet.createRow(nextRowNum);
            }

            Cell cell = row.getCell(point.y);
            if (cell == null) {
                cell = row.createCell(point.y);
            }

            cell.setCellValue(value);

            saveFile();
        } else {
            Log.w("Report", "Не найден заголовок '" + key + "'");
        }
    }

    public void writeToCellInRow(int rowIndex, String key, String value) {
        Point point = fields.get(key);
        if (point != null) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                row = sheet.createRow(rowIndex);
            }
            Cell cell = row.getCell(point.y);
            if (cell == null) {
                cell = row.createCell(point.y);
            }
            cell.setCellValue(value);
        } else {
            Log.w("Report", "Не найден заголовок '" + key + "'");
        }
    }

    public void saveFile() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileReport)) {
            workbook.write(fos);
        }
    }

    public void createReport() {
        File report = new File(context.getFilesDir(), nameFileReport);
        File dir = report.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (InputStream inputStream = context.getAssets().open("pattern.xlsx")) {
            Files.copy(inputStream, report.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Log.e("FileCopy", "Ошибка копирования файла: ", e);
        }
    }

    public int sizeRow() {
        return sheet.getLastRowNum() + 1;
    }


    public List<String> readReport(int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            return Collections.emptyList();
        }

        int numCols = sheet.getRow(0).getPhysicalNumberOfCells();

        List<String> data = new ArrayList<>(numCols);

        for (int i = 0; i < numCols; i++) {
            Cell cell = row.getCell(i);
            if (cell == null || cell.getCellType() == CellType.BLANK) {
                data.add("");
            } else {
                switch (cell.getCellType()) {
                    case STRING:
                        data.add(cell.getStringCellValue());
                        break;
                    case NUMERIC:
                        data.add(Double.toString(cell.getNumericCellValue()));
                        break;
                    case BOOLEAN:
                        data.add(Boolean.toString(cell.getBooleanCellValue()));
                        break;
                    default:
                        data.add("");
                }
            }
        }

        System.out.println(data.toString());
        return List.copyOf(data);
    }
}