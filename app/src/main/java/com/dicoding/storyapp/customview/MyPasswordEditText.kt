package com.dicoding.storyapp.customview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.dicoding.storyapp.R
import com.dicoding.storyapp.utils.passwordValidation

class MyPasswordEditText : AppCompatEditText {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        this@MyPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val inputText = s.toString()
                if (inputText.isEmpty()) {
                    this@MyPasswordEditText.setBackgroundResource(R.drawable.tv_border_black)
                } else {
                    if (passwordValidation(inputText)) {
                        this@MyPasswordEditText.setBackgroundResource(R.drawable.tv_border_black)
                    } else {
                        this@MyPasswordEditText.setBackgroundResource(R.drawable.tv_border_red)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

}