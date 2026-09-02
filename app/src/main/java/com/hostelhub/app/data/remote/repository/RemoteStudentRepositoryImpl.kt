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

    override suspend fun generateStudentId(): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val response = studentApi.generateStudentId()
            if (response.isSuccessful && response.body()?.data != null) {
                val studentId = response.body()!!.data!!["studentId"] ?: ""
                Resource.Success(studentId)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to generate Student ID")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error generating Student ID")
        }
    }

    override suspend fun createStudentByAdmin(student: Student, password: String): Resource<Student> = withContext(Dispatchers.IO) {
        try {
            val payload = mapOf(
                "fullName" to student.fullName,
                "email" to student.email,
                "phoneNumber" to student.emergencyContactPhone,
                "collegeName" to student.collegeName,
                "course" to student.course,
                "yearOfStudy" to student.yearOfStudy,
                "gender" to student.gender,
                "permanentAddress" to student.permanentAddress,
                "emergencyContactName" to student.emergencyContactName,
                "emergencyContactPhone" to student.emergencyContactPhone,
                "studentId" to student.rollNumber,
                "password" to password,
                "hostelId" to (student.hostelId ?: ""),
                "roomId" to (student.roomId ?: ""),
                "bedNumber" to (student.bedNumber ?: "")
            )
            var response = studentApi.createStudentByAdmin(payload)
            if (response.code() == 404) {
                response = studentApi.createStudentDirect(payload)
            }
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(student)
            } else {
                val errorMsg = try {
                    val rawError = response.errorBody()?.string()
                    if (!rawError.isNullOrBlank()) {
                        val json = org.json.JSONObject(rawError)
                        json.optString("message", json.optString("error", "Failed to create student record (HTTP ${response.code()})"))
                    } else {
                        response.body()?.message ?: "Failed to create student record (HTTP ${response.code()})"
                    }
                } catch (_: Exception) {
                    "Failed to create student record (HTTP ${response.code()})"
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error registering student")
        }
    }

    override suspend fun deallocateStudent(studentId: String, remarks: String): Resource<Student> = withContext(Dispatchers.IO) {
        try {
            val response = studentApi.deallocateStudent(studentId, mapOf("remarks" to remarks))
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!.toDomain())
            } else {
                Resource.Error(response.body()?.message ?: "Failed to deallocate student")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error deallocating student")
        }
    }

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
