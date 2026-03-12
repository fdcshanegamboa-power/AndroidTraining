package com.learn.androidtraining

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sample)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val editText = findViewById<EditText>(R.id.editText)
        val button = findViewById<Button>(R.id.buttonClickMe)
        val urlButton = findViewById<Button>(R.id.urlButton)
        val webView = findViewById<WebView>(R.id.webView)
        button.setOnClickListener {

            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Open Second Activity")
            builder.setMessage("Do you want to open the second activity with the message: \"${editText.text}\"?")
            builder.setPositiveButton("Yes") { dialog, which ->
                val intent = Intent(this, SecondActivity::class.java)
                val bundle = Bundle()

                bundle.putString("message", editText.text.toString())
                intent.putExtras(bundle)
                startActivity(intent)
            }
            builder.setNegativeButton("No"){ dialog, which ->
            }
            builder.show()
        }

        urlButton.setOnClickListener {
            val url = "https://www.google.com/"
            webView.loadUrl(url)
        }
    }
}