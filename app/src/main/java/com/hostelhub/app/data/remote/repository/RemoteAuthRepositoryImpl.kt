package com.hostelhub.app.data.remote.repository

import com.hostelhub.app.data.remote.api.AuthApi
import com.hostelhub.app.data.remote.api.UsersApi
import com.hostelhub.app.data.remote.datasource.TokenManager
import com.hostelhub.app.data.remote.dto.*
import com.hostelhub.app.domain.model.Admin
import com.hostelhub.app.domain.model.Host
import com.hostelhub.app.domain.model.Student
import com.hostelhub.app.domain.model.User
import com.hostelhub.app.domain.model.UserRole
import com.hostelhub.app.domain.repository.AuthRepository
import com.hostelhub.app.utils.ErrorParser
import com.hostelhub.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteAuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val usersApi: UsersApi,
    private val tokenManager: TokenManager
) : AuthRepository {

    override fun getCurrentUser(): Flow<User?> = tokenManager.currentUserFlow

    override suspend fun login(email: String, password: String, role: UserRole): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.login(LoginRequestDto(identifier = email, email = email, password = password))
            if (response.isSuccessful && response.body()?.data != null) {
                val authData = response.body()!!.data!!
                val user = authData.user.toDomain()
                tokenManager.saveTokens(authData.tokens.accessToken, authData.tokens.refreshToken)
                tokenManager.saveUser(user)
                Resource.Success(user)
            } else {
                val errorMsg = response.body()?.message ?: ErrorParser.parseErrorMessage(response, "Authentication failed")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(ErrorParser.parseExceptionMessage(e, "Network error during login"))
        }
    }

    override suspend fun validateStudentId(studentId: String): Resource<ValidateStudentIdResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.validateStudentId(ValidateStudentIdRequestDto(studentId.trim()))
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                val errorMsg = response.body()?.message ?: ErrorParser.parseErrorMessage(response, "Invalid Student ID")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(ErrorParser.parseExceptionMessage(e, "Error verifying Student ID"))
        }
    }

    override suspend fun activateStudent(
        studentId: String,
        emailOrPhone: String,
        password: String
    ): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val request = ActivateStudentRequestDto(
                studentId = studentId.trim(),
                emailOrPhone = emailOrPhone.trim(),
                password = password
            )
            val response = authApi.activateStudent(request)
            if (response.isSuccessful && response.body()?.data != null) {
                val authData = response.body()!!.data!!
                val user = authData.user.toDomain()
                tokenManager.saveTokens(authData.tokens.accessToken, authData.tokens.refreshToken)
                tokenManager.saveUser(user)
                Resource.Success(user)
            } else {
                val errorMsg = response.body()?.message ?: ErrorParser.parseErrorMessage(response, "Student activation failed")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(ErrorParser.parseExceptionMessage(e, "Error activating student account"))
        }
    }

    override suspend fun forgotPassword(identifier: String): Resource<ForgotPasswordResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.forgotPassword(ForgotPasswordRequestDto(identifier.trim()))
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                val errorMsg = response.body()?.message ?: ErrorParser.parseErrorMessage(response, "Account not found")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(ErrorParser.parseExceptionMessage(e, "Error requesting password reset"))
        }
    }

    override suspend fun resetPassword(
        identifier: String,
        otp: String,
        newPassword: String
    ): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = ResetPasswordRequestDto(
                identifier = identifier.trim(),
                otp = otp.trim(),
                newPassword = newPassword
            )
            val response = authApi.resetPassword(request)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                val errorMsg = response.body()?.message ?: ErrorParser.parseErrorMessage(response, "Failed to reset password")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(ErrorParser.parseExceptionMessage(e, "Error resetting password"))
        }
    }

    override suspend fun registerStudent(student: Student, password: String): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterStudentRequestDto(
                email = if (student.email.isNotBlank()) student.email else (student.rollNumber.lowercase().replace(" ", "") + "@campus.edu"),
                password = password,
                fullName = student.fullName,
                rollNumber = student.rollNumber,
                collegeName = student.collegeName,
                course = student.course,
                yearOfStudy = student.yearOfStudy,
                gender = student.gender,
                permanentAddress = student.permanentAddress,
                emergencyContactName = student.emergencyContactName,
                emergencyContactPhone = student.emergencyContactPhone,
                hostelId = student.hostelId,
                roomId = student.roomId,
                bedNumber = student.bedNumber
            )
            val response = authApi.registerStudent(request)
            if (response.isSuccessful && response.body()?.data != null) {
                val authData = response.body()!!.data!!
                val user = authData.user.toDomain()
                tokenManager.saveTokens(authData.tokens.accessToken, authData.tokens.refreshToken)
                tokenManager.saveUser(user)
                Resource.Success(user)
            } else {
                val errorMsg = response.body()?.message ?: ErrorParser.parseErrorMessage(response, "Registration failed")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(ErrorParser.parseExceptionMessage(e, "Network error during student registration"))
        }
    }

    override suspend fun registerHost(host: Host, password: String): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterHostRequestDto(
                email = host.contactEmail,
                password = password,
                fullName = host.fullName,
                businessName = host.businessName,
                contactPhone = host.contactPhone,
                contactEmail = host.contactEmail
            )
            val response = authApi.registerHost(request)
            if (response.isSuccessful && response.body()?.data != null) {
                val authData = response.body()!!.data!!
                val user = authData.user.toDomain()
                tokenManager.saveTokens(authData.tokens.accessToken, authData.tokens.refreshToken)
                tokenManager.saveUser(user)
                Resource.Success(user)
            } else {
                val errorMsg = response.body()?.message ?: ErrorParser.parseErrorMessage(response, "Host registration failed")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(ErrorParser.parseExceptionMessage(e, "Network error during host registration"))
        }
    }

    override suspend fun registerAdmin(admin: Admin, password: String): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterAdminRequestDto(
                email = if (admin.userId.isNotBlank()) admin.userId else "${admin.fullName.lowercase().replace(" ", "")}@campus.edu",
                password = password,
                fullName = admin.fullName,
                associationName = admin.associationName,
                designation = admin.designation,
                contactPhone = admin.contactPhone
            )
            val response = authApi.registerAdmin(request)
            if (response.isSuccessful && response.body()?.data != null) {
                val authData = response.body()!!.data!!
                val user = authData.user.toDomain()
                tokenManager.saveTokens(authData.tokens.accessToken, authData.tokens.refreshToken)
                tokenManager.saveUser(user)
                Resource.Success(user)
            } else {
                val errorMsg = response.body()?.message ?: ErrorParser.parseErrorMessage(response, "Association Head registration failed")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(ErrorParser.parseExceptionMessage(e, "Network error during admin registration"))
        }
    }

    override suspend fun logout(): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val refreshToken = tokenManager.getRefreshToken()
            if (!refreshToken.isNullOrBlank()) {
                authApi.logout(RefreshTokenRequestDto(refreshToken))
            }
            tokenManager.clearSession()
            Resource.Success(Unit)
        } catch (e: Exception) {
            tokenManager.clearSession()
            Resource.Success(Unit)
        }
    }

    override fun getAllUsers(): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading)
        try {
            val response = usersApi.getAllUsers()
            if (response.isSuccessful && response.body()?.data != null) {
                val users = response.body()!!.data!!.map { it.toDomain() }
                emit(Resource.Success(users))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch users"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching users"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun toggleUserStatus(userId: String, isActive: Boolean): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = usersApi.toggleUserStatus(userId, mapOf("isActive" to isActive))
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to update user status")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error updating user status")
        }
    }
}
