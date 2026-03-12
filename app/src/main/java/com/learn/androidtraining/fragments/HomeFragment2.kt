package com.learn.androidtraining.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.learn.androidtraining.MainActivity
import com.learn.androidtraining.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment2.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment2 : Fragment(R.layout.fragment_home2) {

    private lateinit var backButton: Button
    private lateinit var nextButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backButton = view.findViewById<Button>(R.id.button_back)
        nextButton = view.findViewById<Button>(R.id.button_go_home3)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        nextButton.setOnClickListener {
            (activity as MainActivity).navigateTo(HomeFragment3())
        }
    }

}