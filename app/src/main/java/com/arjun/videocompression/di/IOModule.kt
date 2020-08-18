package com.arjun.videocompression.di

import android.content.Context
import com.arjun.videocompression.util.FileHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ApplicationComponent::class)
object IOModule {
    @Provides
    fun provideFileHelper(@ApplicationContext context: Context): FileHelper =
        FileHelper(context)
}