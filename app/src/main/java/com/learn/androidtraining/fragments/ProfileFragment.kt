package com.learn.androidtraining.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.learn.androidtraining.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private var message: String = "This message is from ProfileFragmentkjkkkjjkkjjkjkkj"


    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("ProfileFragment", "onAttach")
        Toast.makeText(context, "ProfileFragment onAttach", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ProfileFragment", "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("ProfileFragment", "onCreateView")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val textView: TextView = view.findViewById<TextView>(R.id.textTitle)
        textView.text = message
        Log.d("ProfileFragment", "onViewCreated")
    }

    override fun onStart() {
        super.onStart()
        Log.d("ProfileFragment", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("ProfileFragment", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("ProfileFragment", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("ProfileFragment", "onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("ProfileFragment", "onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ProfileFragment", "onDestroy")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("ProfileFragment", "onDetach")
    }
}