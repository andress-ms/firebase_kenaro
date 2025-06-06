package com.amms.kenaro.services

import com.amms.kenaro.dataclass.Product
import com.google.firebase.appdistribution.gradle.ApiService

class ProductRepository(private val api: ApiService) {
    suspend fun fetchAll(): List<Product> = api.getAll()
    suspend fun add(product: Product): Product = api.create(product)
}