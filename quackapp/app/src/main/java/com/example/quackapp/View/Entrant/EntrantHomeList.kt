package com.example.quackapp.View.Entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.quackapp.Model.Campsite
import com.example.quackapp.View.SearchBar
import com.example.quackapp.View.ValueRangeFilter

//TODO Add Comments + Tests
@Composable
fun EntrantHomeList(
    campsites: List<Campsite>,
    campsiteCardClicked: (Campsite) -> Unit,
    modifier: Modifier = Modifier
) {

    //Campsite Capacity Range Bar
    val minCapacity = remember(campsites) { campsites.minOfOrNull { it.capacity } ?: 0 }
    val maxCapacity = remember(campsites) { campsites.maxOfOrNull { it.capacity } ?: 0 }
    var capacityRange by remember(minCapacity, maxCapacity) {
        mutableStateOf(minCapacity.toFloat()..maxCapacity.toFloat())
    }

    //Filter Button
    var filterVisible by remember { mutableStateOf(false) }

    //Location Check Box
    var locationSearchChecked by remember { mutableStateOf(false) }
    var filterColor = if (filterVisible) Color(0xFFF16B00) else Color(0xFFF2BA00)

    //Search Bar
    var query by remember { mutableStateOf("") }
    val searchBarText = if(locationSearchChecked) "Search locations..." else "Search campsites..."
    val searchBarColor = if(locationSearchChecked) Color(0xFFF16B00) else MaterialTheme.colorScheme.outline

    //Displays Campsites that fit the filter requirements if there are any
    val resultingCampsites = remember(query, capacityRange, locationSearchChecked, campsites) {
        campsites.filter { campsite ->
            val matchesQuery = when {
                //If search bar is empty, matchesQuery is True
                query.isBlank() -> true
                //If search by location is on, only display based on location
                locationSearchChecked -> campsite.location.contains(query, ignoreCase = true)
                //If search by location is off, only display based on campsite name
                else -> campsite.name.contains(query, ignoreCase = true)
            }

            //Checks if campsite is within capacity range
            val matchesCapacity = campsite.capacity.toFloat() in capacityRange

            //If campsite is within capacity range and is valid based on the filters
            //Then the campsite is approved and can be displayed
            matchesQuery && matchesCapacity
        }
    }

    //Scrollable Column
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            Row(verticalAlignment = Alignment.CenterVertically){
                //Allows users to Search
                SearchBar(
                    query = query,
                    whenQueryChanges = { query = it },
                    searchBarText = searchBarText,
                    searchBarColor = searchBarColor,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))

                //Filter Button
                Button(
                    onClick = {
                        filterVisible = !filterVisible
                    },

                    colors = ButtonDefaults.buttonColors(containerColor = filterColor)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = "Filter"
                    )
                }
            }
        }
        item {
            if (filterVisible) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //Search by Location
                    Text("Search By Location")
                    Checkbox(
                        checked = locationSearchChecked,
                        onCheckedChange = { locationSearchChecked = it }
                    )
                }
            }

        }
        if (maxCapacity > minCapacity && filterVisible) {
            item(key = "capacityRange") {
                //Filter by Campsite Capacity
                ValueRangeFilter(
                    label = "Campsite Capacity",
                    range = capacityRange,
                    onRangeChange = { capacityRange = it },
                    valueRange = minCapacity.toFloat()..maxCapacity.toFloat(),
                    steps = maxCapacity - minCapacity - 1
                )
            }
        }
        item {
            Text("Showing ${resultingCampsites.size} of ${campsites.size} campsites.")
        }

        //Displays Campsite Cards
        items(resultingCampsites, key = { it.campId }) { campsite ->
            EntrantHomeCard(
                campsite = campsite,
                onClick = { campsiteCardClicked(campsite) }
            )
        }

    }
}


