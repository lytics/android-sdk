package com.lytics.android.demo.ui.account

import android.app.AlertDialog
import android.content.DialogInterface
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
import com.lytics.android.demo.databinding.FragmentRegisterBinding
import com.lytics.android.events.LyticsConsentEvent
import com.lytics.android.events.LyticsIdentityEvent

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: RegisterViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        Lytics.currentUser?.let {
            val name: String = it.attributes?.get("name") as? String ?: ""
            binding.nameTextField.editText?.setText(name)
            val email: String = it.identifiers?.get("email") as? String ?: ""
            binding.emailTextField.editText?.setText(email)

            val tac: Boolean = it.consent?.get("tac") as? Boolean ?: false
            binding.privacyPolicyCheckbox.isChecked = tac
            val idfa: Boolean = it.consent?.get("idfa") as? Boolean ?: false
            binding.idfaCheckbox.isChecked = idfa
        }

        binding.registerButton.setOnClickListener {
            val email = binding.emailTextField.editText?.text.toString()
            val name = binding.nameTextField.editText?.text.toString()
            Lytics.identify(
                LyticsIdentityEvent(
                    name = "login",
                    identifiers = mapOf("email" to email),
                    attributes = mapOf("name" to name)
                )
            )

            val termsAndConditions = binding.privacyPolicyCheckbox.isChecked
            Lytics.consent(LyticsConsentEvent(name = "android consent", consent = mapOf("tac" to termsAndConditions)))

            val idfa = binding.idfaCheckbox.isChecked
            Lytics.consent(LyticsConsentEvent(name = "android consent", consent = mapOf("idfa" to idfa)))

            Toast.makeText(requireContext(), R.string.signed_up, Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.navigation_events)
        }

        binding.idfaCheckbox.setOnClickListener {
            val idfa = binding.idfaCheckbox.isChecked
            if (idfa) {
                val builder = AlertDialog.Builder(requireContext())
                builder.apply {
                    setMessage(R.string.idfa_prompt)
                    setPositiveButton(R.string.allow_tracking) { _, _ ->
                        Lytics.enableIDFA()
                    }
                    setNegativeButton(R.string.do_no_track) { _, _ ->
                        binding.idfaCheckbox.isChecked = false
                    }
                }
                builder.create().show()
            } else {
                Lytics.disableIDFA()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}