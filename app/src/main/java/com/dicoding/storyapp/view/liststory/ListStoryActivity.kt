package com.dicoding.storyapp.view.liststory

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.remote.response.Story
import com.dicoding.storyapp.databinding.ActivityListStoryBinding
import com.dicoding.storyapp.view.account.AccountActivity
import com.dicoding.storyapp.view.adapter.ListStoryAdapter
import com.dicoding.storyapp.view.detailstory.DetailStoryActivity
import com.dicoding.storyapp.view.insertstory.InsertStoryActivity
import com.dicoding.storyapp.view.login.LoginPreferences

class ListStoryActivity : AppCompatActivity() {

    private lateinit var listBinding: ActivityListStoryBinding
    private lateinit var listStoryViewModel: ListStoryViewModel
    private lateinit var preference: LoginPreferences
    private lateinit var adapter: ListStoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listBinding = ActivityListStoryBinding.inflate(layoutInflater)
        setContentView(listBinding.root)

        setupView()

        showLoading(true)

        preference = LoginPreferences(this)

        adapter = ListStoryAdapter()

        goToDetail()

        listStoryViewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        )[ListStoryViewModel::class.java]

        val token: String = preference.getString(LoginPreferences.TOKEN).toString()

        showList(token)

        listBinding.apply {
            rvListstory.layoutManager = LinearLayoutManager(this@ListStoryActivity)
            rvListstory.setHasFixedSize(true)
            rvListstory.adapter = adapter
            showList(token)
        }

        listBinding.fbAdd.setOnClickListener {
            val moveToInsert = Intent(this, InsertStoryActivity::class.java)
            startActivity(moveToInsert)
        }
    }

    private fun getStories(token: String) {
        listStoryViewModel.getStories(token)
    }

    private fun showList(token: String) {
        getStories(token)
        listStoryViewModel.getListStories().observe(this) {
            if (it != null) {
                adapter.setList(it)
                showLoading(false)
            }
        }
    }

    private fun goToDetail() {
        adapter.setOnItemClickCallback(object : ListStoryAdapter.OnItemClickCallback {
            override fun onItemClicked(data: Story) {
                Intent(this@ListStoryActivity, DetailStoryActivity::class.java).also {
                    it.putExtra(DetailStoryActivity.USERNAME, data.name)
                    it.putExtra(DetailStoryActivity.DESCRIPTION, data.description)
                    it.putExtra(DetailStoryActivity.PHOTO, data.photoUrl)
                    it.putExtra(DetailStoryActivity.DATE, data.createdAt)
                    startActivity(it)
                }
            }
        })
    }

    private fun setupView() {
        val actionbar = supportActionBar
        if (actionbar != null) {
            actionbar.title = getString(R.string.list_page)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_account -> {
                val moveToAccount = Intent(this@ListStoryActivity, AccountActivity::class.java)
                startActivity(moveToAccount)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showLoading(isLoading: Boolean) {
        listBinding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

}