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

//TODO Add comments + Tests
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    whenQueryChanges: (String) -> Unit,
    searchBarText: String,
    searchBarColor: Color,
    modifier: Modifier = Modifier
    ) {
    OutlinedTextField(
        value = query,
        onValueChange = whenQueryChanges,
        colors = OutlinedTextFieldDefaults.colors(
            //focusedContainerColor = searchBarColor,
            //unfocusedContainerColor = searchBarColor,
            focusedBorderColor = searchBarColor,
            unfocusedBorderColor = searchBarColor
        ),
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        label = { Text(searchBarText) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { whenQueryChanges("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                }
            }
        }
    )
}