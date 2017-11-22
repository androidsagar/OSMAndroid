package com.rahulrv.tweetz;

import android.app.Application;

import com.rahulrv.tweetz.di.component.AppComponent;
import com.rahulrv.tweetz.di.component.DaggerAppComponent;
import com.rahulrv.tweetz.di.module.AppModule;
import com.rahulrv.tweetz.di.module.NetworkModule;
import com.rahulrv.tweetz.di.module.TwitterModule;

/**
 * Main application class. Initializes dagger.
 */
public class MyApplication extends Application {

    private static  AppComponent component;

    @Override public void onCreate() {
        super.onCreate();
        component = DaggerAppComponent.builder()
                .twitterModule(new TwitterModule())
                .appModule(new AppModule(this))
                .networkModule(new NetworkModule())
                .build();
    }

    public static AppComponent getComponent() {
        return component;
    }
}
