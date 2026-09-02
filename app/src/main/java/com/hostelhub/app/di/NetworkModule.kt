package com.hostelhub.app.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hostelhub.app.data.remote.NetworkConfig
import com.hostelhub.app.data.remote.api.*
import com.hostelhub.app.data.remote.interceptor.AuthInterceptor
import com.hostelhub.app.data.remote.interceptor.DynamicHostInterceptor
import com.hostelhub.app.data.remote.interceptor.RetryInterceptor
import com.hostelhub.app.data.remote.interceptor.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("baseUrl")
    fun provideBaseUrl(networkConfig: NetworkConfig): String {
        return networkConfig.getBaseUrl()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        dynamicHostInterceptor: DynamicHostInterceptor,
        authInterceptor: AuthInterceptor,
        retryInterceptor: RetryInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(dynamicHostInterceptor)
            .addInterceptor(retryInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        networkConfig: NetworkConfig,
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(networkConfig.getBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideUsersApi(retrofit: Retrofit): UsersApi = retrofit.create(UsersApi::class.java)

    @Provides
    @Singleton
    fun provideStudentApi(retrofit: Retrofit): StudentApi = retrofit.create(StudentApi::class.java)

    @Provides
    @Singleton
    fun provideHostelApi(retrofit: Retrofit): HostelApi = retrofit.create(HostelApi::class.java)

    @Provides
    @Singleton
    fun provideRoomApi(retrofit: Retrofit): RoomApi = retrofit.create(RoomApi::class.java)

    @Provides
    @Singleton
    fun provideFeePaymentApi(retrofit: Retrofit): FeePaymentApi = retrofit.create(FeePaymentApi::class.java)

    @Provides
    @Singleton
    fun provideComplaintApi(retrofit: Retrofit): ComplaintApi = retrofit.create(ComplaintApi::class.java)

    @Provides
    @Singleton
    fun provideAttendanceApi(retrofit: Retrofit): AttendanceApi = retrofit.create(AttendanceApi::class.java)

    @Provides
    @Singleton
    fun provideFoodMenuApi(retrofit: Retrofit): FoodMenuApi = retrofit.create(FoodMenuApi::class.java)

    @Provides
    @Singleton
    fun provideAnnouncementApi(retrofit: Retrofit): AnnouncementApi = retrofit.create(AnnouncementApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi = retrofit.create(NotificationApi::class.java)
}