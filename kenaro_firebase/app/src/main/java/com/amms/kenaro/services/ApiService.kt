package com.amms.kenaro.services

import android.os.AsyncTask
import com.amms.kenaro.dataclass.Product
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ApiService {

    private const val BASE_URL = "https://yourapi.com/" // Cambia esta URL por la base de tu API

    // GET Request para obtener productos
    suspend fun getAll(): List<Product> {
        val url = URL("${BASE_URL}api/products")
        val urlConnection = url.openConnection() as HttpURLConnection
        urlConnection.requestMethod = "GET"
        urlConnection.setRequestProperty("Accept", "application/json")

        return withContext(Dispatchers.IO) {
            val responseCode = urlConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = urlConnection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                val products = parseProductResponse(response.toString())
                inputStream.close()
                products
            } else {
                throw Exception("Error getting products")
            }
        }
    }

    // POST Request para crear un producto
    suspend fun create(product: Product): Product {
        val url = URL("${BASE_URL}api/products")
        val urlConnection = url.openConnection() as HttpURLConnection
        urlConnection.requestMethod = "POST"
        urlConnection.setRequestProperty("Content-Type", "application/json")
        urlConnection.doOutput = true

        val jsonInputString = JSONObject().apply {
            put("kenaro", product.kenaro)
            put("name", product.name)
        }.toString()

        withContext(Dispatchers.IO) {
            urlConnection.outputStream.write(jsonInputString.toByteArray())
            urlConnection.outputStream.flush()

            val responseCode = urlConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = urlConnection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                inputStream.close()
                return parseProductResponse(response.toString()).first()
            } else {
                throw Exception("Error creating product")
            }
        }
    }

    // Función para parsear la respuesta del GET request (una lista de productos)
    private fun parseProductResponse(response: String): List<Product> {
        val productsList = mutableListOf<Product>()
        val jsonArray = JSONArray(response)

        for (i in 0 until jsonArray.length()) {
            val productJson = jsonArray.getJSONObject(i)
            val product = Product(
                id = productJson.optLong("id", 0),  // Usamos optLong para evitar valores nulos
                kenaro = productJson.getBoolean("kenaro"),
                name = productJson.getString("name")
            )
            productsList.add(product)
        }
        return productsList
    }
}
