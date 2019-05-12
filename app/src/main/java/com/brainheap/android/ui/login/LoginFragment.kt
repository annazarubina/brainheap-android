package com.brainheap.android.ui.login

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
import com.brainheap.android.CredentialsHolder
import com.brainheap.android.R
import com.brainheap.android.model.UserView
import com.brainheap.android.network.RetrofitFactory
import com.brainheap.android.ui.wordsupload.WordsUploadViewModel
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class LoginFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var viewModel: LoginViewModel

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

        emailSetButton.setOnClickListener {
            val email = emailEditText.text.toString()
            Toast.makeText(activity, "Trying to fetch user data", Toast.LENGTH_SHORT).show()

            CoroutineScope(Dispatchers.IO).launch {
                val retrofitService = RetrofitFactory.makeRetrofitService()
                var toastMessage = "Unknown error"
                try {
                    var userId: String? = null
                    val findUserRequest = retrofitService.findUserAsync(email)
                    val findUserResponse = findUserRequest.await()
                    if (findUserResponse.isSuccessful) {
                        userId = findUserResponse.body()?.id
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

                    if (userId != null) {
                        //val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
                        with(sharedPref.edit()) {

                            putString(Constants.NAME_PROP, email)
                            putString(Constants.ID_PROP, userId)
                            commit()
                        }
                        toastMessage = "Found/Registered Id $userId"
                        CredentialsHolder.userId = userId
                        viewModel.loginSuccess.postValue(true)
                    }

                } catch (e: HttpException) {
                    toastMessage = "Exception ${e.message}"

                } catch (e: Throwable) {
                    toastMessage = "Exception ${e.message}"
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if (context is OnFragmentInteractionListener) {
//            listener = context
//        } else {
//            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
//        }
//    }

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
