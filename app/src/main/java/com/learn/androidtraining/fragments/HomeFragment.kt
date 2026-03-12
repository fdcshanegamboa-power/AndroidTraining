package com.learn.androidtraining.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.learn.androidtraining.MainActivity
import com.learn.androidtraining.R

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var message: String = "This message is from HomeFragment"
    private val tag: String = "HomeFragment"
    private lateinit var goToHome2Button: Button

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(tag, "onAttach")
        Toast.makeText(context, "HomeFragment onAttach", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "onCreateView")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        goToHome2Button = view.findViewById<Button>(R.id.button_go_home2)

        goToHome2Button.setOnClickListener {
            (activity as MainActivity).navigateTo(HomeFragment2())
        }

//        val textView: TextView = view.findViewById<TextView>(R.id.textTitle)
//        textView.text = message
        Log.d(tag, "onViewCreated")
    }

    override fun onStart() {
        super.onStart()
        Log.d(tag, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(tag, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(tag, "onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(tag, "onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(tag, "onDetach")
    }
}