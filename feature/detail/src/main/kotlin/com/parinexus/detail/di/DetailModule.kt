package com.parinexus.detail.di

import com.parinexus.data.util.NoteFormatterImpl
import com.parinexus.data.util.SystemClockProvider
import com.parinexus.domain.ClockProvider
import com.parinexus.domain.NoteFormatter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object DetailModule {
    @Provides fun provideClockProvider(): ClockProvider = SystemClockProvider()
    @Provides fun provideNoteFormatter(impl: NoteFormatterImpl): NoteFormatter = impl
}
