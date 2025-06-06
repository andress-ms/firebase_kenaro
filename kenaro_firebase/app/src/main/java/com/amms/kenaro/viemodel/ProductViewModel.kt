package com.amms.kenaro.viemodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amms.kenaro.dataclass.Product
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    fun fetchProducts() {
        viewModelScope.launch {
            try {
                val products = RetrofitInstance.apiService.getAll()
                // Actualiza la UI con los productos
            } catch (e: Exception) {
                // Maneja el error
            }
        }
    }

    fun createProduct(product: Product) {
        viewModelScope.launch {
            try {
                val createdProduct = RetrofitInstance.apiService.create(product)
                // Actualiza la UI con el producto creado
            } catch (e: Exception) {
                // Maneja el error
            }
        }
    }
}