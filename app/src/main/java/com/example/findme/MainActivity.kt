package com.example.findme

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.findme.ui.theme.FindMeTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

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
    Column (modifier = modifier) {
        val context = LocalContext.current

        val locationPermissionsState = rememberMultiplePermissionsState(
            // add the permissions you want here
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )

        if (locationPermissionsState.allPermissionsGranted) {
            Column {
                Text(text = "All location permissions granted!")

                // we can check for locations now
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val cameraPositionState = rememberCameraPositionState {}

                fusedLocationClient.lastLocation.addOnSuccessListener {
                    Log.d("pos", it.toString())
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(it.latitude, it.longitude), 20f)
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                )
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