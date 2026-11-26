package com.example.quackapp.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quackapp.View.Entrant.EntrantScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserChats (
    navController: NavController,
) {
    var query by remember { mutableStateOf("") }
    val searchBarText = "Search..."
    val searchBarColor = MaterialTheme.colorScheme.outline

    EntrantScaffold(navController) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SearchBar(
                        query = query,
                        whenQueryChanges = { query = it },
                        searchBarText = searchBarText,
                        searchBarColor = searchBarColor,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
            /*
            items(resultingCampsites, key = { it.campId }) { campsite ->
                EntrantHomeCard(
                    campsite = campsite,
                    onClick = { campsiteCardClicked(campsite) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
            */
        }
    }
}