package com.example.quackapp.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CheckboxDefaults.colors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.quackapp.Model.Campsite
import com.example.quackapp.Model.sampleCampsites

@Composable
fun EntrantHomeList(
    campsites: List<Campsite>,
    campsiteCardClicked: (Campsite) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically){
                Box(modifier = Modifier.weight(1f)) {
                    SearchBar(sampleCampsites)
                }
                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF2BA00)
                ))  {

                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = "Filter"
                    )
                }
            }
        }

        items(campsites, key = { it.id }) { campsite ->
            EntrantHomeCard(
                campsite = campsite,
                onClick = { campsiteCardClicked(campsite) }
            )
        }

    }
}


@Preview(showBackground = true)
@Composable
private fun CampsiteListPreview() {
    MaterialTheme {
        EntrantHomeList(
            campsites = sampleCampsites,
            campsiteCardClicked = {}
        )
    }
}