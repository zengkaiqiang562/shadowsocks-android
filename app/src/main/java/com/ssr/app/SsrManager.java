package com.ssr.app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.library.ssr.aidl.TrafficStats;

import java.util.ArrayList;
import java.util.List;

public enum SsrManager {
    INSTANCE;

    private List<SsrListener> ssrListeners = new ArrayList<>();


    public void addSsrListener(@NonNull SsrListener listener) {
        if (!ssrListeners.contains(listener)) {
            ssrListeners.add(listener);
        }
    }

    public void removeSsrListener(@NonNull SsrListener listener) {
        ssrListeners.remove(listener);
    }

    public interface SsrListener {
        void onStateChanged(ConnectState state, @Nullable String msg, @Nullable Throwable throwable);
        void onTrafficChanged(long txRate, long rxRate, long txTotal, long rxTotal);
    }
}
