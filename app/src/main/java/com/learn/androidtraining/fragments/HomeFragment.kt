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
import com.learn.androidtraining.R
import com.learn.androidtraining.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
//    private var message: String = "This message is from HomeFragment"

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val tag: String = "HomeFragment"

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
    ): View {
        Log.d(tag, "onCreateView - inflating layout")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonGoHome2.setOnClickListener {
            navigateTo(HomeFragment2())
        }
        childFragmentManager.addOnBackStackChangedListener {
            val hasChildren = childFragmentManager.backStackEntryCount > 0
            binding.buttonGoHome2.visibility = if (hasChildren) View.GONE else View.VISIBLE
        }

//        val textView: TextView = view.findViewById<TextView>(R.id.textTitle)
//        textView.text = message
        Log.d(tag, "onViewCreated")
    }
    fun navigateTo(fragment: Fragment) {
        val transaction = childFragmentManager.beginTransaction()
        val current = childFragmentManager.fragments.lastOrNull()
        if (current != null) transaction.hide(current)
        transaction
            .add(binding.homeChildContainer.id, fragment)
            .addToBackStack(null)
            .commit()
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
        _binding = null
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