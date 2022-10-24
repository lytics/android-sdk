package com.lytics.android.demo.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.lytics.android.Lytics
import com.lytics.android.demo.R
import com.lytics.android.demo.databinding.FragmentLoginBinding
import com.lytics.android.events.LyticsIdentityEvent

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        Lytics.currentUser?.let {
            val email: String = (it.identifiers?.get("email") as? String) ?: ""
            binding.emailTextField.editText?.setText(email)
        }


        binding.loginButton.setOnClickListener {
            val email = binding.emailTextField.editText?.text.toString()
            Lytics.identify(LyticsIdentityEvent(name="login", identifiers = mapOf("email" to email)))

            Toast.makeText(requireContext(), R.string.logged_in, Toast.LENGTH_SHORT).show()

            findNavController().navigate(R.id.navigation_events)
        }

        binding.registerLink.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}