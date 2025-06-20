package com.ap.applicationproject3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class TrackFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var textLastLocation: TextView
    private lateinit var buttonToggle: Button
    private var isTracking = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = requireContext()
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))

        val view = inflater.inflate(R.layout.fragment_track, container, false)
        mapView = view.findViewById(R.id.mapView)
        textLastLocation = view.findViewById(R.id.textLastLocation)
        buttonToggle = view.findViewById(R.id.buttonToggle)

        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)

        val startPoint = GeoPoint(-6.200000, 106.816666) // Jakarta Dummy
        mapView.controller.setCenter(startPoint)

        buttonToggle.setOnClickListener {
            isTracking = !isTracking
            if (isTracking) {
                buttonToggle.text = "Matikan Tracking"

                // Tambahkan marker
                val marker = Marker(mapView)
                marker.position = startPoint
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = "Lokasi Aktif"
                mapView.overlays.add(marker)
                mapView.invalidate()

                textLastLocation.text = "Lokasi Terakhir: ${startPoint.latitude}, ${startPoint.longitude}"
            } else {
                buttonToggle.text = "Aktifkan Tracking"
                mapView.overlays.clear() // Hapus semua marker
                mapView.invalidate()

                textLastLocation.text = "Lokasi Terakhir: -"
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
