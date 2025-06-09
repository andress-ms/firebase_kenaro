package com.amms.kenaro

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.amms.kenaro.ui.theme.KenaroTheme

class MainActivity : AppCompatActivity() {

    private lateinit var vb: ActivityMainBinding

    // Point explicitly at your instance so you can change it easily if needed
    private val dbUrl = "https://project-47419-default-rtdb.firebaseio.com/"
    private val database by lazy { FirebaseDatabase.getInstance(dbUrl) }
    private val messageRef by lazy { database.getReference("messages") }   // leaf node

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)

        // 1️⃣  Make sure we have an authenticated user (anonymous is fine for dev)
        ensureSignedIn { uid ->
            Snackbar.make(vb.root, "Signed in as $uid", Snackbar.LENGTH_SHORT).show()
            initUi()
        }
    }

    /** Called only after Auth is successful */
    private fun initUi() {
        // Push a new child on every click
        vb.btnSend.setOnClickListener {
            val text = vb.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                messageRef.push().setValue(text)
                vb.etMessage.text?.clear()
            }
        }

        // Live updates from DB
        messageRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val builder = StringBuilder()
                snapshot.children.forEach { child ->
                    builder.appendLine(child.getValue(String::class.java))
                }
                vb.tvResult.text = builder.toString().ifEmpty { "— no data —" }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RTDB", "listen cancelled: ${error.message}", error.toException())
                Snackbar.make(vb.root, "DB error: ${error.message}", Snackbar.LENGTH_LONG).show()
            }
        })
    }

    /**
     * Ensures we have a FirebaseAuth user; runs [onReady] with the uid.
     * If already signed in, it fires immediately.
     */
    private fun ensureSignedIn(onReady: (String) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        auth.currentUser?.let { user ->
            onReady(user.uid)
            return
        }
        auth.signInAnonymously()
            .addOnSuccessListener { onReady(it.user!!.uid) }
            .addOnFailureListener { e ->
                Log.e("Auth", "Anonymous sign-in failed", e)
                Snackbar.make(vb.root, "Auth failed: ${e.message}", Snackbar.LENGTH_INDEFINITE).show()
            }
    }
}