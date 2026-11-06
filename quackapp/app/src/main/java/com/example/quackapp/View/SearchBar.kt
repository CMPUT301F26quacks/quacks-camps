package com.example.quackapp.View

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

//TODO Tests

/**
 * Search Bar
 *
 * @param query the query is what is inside the search bar
 * @param whenQueryChanges whenever a change happens in the text in the search bar
 * it is alerted
 * @param searchBarText the text inside the search bar when it is empty
 * @param searchBarColor the border color of the search bar
 * @param modifier takes in modifiers if needed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    whenQueryChanges: (String) -> Unit,
    searchBarText: String,
    searchBarColor: Color,
    modifier: Modifier = Modifier
    ) {
    //Acts as our search bar
    OutlinedTextField(
        value = query,
        onValueChange = whenQueryChanges,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = searchBarColor,
            unfocusedBorderColor = searchBarColor
        ),
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        label = { Text(searchBarText) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            //When someone types something in the search bar, show the x icon
            if (query.isNotEmpty()) {
                IconButton(onClick = { whenQueryChanges("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                }
            }
        }
    )
}