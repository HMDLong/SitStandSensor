package com.example.gravsensor.di

import com.example.gravsensor.repository.DataRepository
import com.example.gravsensor.repository.sources.DataSource
import com.example.gravsensor.repository.sources.remote.FirestoreSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRemoteSource() : DataSource.Remote? {
        return FirestoreSource()
    }

    @Provides
    fun provideLocalSource() : DataSource.Local? {
        return null
    }

    @Provides
    @Singleton
    fun provideRepository(
        remoteSource: DataSource.Remote?,
        localSource: DataSource.Local?,
    ) : DataRepository {
        return DataRepository.getInstance(remote = remoteSource, local = localSource)
    }
}