package com.brainheap.android.ui.wordslist

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brainheap.android.preferences.CredentialsHolder
import com.brainheap.android.ui.wordseditupload.WordsEditUploadActivity
import com.brainheap.android.repository.ItemsListPeriod
import kotlinx.android.synthetic.main.fragment_words_list.*
import com.brainheap.android.ui.ClipboardProcessor
import com.brainheap.android.ui.login.LoginActivity
import com.brainheap.android.R
import com.brainheap.android.ui.wordsupload.WordsUploadActivity

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
        if (CredentialsHolder.userId.isNullOrEmpty()) {
            startActivity(Intent(context, LoginActivity::class.java))
        }

        activity?.let {
            viewModel = ViewModelProviders.of(it).get(WordsListViewModel::class.java)
        }

        words_list_refresh_button.setOnClickListener { viewModel.refresh() }
        words_list_add_button.setOnClickListener {
            ClipboardProcessor(context!!).process()
                ?.let{
                    val intent = Intent(this.context, WordsUploadActivity::class.java)
                    intent.putExtra ("text", it)
                    startActivity(intent)
                }
                ?:let{
                    val intent = Intent(this.context, WordsEditUploadActivity::class.java)
                    intent.putExtra ("title", "title")
                    intent.putExtra ("description", "description" )
                    startActivity(intent)
                }
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

        //attach swipe handler
        val swipeHandler = object : WordsListSwipeCallback(context!!) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val swipeAdapter = words_list_recyclerView.adapter as WordsListAdapter
                viewModel.deleteItem(swipeAdapter.items[viewHolder.adapterPosition].id)

            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(words_list_recyclerView)

        //Observe changes
        viewModel.liveDataItemList.observe(this, Observer {
            adapter.loadItems(it ?: emptyList())
            adapter.notifyDataSetChanged()
        })
        viewModel.isRefreshing.observe(this, Observer {
            word_list_refresh.isRefreshing = it })

        //Setup spinner
        ArrayAdapter.createFromResource(
            activity,
            R.array.items_time_array,
            R.layout.word_list_spinner
        ).also { spinAdapter ->
            spinAdapter.setDropDownViewResource(R.layout.word_list_spinner)
            spinner.adapter = spinAdapter
        }
        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setWordsListPeriod(ItemsListPeriod.values()[position])
            }
        }
        viewModel.period.observe(this, Observer{
            spinner.setSelection(it.ordinal)
        })
    }

}
