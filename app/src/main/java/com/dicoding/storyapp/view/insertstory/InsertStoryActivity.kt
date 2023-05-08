package com.dicoding.storyapp.view.insertstory

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.Result
import com.dicoding.storyapp.databinding.ActivityInsertStoryBinding
import com.dicoding.storyapp.utils.createCustomTempFile
import com.dicoding.storyapp.utils.reduceFileImage
import com.dicoding.storyapp.utils.uriToFile
import com.dicoding.storyapp.view.ViewModelFactory
import com.dicoding.storyapp.view.liststory.ListStoryActivity
import com.dicoding.storyapp.view.login.LoginPreferences
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class InsertStoryActivity : AppCompatActivity() {

    private lateinit var insertBinding: ActivityInsertStoryBinding
    private lateinit var currentPhotoPath: String
    private lateinit var insertStoryViewModel: InsertStoryViewModel
    private var getFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        insertBinding = ActivityInsertStoryBinding.inflate(layoutInflater)
        setContentView(insertBinding.root)
        insertStoryViewModel = obtainViewModel(this as AppCompatActivity)

        showLoading(false)
        setupView()
        setupAction()

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        insertBinding.cameraButton.setOnClickListener { startTakePhoto() }
        insertBinding.galleryButton.setOnClickListener { startGallery() }
    }

    private fun obtainViewModel(activity: AppCompatActivity): InsertStoryViewModel {
        val loginPreferences = LoginPreferences(activity.application)
        val factory = ViewModelFactory.getInstance(activity.application, loginPreferences)
        return ViewModelProvider(activity, factory)[InsertStoryViewModel::class.java]
    }

    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)
        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@InsertStoryActivity,
                "com.dicoding.storyapp",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            getFile = myFile
            val result = BitmapFactory.decodeFile(myFile.path)

            insertBinding.previewImageView.setImageBitmap(result)
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this@InsertStoryActivity)
            getFile = myFile
            insertBinding.previewImageView.setImageURI(selectedImg)
        }
    }

    private fun setupAction() {
        insertBinding.uploadButton.setOnClickListener {
            if (insertBinding.edAddDescription.text.isNullOrEmpty()) {
                showToast(getString(R.string.desc_require))
            } else {
                val description = insertBinding.edAddDescription.text.toString()
                if (!TextUtils.isEmpty(description) && getFile != null) {
                    lifecycleScope.launch {
                        postStory(description)
                    }
                } else {
                    showAlert(
                        getString(R.string.post_fail_1),
                        getString(R.string.post_fail)
                    )
                    { }
                }
            }
        }
    }

    private fun postStory(description: String) {
        val image = convertImage()
        val desc = convertDescription(description)
        insertStoryViewModel.insertStory(
            image,
            desc
        ).observe(this@InsertStoryActivity) { result ->
            if (result != null) {
                when (result) {
                    is Result.Loading -> {
                        showLoading(true)
                    }
                    is Result.Error -> {
                        showLoading(false)
                        showAlert(
                            getString(R.string.post_fail_1),
                            getString(R.string.post_fail_2)
                        )
                        { }
                    }
                    is Result.Success -> {
                        showLoading(false)
                        postSuccess()
                    }
                }
            }
        }
    }

    private fun postSuccess() {
        showAlert(getString(R.string.post_success), getString(R.string.post_success_1))
        { navigateToList() }
        insertBinding.previewImageView.setImageResource(R.drawable.ic_place_holder)
        insertBinding.edAddDescription.text?.clear()
    }

    private fun convertImage(): MultipartBody.Part {
        val file = reduceFileImage(getFile as File)
        val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())

        return MultipartBody.Part.createFormData(
            "photo",
            file.name,
            requestImageFile
        )
    }

    private fun convertDescription(description: String): RequestBody {
        return description.toRequestBody("text/plain".toMediaType())
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun setupView() {
        val actionbar = supportActionBar
        if (actionbar != null) {
            actionbar.title = getString(R.string.add_page)
            actionbar.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun showLoading(isLoading: Boolean) {
        insertBinding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    fun showAlert(
        title: String,
        message: String,
        positiveAction: (dialog: DialogInterface) -> Unit
    ) {
        AlertDialog.Builder(this).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("OK") { dialog, _ ->
                positiveAction.invoke(dialog)
            }
            setCancelable(false)
            create()
            show()
        }
    }

    private fun navigateToList() {
        val intent = Intent(this@InsertStoryActivity, ListStoryActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    private fun showToast(text: String) {
        Toast.makeText(
            this,
            text,
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

}