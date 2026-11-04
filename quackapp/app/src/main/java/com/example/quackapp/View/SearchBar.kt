package com.example.quackapp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.quackapp.Model.Campsite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(campsites: List<Campsite>) {

    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val items = campsites.map { it.name }
    val allFiltered = mutableListOf<String>()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        DockedSearchBar(
            modifier = Modifier.align(Alignment.TopStart),
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = { expanded = false },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = { Text("Search campsites...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (expanded) {
                            IconButton(onClick = { expanded = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    }
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ){

            items.filter { it.contains(query, ignoreCase = true) }.forEach { result ->
                allFiltered.add(result)
                /*
                ListItem(
                    headlineContent = { Text(result) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            query = result
                            expanded = false

                        }
                )

                 */
            }


        }

    }
}