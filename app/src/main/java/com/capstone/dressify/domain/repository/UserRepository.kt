package com.capstone.dressify.domain.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.capstone.dressify.data.local.datastore.UserPreference
import com.capstone.dressify.data.remote.api.ApiService
import com.capstone.dressify.data.remote.response.CatalogResponse
import com.capstone.dressify.data.remote.response.LoginResponse
import com.capstone.dressify.data.remote.response.RegisterResponse
import com.capstone.dressify.domain.model.User
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository constructor(
    private val apiService: ApiService,
    private val pref: UserPreference
) {
    val _productList = MutableLiveData<List<CatalogResponse>>()
    val productList : LiveData<List<CatalogResponse>>get()  = _productList

    val _isLoading = MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> = _isLoading


    suspend fun getProductCatalog(): List<CatalogResponse> = withContext(Dispatchers.IO) {
        val response = apiService.getProducts().execute()

        if (response.isSuccessful) {
            response.body() ?: emptyList() // Return the list or an empty list if null
        } else {
            throw Exception("Failed to fetch products: ${response.code()}") // Throw an exception on failure
        }
    }



    fun getSession(): LiveData<User> {
        return pref.getToken().asLiveData()
    }

    suspend fun saveSession(user: User) {
        return pref.saveUserToken(user)
    }

    suspend fun isLoggedIn() {
        return pref.isLogin()
    }

    suspend fun login(email: String, password: String) : LoginResponse{
        val param = JsonObject().apply {
            addProperty("email", email)
            addProperty("password", password)
        }
        var response = apiService.login(param)
        return response
    }

    suspend fun register(email: String, username: String, password: String): RegisterResponse {
        val param = JsonObject().apply {
            addProperty("email", email)
            addProperty("username", username)
            addProperty("password", password)
        }
        var response = apiService.register(param)
        return response
    }
        
//    suspend fun login(email: String, password: String) : LoginResponse {
//        return apiService.login(email, password)
//    }


    suspend fun logout() {
        return pref.clearUserToken()
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference
        ): UserRepository {
            return instance ?: synchronized(this) {
                instance ?: UserRepository(
                    apiService,
                    userPreference
                ).also { instance = it }
            }
        }
    }
}