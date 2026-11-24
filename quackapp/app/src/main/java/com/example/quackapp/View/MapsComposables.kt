package com.example.quackapp.View

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun MapPreviewCard(
    location: LatLng,
    title: String,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 15f)
    }
    GoogleMap(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(12.dp)),
        cameraPositionState = cameraPositionState,
        // Hide default map toolbar buttons (directions/open in Maps) if desired
        uiSettings = MapUiSettings(
            scrollGesturesEnabled = false,
            zoomGesturesEnabled = false
        ),
        // Intercept taps on the preview to open a full-screen map or details screen
        onMapClick = { onMapClick() }
    ) {
        Marker(
            state = MarkerState(position = location),
            title = title
        )
    }
}

@Composable
fun LocationPicker(onConfirm: (LatLng) -> Unit, startLoc: LatLng?) {
    var selected by remember { mutableStateOf<LatLng?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState {
        if (startLoc == null) {
            // Default Edmonton
            position = CameraPosition.fromLatLngZoom(LatLng(53.5461, -113.4938), 11f)
        }
        else{
            position = CameraPosition.fromLatLngZoom(startLoc, 11f)
        }
    }
//    val context = LocalContext.current
//    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
//    var hasPermission by remember {
//        mutableStateOf(
//            ContextCompat.checkSelfPermission(
//                context, Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED ||
//                    ContextCompat.checkSelfPermission(
//                        context, Manifest.permission.ACCESS_COARSE_LOCATION
//                    ) == PackageManager.PERMISSION_GRANTED
//        )
//    }
//
//    val permissionLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()
//    ) { results ->
//        hasPermission = results.values.any { it }
//    }
//
//    // Ask for location when the picker appears
//    LaunchedEffect(Unit) {
//        if (!hasPermission) {
//            permissionLauncher.launch(
//                arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                )
//            )
//        }
//    }
//
//    // Once granted, fetch the location and move the camera
//    LaunchedEffect(hasPermission) {
//        Log.d("LocPicker", "hasPermission = $hasPermission")
//        if (hasPermission) {
//            try {
//                val location = fusedClient
//                    .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
//                    .await()
//                    ?: fusedClient.lastLocation.await()   // fallback
//                Log.d("LocPicker", "location = $location")
//                location?.let {
//                    if (startLoc == null) {
//                        cameraPositionState.move(
//                            CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
//                        )
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("LocPicker", "location failed", e)
//            }
//        }
//    }

    Box(modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
        .clip(RoundedCornerShape(12.dp)),
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                selected = latLng
                scope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                        durationMs = 500
                    )
                }
            }
        ) {
            startLoc?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Old location"
                )
            }
            selected?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Selected location"
                )
            }
        }

        selected?.let { loc ->
            Button(
                onClick = {
                    onConfirm(loc)
                    scope.launch { snackbarHostState.showSnackbar("Location saved") }
                },
                colors = ButtonDefaults.buttonColors(Color(0xFFFF9800)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)

            ) {
                Text("Confirm (${"%.4f".format(loc.latitude)}, ${"%.4f".format(loc.longitude)})")
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
//                .align(Alignment.TopCenter)
                .padding(8.dp)
        )
    }

}