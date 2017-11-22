package com.rahulrv.tweetz.di.module;

import com.rahulrv.tweetz.api.TwitterApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

/**
 *
 *
 */
@Module
public class TwitterModule {

    @Provides
    @Singleton
    TwitterApi provideRetrofit(Retrofit retrofit) {
        return retrofit.create(TwitterApi.class);
    }

}
