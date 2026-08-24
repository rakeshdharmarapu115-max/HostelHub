package com.hostelhub.app.di

import com.hostelhub.app.data.remote.repository.*
import com.hostelhub.app.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: RemoteAuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindStudentRepository(impl: RemoteStudentRepositoryImpl): StudentRepository

    @Binds
    @Singleton
    abstract fun bindHostelRepository(impl: RemoteHostelRepositoryImpl): HostelRepository

    @Binds
    @Singleton
    abstract fun bindRoomRepository(impl: RemoteRoomRepositoryImpl): RoomRepository

    @Binds
    @Singleton
    abstract fun bindFeePaymentRepository(impl: RemoteFeePaymentRepositoryImpl): FeePaymentRepository

    @Binds
    @Singleton
    abstract fun bindComplaintRepository(impl: RemoteComplaintRepositoryImpl): ComplaintRepository

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(impl: RemoteAttendanceRepositoryImpl): AttendanceRepository

    @Binds
    @Singleton
    abstract fun bindFoodMenuRepository(impl: RemoteFoodMenuRepositoryImpl): FoodMenuRepository

    @Binds
    @Singleton
    abstract fun bindAnnouncementRepository(impl: RemoteAnnouncementRepositoryImpl): AnnouncementRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: RemoteNotificationRepositoryImpl): NotificationRepository
}
