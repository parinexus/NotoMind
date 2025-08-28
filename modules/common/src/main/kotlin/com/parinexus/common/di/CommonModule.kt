package com.parinexus.common.di

import com.parinexus.common.ContentManager
import com.parinexus.common.IContentManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CommonModule {
    @Binds
    internal abstract fun bindsContentManager(
        contentManager: ContentManager,
    ): IContentManager
}
