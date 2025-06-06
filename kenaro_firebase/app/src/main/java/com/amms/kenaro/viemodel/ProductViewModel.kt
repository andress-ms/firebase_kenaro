package com.amms.kenaro.viemodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amms.kenaro.dataclass.Product
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val _products = mutableStateListOf<Product>()
    val products: List<Product> = _products

    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    private val repository: ProductRepository

    init {
        val interceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8087/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(ApiService::class.java)
        repository = ProductRepository(api)
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val list = repository.fetchAll()
                _products.clear()
                _products.addAll(list)
                errorMessage.value = null
            } catch (e: Exception) {
                errorMessage.value = e.localizedMessage
            } finally {
                isLoading.value = false
            }
        }
    }

    fun addProduct(product: Product, onComplete: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                repository.add(product)
                loadProducts()
                onComplete()
            } catch (e: Exception) {
                errorMessage.value = e.localizedMessage
            } finally {
                isLoading.value = false
            }
        }
    }
}