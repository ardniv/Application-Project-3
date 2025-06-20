package com.ap.applicationproject3

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView

class SettingFragment : Fragment() {

    private lateinit var cardInfoApp: CardView
    private lateinit var cardLogout: CardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        cardInfoApp = view.findViewById(R.id.cardInfoApp)
        cardLogout = view.findViewById(R.id.cardLogout)

        cardInfoApp.setOnClickListener {
            // Tampilkan dialog info aplikasi
            AlertDialog.Builder(requireContext())
                .setTitle("Info Aplikasi")
                .setMessage("Aplikasi Keamanan Sepeda\nVersi 1.0\nDibuat oleh Vin Prm.")
                .setPositiveButton("OK", null)
                .show()
        }

        cardLogout.setOnClickListener {
            // Konfirmasi keluar aplikasi
            AlertDialog.Builder(requireContext())
                .setTitle("Keluar Aplikasi")
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Ya") { _, _ ->
                    requireActivity().finishAffinity() // Tutup semua activity
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        return view
    }
}
