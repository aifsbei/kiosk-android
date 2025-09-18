package com.coderbunker.kioskapp;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Handler;
import android.util.Log;

public class TrafficMonitor {

    private final int uid;
    private final Handler handler;
    private final int intervalMs;

    private long rxStart;
    private long txStart;

    private boolean isRunning = false;

    private final Runnable monitorRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) return;

            long rxCurrent = TrafficStats.getUidRxBytes(uid);
            long txCurrent = TrafficStats.getUidTxBytes(uid);

            long rxUsed = rxCurrent - rxStart;
            long txUsed = txCurrent - rxStart;

            Log.d("Traffic Monitor", String.format("Traffic since start: rx = %.2f MB; tx = %.2f MB", bytesToMB(rxUsed), bytesToMB(txUsed)));

            handler.postDelayed(this, intervalMs);
        }
    };

    public TrafficMonitor(Context context, int intervalMs) {
        this.uid = android.os.Process.myUid();
        this.handler = new Handler();
        this.intervalMs = intervalMs;
    }

    public void start() {
        isRunning = true;
        handler.post(monitorRunnable);
    }

    private double bytesToMB(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }
}
