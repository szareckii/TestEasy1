package com.zareckii.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.*
import android.util.Patterns.EMAIL_ADDRESS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    private companion object {
        const val INITIAL = 0
        const val PROGRESS = 1
        const val SUCCESS = 2
        const val FAILED = 3
    }

    private var state = INITIAL

    private lateinit var textInputLayout: TextInputLayout
    private lateinit var textInputEditText: TextInputEditText

    private val textWatcher = object : SimpleTextWatcher() {
        override fun afterTextChanged(p0: Editable?) {
            textInputLayout.isErrorEnabled = false
            val input = p0.toString()
            if (input.endsWith("@g"))
                setText("${input}mail.com")
        }
    }

    private fun setText(text: String) {
        textInputEditText.removeTextChangedListener(textWatcher)
        textInputEditText.setTextCorrectly(text)
        textInputEditText.addTextChangedListener(textWatcher)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        savedInstanceState?.let {
            state = it.getInt("screenState")
        }

        textInputLayout = findViewById(R.id.textInputLayout)
        textInputEditText = textInputLayout.editText as TextInputEditText

//        textInputEditText.addTextChangedListener(textWatcher)
//        textInputEditText.listenChanges { textInputLayout.isErrorEnabled = false }

        val fullText = getString(R.string.agreement_full_text)
        val confidential = getString(R.string.confidential_info)
        val policy = getString(R.string.privacy_policy)
        val spannableString = SpannableString(fullText)

        val confidentialClickable = MyClickableSpan {
            Snackbar.make(it, "Go to link1", Snackbar.LENGTH_SHORT).show()
        }

        val policyClickable = MyClickableSpan {
            Snackbar.make(it, "Go to link2", Snackbar.LENGTH_SHORT).show()
        }

        spannableString.setSpan(
            confidentialClickable,
            fullText.indexOf(confidential),
            fullText.indexOf(confidential) + confidential.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            policyClickable,
            fullText.indexOf(policy),
            fullText.indexOf(policy) + policy.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )


        val checkBox = findViewById<CheckBox>(R.id.checkBox)
        val progressBar = findViewById<ProgressBar>(R.id.progressbar)
        val contentLayout = findViewById<ViewGroup>(R.id.contentLayout)

        checkBox.text = spannableString

        //region

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.isEnabled = false
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            loginButton.isEnabled = isChecked
        }
        //endregion

        loginButton.setOnClickListener {
            if (EMAIL_ADDRESS.matcher(textInputEditText.text.toString()).matches()) {
                hideKeyboard(textInputEditText)
                contentLayout.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                state = PROGRESS
                Handler(Looper.getMainLooper()).postDelayed({
                    state = FAILED
                    contentLayout.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    showDialog(contentLayout)
                }, 3000)
            } else {
                textInputLayout.isErrorEnabled = true
                textInputLayout.error = getString(R.string.invalid_email_message)
            }
        }

        when (state) {
            FAILED -> showDialog(contentLayout)
            SUCCESS -> {
                Snackbar.make(loginButton, "Go to postLogin", Snackbar.LENGTH_SHORT).show()
                state = INITIAL
            }
        }

    }

    private fun showDialog(viewGroup: ViewGroup) {
        val dialog = BottomSheetDialog(this)
        val view =
            LayoutInflater.from(this).inflate(R.layout.dialog, viewGroup, false)

        dialog.setCancelable(false)
        view.findViewById<View>(R.id.closeButton).setOnClickListener {
            state = INITIAL
            dialog.dismiss()
        }
        dialog.setContentView(view)
        dialog.show()
    }

    override fun onPause() {
        super.onPause()
        textInputEditText.removeTextChangedListener(textWatcher)
    }

    override fun onResume() {
        super.onResume()
        textInputEditText.addTextChangedListener(textWatcher)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("screenState", state)
    }
}