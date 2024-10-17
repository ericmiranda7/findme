package com.example.findme

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.findme.ui.theme.FindMeTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FindMeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Map(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Map(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        val context = LocalContext.current

        val locationPermissionsState = rememberMultiplePermissionsState(
            // add the permissions you want here
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            )
        )

        if (locationPermissionsState.allPermissionsGranted) {
            Column {
                Text(text = "All location permissions granted!")

                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val cameraPositionState = rememberCameraPositionState()
                val locationMarkerState = rememberMarkerState()
                Log.d("defaults", "${cameraPositionState.position}, $locationMarkerState")

                // this time, let's subscribe for location updates
                val locationRequest = LocationRequest
                    .Builder(TimeUnit.SECONDS.toMillis(10))
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationUpdate: LocationResult) {
                        super.onLocationResult(locationUpdate)
                        Log.d("location update", locationUpdate.lastLocation.toString())
                        val updatedLatLng = LatLng(
                            locationUpdate.lastLocation!!.latitude,
                            locationUpdate.lastLocation!!.longitude
                        )
                        cameraPositionState.position =
                            CameraPosition.fromLatLngZoom(updatedLatLng, 15f)
                        locationMarkerState.position = updatedLatLng
                    }
                }

                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)

                GoogleMap(
                    modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = locationMarkerState,
                        title = "Current location",
                        snippet = "Marker of current location"
                    )
                }
            }
        } else {
            val allPermissionsRevoked =
                locationPermissionsState.permissions.size == locationPermissionsState.revokedPermissions.size

            val textToShow = if (!allPermissionsRevoked) {
                // if user accepts only coarse, only 1 permission will show up
                "Coarse location granted"
            } else if (locationPermissionsState.shouldShowRationale) {
                // Both permissions denied
                "Explain your rationale for asking permissions"
            } else {
                // First time the user sees this feature or the user doesn't want to be asked again
                "This feature requires location permissions"
            }

            val buttonText = if (!allPermissionsRevoked) {
                "Allow precise location"
            } else {
                "Request permissions"
            }

            Text(text = textToShow)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { locationPermissionsState.launchMultiplePermissionRequest() }) {
                Text(buttonText)
            }
        }


    }
}