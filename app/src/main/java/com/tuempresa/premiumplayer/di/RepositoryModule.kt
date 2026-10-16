package com.tuempresa.premiumplayer.di

import android.content.Context
import com.tuempresa.premiumplayer.data.MusicDao
import com.tuempresa.premiumplayer.data.repository.MusicRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMusicRepository(
        @ApplicationContext context: Context,
        dao: MusicDao
    ): MusicRepository {
        return MusicRepository(context, dao)
    }
}
