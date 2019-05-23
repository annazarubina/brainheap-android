package com.brainheap.android.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.brainheap.android.Constants
import com.brainheap.android.Constants.ID_PROP
import com.brainheap.android.CredentialsHolder
import com.brainheap.android.R
import com.brainheap.android.model.UserView
import com.brainheap.android.network.RetrofitFactory
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val RC_SIGN_IN = 9001


class LoginFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var viewModel: LoginViewModel

    private val callbackManager: CallbackManager = CallbackManager.Factory.create()
    private val fbLoginManager: LoginManager = LoginManager.getInstance()

    private val mapper = ObjectMapper()

    init {
        fbLoginManager.registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Toast.makeText(activity, "Facebook token: " + loginResult.accessToken.token, Toast.LENGTH_SHORT)
                        .show()
                    viewModel.oAuthData.postValue(
                        OAuthData(
                            loginResult.accessToken.userId,
                            loginResult.accessToken.token
                        )
                    )
                }
                override fun onCancel() {
                    Toast.makeText(activity, "Facebook: onCancel", Toast.LENGTH_SHORT).show()
                }
                override fun onError(error: FacebookException) {
                    Toast.makeText(activity, "Facebook: onError", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    @SuppressLint("CommitPrefEdits")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity) ?: return
        val mail = sharedPref.getString(Constants.NAME_PROP, "Unknown")
        emailEditText.setText(mail)

        activity?.let {
            viewModel = ViewModelProviders.of(it).get(LoginViewModel::class.java)
        }

        viewModel.loginSuccess.observe(this, Observer {
            if (it) findNavController(this).navigate(R.id.action_logged_in)
        })

        viewModel.oAuthData.observe(this, Observer {
            getAuthUserEmail(it)
        })

        viewModel.email.observe(this, Observer {
            getUserId(it)
        })

        viewModel.userId.observe(this, Observer {
            CredentialsHolder.userId = it
            sharedPref.edit().putString(ID_PROP, it).apply()
            viewModel.loginSuccess.postValue(true)
        })

        emailSetButton.setOnClickListener {
            Toast.makeText(activity, "Trying to fetch user data", Toast.LENGTH_SHORT).show()
            viewModel.email.postValue(emailEditText.text.toString())
        }

        fb_login_button.setOnClickListener {
            fbLoginManager.logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        }
        google_login_button.setOnClickListener {
            val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(activity!!, googleSignInOptions)
            val signInIntent : Intent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    private fun getUserId(email: String?) {
        email?.let {
            CoroutineScope(Dispatchers.IO).launch {
                var userId: String? = null
                val retrofitService = RetrofitFactory.makeRetrofitService()
                var toastMessage : String? = null
                try {
                    val findUserRequest = retrofitService.findUserAsync(email)
                    val findUserResponse = findUserRequest.await()
                    if (findUserResponse.isSuccessful) {
                        userId = findUserResponse.body()?.firstOrNull()?.id
                    } else if (findUserResponse.code() == 404) {
                        val createUserRequest = retrofitService.createUserAsync(UserView(email, email))
                        val createUserResponse = createUserRequest.await()
                        if (createUserResponse.isSuccessful) {
                            userId = createUserResponse.body()?.id
                        } else {
                            toastMessage = "CreateUser failed:${createUserResponse.code()}"
                        }
                    } else {
                        toastMessage = "FindUser failed:${findUserResponse.code()}"

                    }
                } catch (e: HttpException) {
                    toastMessage = "Exception ${e.message}"
                } catch (e: Throwable) {
                    toastMessage = "Exception ${e.message}"
                }
                toastMessage?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity, "Error: $it", Toast.LENGTH_SHORT).show()
                    }
                }
                userId?.let { viewModel.userId.postValue(it) }
            }
        }
    }

    private fun getAuthUserEmail(data: OAuthData?) {
        data?.let {
            CoroutineScope(Dispatchers.IO).launch {
                var toastMessage : String? = null
                val graphUrl =
                    URL("https://graph.facebook.com/${data.userId}?fields=id,name,email&access_token=${data.token}")
                try {
                    val connection = graphUrl.openConnection()
                    val inputStream = BufferedReader(InputStreamReader(connection.getInputStream()))
                    val buffer = StringBuffer()
                    var line = inputStream.readLine()
                    while (line != null) {
                        buffer.append(line)
                        line = inputStream.readLine()
                    }
                    inputStream.close()
                    val userData = mapper.readValue(buffer.toString(), OAuthUserData::class.java)
                    viewModel.email.postValue(userData.email)
                } catch (e: Throwable) {
                    toastMessage = "Exception ${e.message}"
                }
                toastMessage?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity, "Facebook auth error: $it", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            onGoogleActivityResult(data!!)
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun onGoogleActivityResult(data: Intent) {
        var toastMessage : String? = null
        try {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            viewModel.email.postValue(account?.email)
        } catch (e: ApiException) {
            toastMessage = "Api exception. Error code: " + e.statusCode
        }
        toastMessage?.let { Toast.makeText(activity, "Google auth error: $it", Toast.LENGTH_SHORT).show() }
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
