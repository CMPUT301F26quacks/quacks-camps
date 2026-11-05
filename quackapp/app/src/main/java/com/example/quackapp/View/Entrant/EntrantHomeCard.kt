package com.example.quackapp.View.Entrant

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.quackapp.Model.Campsite
import com.example.quackapp.Model.sampleCampsites

//Typography stuff
//https://developer.android.com/develop/ui/compose/designsystems/material3#typography

//TODO Add Comments + Tests
@Composable
fun EntrantHomeCard(
    campsite: Campsite,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    //Campsite Card Template
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF248AFF),
            contentColor = Color.White,
        ),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = campsite.name,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "\u2B50 ${campsite.rating} | $${campsite.price} per night",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                text = "\uD83D\uDCCC ${campsite.location} | Max ${campsite.capacity} people",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 6.dp)
            )

        }
    }
}

//TO BE DELETED once Firebase is connected
//Sets up Sample Cards
@Preview(showBackground = true)
@Composable
private fun CampsiteCardPreview() {
    MaterialTheme {
        EntrantHomeCard(
            campsite = sampleCampsites.first(),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}