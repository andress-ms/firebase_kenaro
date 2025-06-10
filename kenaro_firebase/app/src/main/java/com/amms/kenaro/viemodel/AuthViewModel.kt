package com.amms.kenaro.viemodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amms.kenaro.dataclass.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private val _users = MutableLiveData<List<User>>(emptyList())
    val users: LiveData<List<User>> = _users

    private val _authResult = MutableLiveData<Result<Unit>>()
    val authResult: LiveData<Result<Unit>> = _authResult

    /** Registra en FirebaseAuth + guarda perfil en Firestore */
    fun signUp(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { cred ->
                val uid = cred.user!!.uid
                val user = User(uid = uid, name = name, email = email)
                db.collection("users").document(uid)
                    .set(user)
                    .addOnSuccessListener { _authResult.postValue(Result.success(Unit)) }
                    .addOnFailureListener { e -> _authResult.postValue(Result.failure(e)) }
            }
            .addOnFailureListener { e ->
                _authResult.postValue(Result.failure(e))
            }
    }

    /** Inicia sesión con email/contraseña */
    fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { _authResult.postValue(Result.success(Unit)) }
            .addOnFailureListener { e -> _authResult.postValue(Result.failure(e)) }
    }

    /** Trae todos los perfiles de “users” */
    fun fetchUsers() {
        db.collection("users")
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { it.toObject(User::class.java) }
                _users.postValue(list)
            }
            .addOnFailureListener { /* manejar error si quieres */ }
    }
}