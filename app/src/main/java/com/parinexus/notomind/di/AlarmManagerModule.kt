package com.parinexus.notomind.di

import com.parinexus.domain.IAlarmManager
import com.parinexus.notomind.util.AlarmManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmManagerModule {

    @Binds
    internal abstract fun bindsAlarmManager(
        alarmManagerImpl: AlarmManagerImpl,
    ): IAlarmManager

}
