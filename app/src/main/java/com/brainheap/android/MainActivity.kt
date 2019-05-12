package com.brainheap.android

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.brainheap.android.model.UserView
import com.brainheap.android.network.RetrofitFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class MainActivity : AppCompatActivity() {

    val NAME_PROP = "user_name"
    val ID_PROP = "user_id"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        val name = sharedPref.getString(NAME_PROP, "Unknown")
        val editText = findViewById<EditText>(R.id.editText)
        editText.setText(name)
    }

    fun saveName(view: View) {
        val editText = findViewById<EditText>(R.id.editText)

        val email = editText.text.toString()
        Toast.makeText(applicationContext, "Trying to fetch user data", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            val retrofitService = RetrofitFactory.makeRetrofitService()
            var toastMessage = "Unknown error"
            try {
                var userId: String? = null
                val findUserRequest = retrofitService.findUser(email)
                val findUserResponse = findUserRequest.await()
                if (findUserResponse.isSuccessful) {
                    userId = findUserResponse.body()?.id
                } else if (findUserResponse.code() == 404) {
                    val createUserRequest = retrofitService.createUser(UserView(email,email))
                    val createUserResponse = createUserRequest.await()
                    if (createUserResponse.isSuccessful) {
                        userId = createUserResponse.body()?.id
                    } else {
                        toastMessage = "CreateUser failed:${createUserResponse.code()}"
                    }
                } else {
                    toastMessage = "FindUser failed:${findUserResponse.code()}"

                }

                if (userId != null) {
                    val sharedPref = getPreferences(Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {

                            putString(NAME_PROP, email)
                            putString(ID_PROP, userId)
                            commit()
                    }
                    toastMessage =  "Found/Registered Id $userId"
                }

            } catch (e: HttpException) {
                toastMessage = "Exception ${e.message}"

            } catch (e: Throwable) {
                toastMessage = "Exception ${e.message}"
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(applicationContext, toastMessage, Toast.LENGTH_SHORT).show()
            }

        }

    }
}
