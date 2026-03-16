package com.learn.androidtraining.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.learn.androidtraining.LoginActivity
import com.learn.androidtraining.R
import com.learn.androidtraining.auth.AuthViewModel
import com.learn.androidtraining.databinding.FragmentSettingsBinding
import org.w3c.dom.Text

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private var message: String = "This message is from SettingsFragment"
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel = AuthViewModel()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("SettingsFragment", "onAttach")
        Toast.makeText(context, "SettingsFragment onAttach", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SettingsFragment", "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("SettingsFragment", "onCreateView")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textTitle.text = message
        binding.buttonLogout.setOnClickListener {
            viewModel.logout()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        Log.d("SettingsFragment", "onViewCreated")
    }

    override fun onStart() {
        super.onStart()
        Log.d("SettingsFragment", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("SettingsFragment", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("SettingsFragment", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("SettingsFragment", "onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("SettingsFragment", "onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("SettingsFragment", "onDestroy")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("SettingsFragment", "onDetach")
    }
}