package com.ssr.app;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.Configuration;

import com.library.ssr.Core;

public class App extends Application implements Configuration.Provider {
    @Override
    public void onCreate() {
        super.onCreate();

        Core.INSTANCE.init(this);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return Core.INSTANCE.getWorkManagerConfiguration();
    }

    @Override
    public void onConfigurationChanged(@NonNull android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Core.INSTANCE.updateNotificationChannels();
    }
}
