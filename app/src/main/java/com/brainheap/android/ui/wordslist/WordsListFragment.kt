package com.brainheap.android.ui.wordslist

import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brainheap.android.Constants
import com.brainheap.android.CredentialsHolder
import com.brainheap.android.R
import com.brainheap.android.ui.login.LoginViewModel
import kotlinx.android.synthetic.main.fragment_words_list.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [WordsListFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [WordsListFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class WordsListFragment : Fragment() {

    private lateinit var viewModel: WordsListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_words_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
        val userId = sharedPref.getString(Constants.ID_PROP, "")
        if (userId.isNullOrEmpty()) {
            findNavController(this).navigate(R.id.action_force_login)
        } else CredentialsHolder.userId = userId

        activity?.let {
            viewModel = ViewModelProviders.of(it).get(WordsListViewModel::class.java)
        }

        word_list_refresh.setOnRefreshListener { viewModel.refresh() }


        val adapter = WordsListAdapter {
            val action = WordsListFragmentDirections.actionViewWordDetail(it.id)
            findNavController(this).navigate(action)
        }
        words_list_recyclerView.layoutManager = LinearLayoutManager(activity).apply {
            orientation = RecyclerView.VERTICAL
        }
        words_list_recyclerView.adapter = adapter

        viewModel.liveDataItemList.observe(this, Observer {
            adapter.loadItems(it ?: emptyList())
            adapter.notifyDataSetChanged()
        })
        viewModel.isRefreshing.observe(this, Observer {
            word_list_refresh.isRefreshing = it })
    }


}
