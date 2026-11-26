package com.example.quackapp.View.Entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quackapp.Controller.FilterCampsiteController
import com.example.quackapp.Model.Campsite
import com.example.quackapp.Model.Lottery
import com.example.quackapp.View.DateRangePickerField
import com.example.quackapp.View.SearchBar
import com.example.quackapp.View.ValueRangeFilter
import com.example.quackapp.View.todayUtcMillis

/**
 * Displays campsite cards based on filters
 *
 * Looks at if the entrant has adjusted any filters, if so,
 * display results that fit the requirements of the filter.
 *
 * [US 01.01.01] As an entrant, I want to search for campsites.
 * [US 01.01.03] As an entrant, I want to see a list of campsites
 * based on my filters.
 *
 * @param campsites takes in a list of campsites to be displayed
 * @param lotteries takes in a list of lotteries
 * @param campsiteCardClicked allows campsites to be clicked
 * @param modifier allows modifiers to be passed through
 */
@Composable
fun EntrantHomeList(
    campsites: List<Campsite>,
    lotteries: List<Lottery>,
    campsiteCardClicked: (Campsite) -> Unit,
    modifier: Modifier = Modifier
) {

    //Campsite Capacity Range Bar
    val minCapacity = remember(campsites) { campsites.minOfOrNull { it.capacity } ?: 0 }
    val maxCapacity = remember(campsites) { campsites.maxOfOrNull { it.capacity } ?: 0 }
    var capacityRange by remember(minCapacity, maxCapacity) {
        mutableStateOf(minCapacity.toFloat()..maxCapacity.toFloat())
    }

    //Filter by Date
    var startingDate by rememberSaveable { mutableStateOf<Long?>(null) }
    var endingDate by rememberSaveable { mutableStateOf<Long?>(null) }

    //Filter Button
    var filterVisible by remember { mutableStateOf(false) }

    //Location Check Box
    var locationSearchChecked by remember { mutableStateOf(false) }
    val filterColor = if (filterVisible) Color(0xFFF16B00) else Color(0xFFF2BA00)

    //Search Bar
    var query by remember { mutableStateOf("") }
    val searchBarText = if(locationSearchChecked) "Search locations..." else "Search campsites..."
    val searchBarColor = if(locationSearchChecked) Color(0xFFF16B00) else MaterialTheme.colorScheme.outline


    //Gets all Campsites that fit the filter requirements if there are any
    val filterController = remember { FilterCampsiteController() }
    val resultingCampsites = remember(
        query,
        capacityRange,
        locationSearchChecked,
        campsites,
        lotteries,
        startingDate,
        endingDate

    ) {
        filterController.filterCampsites(
            query,
            capacityRange,
            locationSearchChecked,
            campsites,
            lotteries,
            startingDate,
            endingDate
        )
    }
    //Scrollable Column
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            Row(verticalAlignment = Alignment.CenterVertically){
                //Allows users to Search
                //query is what the user has entered in the search bar
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

        //Filters
        if (filterVisible) {
            item{
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.displaySmall
                    )

                    //Button that resets all fields
                    Button(
                        onClick = {
                            startingDate = null
                            endingDate = null
                            capacityRange = minCapacity.toFloat()..maxCapacity.toFloat()
                            locationSearchChecked = false
                            query = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2BA00))


                    ) {
                        Text("Reset")
                    }

                }
            }
            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //Search by Location check box
                    Text("Search By Location",
                        modifier = Modifier.testTag("searchByLocation"))
                    Checkbox(
                        checked = locationSearchChecked,
                        onCheckedChange = { locationSearchChecked = it }
                    )
                }


            }
            item {
                    Text(
                        text = "Available Dates",
                        style = MaterialTheme.typography.titleMedium
                    )
                    //Filter campsites by their availability
                    DateRangePickerField(
                        label = "Enter Dates",
                        startMillis = startingDate,
                        endMillis = endingDate,
                        onRangeSelected = { s, e -> startingDate = s; endingDate = e },
                        minDateMillis = todayUtcMillis()
                    )

            }


            if (maxCapacity > minCapacity) {
                item(key = "capacityRange") {
                    //Filter by Campsite Capacity
                    ValueRangeFilter(
                        label = "Capacity",
                        range = capacityRange,
                        onRangeChange = { capacityRange = it },
                        valueRange = minCapacity.toFloat()..maxCapacity.toFloat(),
                        steps = maxCapacity - minCapacity - 1
                    )
                }
            }

        }


        item {
            Text("Showing ${resultingCampsites.size} of ${campsites.size} campsites.",)
        }

        //Displays Campsite Cards
        items(resultingCampsites, key = { it.campId }) { campsite ->
            EntrantHomeCard(
                campsite = campsite,
                onClick = { campsiteCardClicked(campsite) }
            )
        }
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }

    }
}


