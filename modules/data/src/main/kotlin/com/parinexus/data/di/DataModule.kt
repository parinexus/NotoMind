package com.parinexus.data.di

import com.parinexus.data.repository.NoteTagRepositoryImpl
import com.parinexus.data.repository.NoteRepositoryImpl
import com.parinexus.data.repository.UserSettingsRepositoryImpl
import com.parinexus.data.util.NetworkMonitorImpl
import com.parinexus.data.util.NetworkMonitor
import com.parinexus.domain.repository.NoteTagRepository
import com.parinexus.domain.repository.NoteRepository
import com.parinexus.domain.repository.UserSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    internal abstract fun bindsNetworkMonitor(
        networkMonitor: NetworkMonitorImpl,
    ): NetworkMonitor

    @Binds
    internal abstract fun bindsUserDataRepository(
        userDataRepository: UserSettingsRepositoryImpl,
    ): UserSettingsRepository

    @Binds
    internal abstract fun bindsLabelRepository(
        noteTagRepositoryImpl: NoteTagRepositoryImpl,
    ): NoteTagRepository

    @Binds
    internal abstract fun bindsNotePadRepository(
        notePadRepositoryImpl: NoteRepositoryImpl,
    ): NoteRepository
}
