package com.example.quackapp.View

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

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
fun LocationPicker(onConfirm: (LatLng) -> Unit) {
    var selected by remember { mutableStateOf<LatLng?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(53.5461, -113.4938), 11f)
    }

    Box(modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)) {
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