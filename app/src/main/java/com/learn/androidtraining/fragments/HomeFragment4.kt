package com.learn.androidtraining.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import com.learn.androidtraining.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment4.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment4 : Fragment(R.layout.fragment_home4) {
    private lateinit var backButton: Button
    private lateinit var containerLeft: FrameLayout
    private lateinit var containerRight: FrameLayout

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
        containerLeft = view.findViewById<FrameLayout>(R.id.container_left)
        containerRight = view.findViewById<FrameLayout>(R.id.container_right)
        backButton.setOnClickListener {
            Log.d("gggggg", "Test ${(parentFragment as HomeFragment).childFragmentManager.fragments
                .forEach { fragment -> Log.d("Fragment", "${fragment::class.simpleName}") }}" )
            (parentFragment as HomeFragment).childFragmentManager.popBackStack()
        }

        childFragmentManager.beginTransaction()
            .add(R.id.container_left, NestedFragment1())
            .add(R.id.container_right, NestedFragment2())
            .commit()
    }
}