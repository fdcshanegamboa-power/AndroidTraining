package com.learn.androidtraining.fragments.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.learn.androidtraining.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment3.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment3 : Fragment(R.layout.fragment_home3) {


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
        nextButton = view.findViewById<Button>(R.id.button_go_home4)
        backButton.setOnClickListener {
            (parentFragment as HomeFragment).childFragmentManager.popBackStack()
        }
        nextButton.setOnClickListener {
            (parentFragment as HomeFragment).navigateTo(HomeFragment4())
        }
    }
}