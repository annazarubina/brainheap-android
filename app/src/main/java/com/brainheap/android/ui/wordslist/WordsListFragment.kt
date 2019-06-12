package com.brainheap.android.ui.wordslist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brainheap.android.R
import com.brainheap.android.preferences.CredentialsHolder
import com.brainheap.android.repository.ItemsListPeriod
import com.brainheap.android.ui.ClipboardProcessor
import com.brainheap.android.ui.login.LoginActivity
import com.brainheap.android.ui.wordseditupload.WordsEditUploadActivity
import com.brainheap.android.ui.wordsupload.WordsUploadActivity
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_words_list.*

class WordsListFragment : Fragment() {
    private lateinit var viewModel: WordsListViewModel
    private var baseTitle: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_words_list, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            viewModel = ViewModelProviders.of(it).get(WordsListViewModel::class.java)
        }

        baseTitle ?: let { activity?.title.toString() }.takeIf { it.isNotEmpty() }?.let { baseTitle = it }

        words_list_refresh_button.setOnClickListener { viewModel.refresh() }
        words_list_add_button.setOnClickListener {
            ClipboardProcessor(context!!).process()
                ?.let {
                    val intent = Intent(this.context, WordsUploadActivity::class.java)
                    intent.putExtra("text", it)
                    startActivity(intent)
                }
                ?: let {
                    val intent = Intent(this.context, WordsEditUploadActivity::class.java)
                    intent.putExtra("title", "title")
                    intent.putExtra("description", "description")
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

        CredentialsHolder.email.observe(this, Observer { email ->
            activity?.title = email?.takeIf { it.isNotEmpty() }?.let { "$baseTitle ($it)" } ?: baseTitle
        })

        CredentialsHolder.userId.observe(this, Observer {
            viewModel.itemRepositry.syncList(true)
        })

        viewModel.itemRepositry.liveItemsList.observe(this, Observer {
            adapter.loadItems(it ?: emptyList())
            adapter.notifyDataSetChanged()
        })

        viewModel.itemRepositry.isRefreshing.observe(this, Observer {
            word_list_refresh.isRefreshing = it
        })

        viewModel.itemRepositry.period.observe(this, Observer {
            words_list_tab.getTabAt(it.ordinal)?.select()
        })

        words_list_tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.setWordsListPeriod(ItemsListPeriod.values()[tab.position])
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
            override fun onTabUnselected(tab: TabLayout.Tab) {}
        })
    }
}
