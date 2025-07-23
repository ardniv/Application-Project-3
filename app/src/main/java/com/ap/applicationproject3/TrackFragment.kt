package com.ap.applicationproject3

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class TrackFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var textLastLocation: TextView
    private lateinit var buttonToggle: Button
    private var isTracking = false
    private var currentMarker: Marker? = null
    private var firebaseListener: ValueEventListener? = null
    private lateinit var ref: DatabaseReference
    private var lastKnownPoint: GeoPoint? = null
    private var isNotifSent = false

    data class Lokasi(
        val lat: Double? = null,
        val lng: Double? = null
    )

    data class Sensor(
        val is_gerak: Boolean? = null,
        val lokasi: Lokasi? = null
    )

    data class Motor(
        val sensor: Sensor? = null
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))

        val view = inflater.inflate(R.layout.fragment_track, container, false)

        createNotificationChannel()

        mapView = view.findViewById(R.id.mapView)
        textLastLocation = view.findViewById(R.id.textLastLocation)
        buttonToggle = view.findViewById(R.id.buttonToggle)

        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(19.0)

        ref = FirebaseDatabase.getInstance().getReference("motor001")

        firebaseListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val motor = snapshot.getValue(Motor::class.java)
                val lat = motor?.sensor?.lokasi?.lat
                val lng = motor?.sensor?.lokasi?.lng
                val isGerak = motor?.sensor?.is_gerak == true

                if (lat != null && lng != null) {
                    val newPoint = GeoPoint(lat, lng)

                    if (isTracking && ::mapView.isInitialized) {
                        lastKnownPoint?.let { prev ->
                            val distance = prev.distanceToAsDouble(newPoint)
                            if (distance > 10.0) {
                                Toast.makeText(
                                    requireContext(),
                                    "Lokasi berpindah sejauh ${"%.2f".format(distance)} meter",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d("LOCATION", "Bergeser sejauh $distance meter")

                                if (!isNotifSent) {
                                    showNotification("Peringatan!", "Motor berpindah sejauh ${"%.2f".format(distance)} meter")
                                    isNotifSent = true
                                }
                            }
                        }

                        lastKnownPoint = newPoint

                        mapView.controller.setCenter(newPoint)
                        currentMarker?.let { mapView.overlays.remove(it) }

                        val marker = Marker(mapView)
                        marker.position = newPoint
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = "Lokasi Aktif"
                        mapView.overlays.add(marker)
                        currentMarker = marker
                        mapView.invalidate()

                        textLastLocation.text = "Lokasi Terakhir: $lat, $lng"
                    }

                    // Notifikasi saat motor dinyalakan
                    if (isGerak && !isNotifSent) {
                        showNotification("Halo!", "Selamat datang di Aplikasi Keamanan Sepeda")
                        isNotifSent = true
                    } else if (!isGerak) {
                        isNotifSent = false // reset untuk semua jenis notifikasi
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FIREBASE", "Gagal baca data", error.toException())
            }
        }

        ref.addValueEventListener(firebaseListener!!)

        buttonToggle.setOnClickListener {
            isTracking = !isTracking
            buttonToggle.text = if (isTracking) "Matikan Tracking" else "Aktifkan Tracking"

            if (!isTracking) {
                mapView.overlays.clear()
                currentMarker = null
                lastKnownPoint = null
                mapView.invalidate()
                textLastLocation.text = "Lokasi Terakhir: -"
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        firebaseListener?.let { ref.removeEventListener(it) }
        firebaseListener = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Channel Notifikasi Umum"
            val descriptionText = "Notifikasi dari Aplikasi Keamanan Sepeda"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("channel_id", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, message: String) {
        Log.d("NOTIF", "Mencoba kirim notifikasi: $title - $message")
        val builder = NotificationCompat.Builder(requireContext(), "channel_id")
            .setSmallIcon(R.drawable.ic_info) // ganti dengan icon yang sesuai
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val context = requireContext()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            with(NotificationManagerCompat.from(context)) {
                notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
            }
        } else {
            Log.w("NOTIF", "Izin notifikasi belum diberikan")
        }
    }
}
