package com.apiarymanager.apiaryassistant;

import android.app.Application;

public class App extends Application {
    private Report activeReport;

    public Report getActiveReport() {
        return activeReport;
    }

    public void setActiveReport(Report report) {
        this.activeReport = report;
    }
}
