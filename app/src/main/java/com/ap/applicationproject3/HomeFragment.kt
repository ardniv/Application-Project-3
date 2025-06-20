package com.ap.applicationproject3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    private lateinit var imageVehicle: ImageView
    private lateinit var textVehicleName: TextView
    private lateinit var textLastLocation: TextView
    private lateinit var textStatus: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        imageVehicle = view.findViewById(R.id.imageVehicle)
        textVehicleName = view.findViewById(R.id.textVehicleName)
        textLastLocation = view.findViewById(R.id.textLastLocation)
        textStatus = view.findViewById(R.id.textStatus)

        // Simulasi data
        val vehicleName = "Yamaha NMax 2024"
        val lastLocation = "-"
        val status = "Aman"

        // Set data ke view
        textVehicleName.text = "Nama Kendaraan: $vehicleName"
        textLastLocation.text = "Lokasi Terakhir: $lastLocation"
        textStatus.text = "Status: $status"

        // Gambar (misal pakai drawable)
        imageVehicle.setImageResource(R.drawable.speda)

        return view
    }
}
