package com.example.quackapp.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quackapp.Model.Campsite

/**
 * Campsite Detail Screen
 * [US 02.01.04] As a campsite owner, I want to view details of my campsite.
 *
 * TO-DO: figure out how it will work if there is no image...
 * @param navController allows you to move between screens using navigation bar
 * @param campsite knowing which campsite to show details of
 */
@Composable
fun CampsiteDetailScreen (navController: NavController, campsite: Campsite) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //upper row: icon in left corner, back button on right corner
        Row (

        ) {

        }
        //middle col: image, name, location, description, map location, tags, max occupancy, rules
        Column (

        ) {

        }
    }


}