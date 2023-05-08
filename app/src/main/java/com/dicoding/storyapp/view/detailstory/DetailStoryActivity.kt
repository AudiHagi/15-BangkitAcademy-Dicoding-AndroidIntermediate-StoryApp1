package com.dicoding.storyapp.view.detailstory

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.storyapp.R
import com.dicoding.storyapp.databinding.ActivityDetailStoryBinding
import com.dicoding.storyapp.utils.formatDate
import java.util.*

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var detailBinding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detailBinding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(detailBinding.root)
        setupView()

        val username = intent.getStringExtra(USERNAME)
        val desc = intent.getStringExtra(DESCRIPTION)
        val photo = intent.getStringExtra(PHOTO)
        val date = intent.getStringExtra(DATE)
        val formattedDate = formatDate(date!!, TimeZone.getDefault().id)
        detailBinding.apply {
            Glide.with(this@DetailStoryActivity)
                .load(photo)
                .into(storyImages)
            storyName.text = username
            storyDesc.text = desc
            storyDate.text = formattedDate
        }

        detailBinding.fbShare.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.extra_subject))
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Dipost oleh $username pada $formattedDate\n\nDengan deskripsi :\n$desc"
                )
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
    }

    private fun setupView() {
        val actionbar = supportActionBar
        actionbar?.title = getString(R.string.detail_page)
        actionbar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        const val USERNAME = "name"
        const val DESCRIPTION = "desc"
        const val PHOTO = "photo"
        const val DATE = "date"
    }

}