package com.hostelhub.app.data.remote.repository

import com.hostelhub.app.data.remote.api.StudentApi
import com.hostelhub.app.data.remote.dto.StudentDto
import com.hostelhub.app.domain.model.Student
import com.hostelhub.app.domain.model.StudentDashboardStats
import com.hostelhub.app.domain.repository.StudentRepository
import com.hostelhub.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteStudentRepositoryImpl @Inject constructor(
    private val studentApi: StudentApi
) : StudentRepository {

    override fun getStudentProfile(studentId: String): Flow<Resource<Student>> = flow {
        emit(Resource.Loading)
        try {
            val response = studentApi.getStudentById(studentId)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(Resource.Success(response.body()!!.data!!.toDomain()))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch student profile"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching student profile"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateStudentProfile(student: Student): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val dto = StudentDto(
                studentId = student.studentId,
                userId = student.userId,
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
                hostelName = student.hostelName,
                roomId = student.roomId,
                roomNumber = student.roomNumber,
                bedNumber = student.bedNumber,
                admissionDate = student.admissionDate,
                status = student.status.name
            )
            val response = studentApi.updateStudentProfile(student.studentId, dto)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to update profile")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error updating student profile")
        }
    }

    override fun getResidentsByHostel(hostelId: String): Flow<Resource<List<Student>>> = flow {
        emit(Resource.Loading)
        try {
            val response = studentApi.getResidentsByHostel(hostelId)
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomain() }
                emit(Resource.Success(list))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch residents"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching hostel residents"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAllStudents(): Flow<Resource<List<Student>>> = flow {
        emit(Resource.Loading)
        try {
            val response = studentApi.getAllStudents()
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomain() }
                emit(Resource.Success(list))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch students"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching students"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun deleteStudent(studentId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = studentApi.deleteStudent(studentId)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to delete student")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error deleting student")
        }
    }

    override fun getStudentDashboardStats(studentId: String): Flow<Resource<StudentDashboardStats>> = flow {
        emit(Resource.Loading)
        try {
            val response = studentApi.getStudentDashboardStats(studentId)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(Resource.Success(response.body()!!.data!!.toDomain()))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch dashboard stats"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching student dashboard stats"))
        }
    }.flowOn(Dispatchers.IO)
}
