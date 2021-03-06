package ltd.evilcorp.atox.ui.addcontact

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.FragmentAddContactBinding
import ltd.evilcorp.atox.ui.BaseFragment
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.domain.tox.ToxID
import ltd.evilcorp.domain.tox.ToxIdValidator

private const val REQUEST_CODE_SCAN_QR = 6100

class AddContactFragment : BaseFragment<FragmentAddContactBinding>(FragmentAddContactBinding::inflate) {
    private val viewModel: AddContactViewModel by viewModels { vmFactory }

    private var toxIdValid: Boolean = false
    private var messageValid: Boolean = true

    private var contacts: List<Contact> = listOf()

    private fun isAddAllowed(): Boolean = toxIdValid && messageValid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!viewModel.isToxRunning() && !viewModel.tryLoadTox()) findNavController().navigateUp()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, compat ->
            val insets = compat.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            toolbar.updatePadding(left = insets.left, top = insets.top)
            content.updatePadding(left = insets.left, right = insets.right)
            compat
        }

        viewModel.contacts.observe(viewLifecycleOwner) {
            contacts = it
        }

        toolbar.setNavigationIcon(R.drawable.back)
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        toxId.doAfterTextChanged { s ->
            val input = ToxID(s?.toString() ?: "")
            toxId.error = when (ToxIdValidator.validate(input)) {
                ToxIdValidator.Result.INCORRECT_LENGTH -> getString(
                    R.string.tox_id_error_length,
                    s?.toString()?.length ?: 0
                )
                ToxIdValidator.Result.INVALID_CHECKSUM -> getString(R.string.tox_id_error_checksum)
                ToxIdValidator.Result.NOT_HEX -> getString(R.string.tox_id_error_hex)
                ToxIdValidator.Result.NO_ERROR -> null
            }

            if (input == viewModel.toxId) {
                toxId.error = getString(R.string.tox_id_error_self_add)
            }

            if (toxId.error == null) {
                if (contacts.find { it.publicKey == input.toPublicKey().string() } != null) {
                    toxId.error = getString(R.string.tox_id_error_already_exists)
                }
            }

            toxIdValid = toxId.error == null
            add.isEnabled = isAddAllowed()
        }

        message.doAfterTextChanged { s ->
            val content = s?.toString() ?: ""
            message.error = if (content.isNotEmpty())
                null
            else
                getString(R.string.add_contact_message_error_empty)

            messageValid = message.error == null
            add.isEnabled = isAddAllowed()
        }

        add.setOnClickListener {
            viewModel.addContact(ToxID(toxId.text.toString()), message.text.toString())
            findNavController().navigateUp()
        }

        if (requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            readQr.setOnClickListener {
                try {
                    val intent = Intent("com.google.zxing.client.android.SCAN").apply {
                        putExtra("SCAN_FORMATS", "QR_CODE")
                        putExtra("SCAN_ORIENTATION_LOCKED", false)
                        putExtra("BEEP_ENABLED", false)
                    }
                    startActivityForResult(intent, REQUEST_CODE_SCAN_QR)
                } catch (e: ActivityNotFoundException) {
                    val uri = Uri.parse("https://f-droid.org/en/packages/com.google.zxing.client.android/")
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            }
        } else {
            readQr.visibility = View.GONE
        }

        add.isEnabled = false

        toxId.setText(arguments?.getString("toxId"), TextView.BufferType.EDITABLE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQUEST_CODE_SCAN_QR || resultCode != RESULT_OK) return
        val toxId = data?.getStringExtra("SCAN_RESULT") ?: return
        binding.toxId.setText(toxId.removePrefix("tox:"))
    }
}
