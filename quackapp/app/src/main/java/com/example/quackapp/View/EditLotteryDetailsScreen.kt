package com.example.quackapp.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults.iconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quackapp.Controller.EditLotteryDetailsController
import com.example.quackapp.Controller.SaveStatus
import com.example.quackapp.Model.Lottery

/**
 * Screen for editing a [Lottery]'s details
 *
 * @param navController the number of winners allowed for the lottery
 * @param viewModel the number of people allowed in the waitlist, or null if no limit
 */
@Composable
fun EditLotteryDetailsScreen(
    navController: NavController,
    viewModel: EditLotteryDetailsController = viewModel()
) {


    val status by viewModel.status.collectAsState()
    LaunchedEffect(status) {
        if (status is SaveStatus.Success){
            navController.navigate(route = "CampsiteDetails/${viewModel.campsiteId}")
            {
                popUpTo("CampsiteDetails/${viewModel.campsiteId}") { inclusive = true }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Editing Lottery", fontSize = 36.sp)
            IconButton(
                colors = iconButtonColors(
                    containerColor = Color(0xFFFF9800),
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.LightGray
                ),
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "On click go back"
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
            Text("Any changes will affect these campsites: ")
            viewModel.names.forEach { name ->
                Text("- $name")
            }
            Text("* signifies required field", color = Color.Red)
            Text(text = buildAnnotatedString {
                append("Registration Period ")
                withStyle(style = SpanStyle(color = Color.Red)) {
                    append("*")
                }
            }, fontSize = 24.sp)

            DateRangePickerField(
                label = "Registration Period",
                startMillis = viewModel.registrationStart,
                endMillis = viewModel.registrationEnd,
                onRangeSelected = { s, e -> viewModel.registrationStart = s; viewModel.registrationEnd = e },
                minDateMillis = todayUtcMillis(),
                modifier = Modifier.testTag("registrationDates")
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = buildAnnotatedString {
                append("Booking Period ")
                withStyle(style = SpanStyle(color = Color.Red)) {
                    append("*")
                }
            }, fontSize = 24.sp)

            DateRangePickerField(
                label = "Booking dates",
                startMillis = viewModel.bookingStart,
                endMillis = viewModel.bookingEnd,
                onRangeSelected = { s, e -> viewModel.bookingStart = s; viewModel.bookingEnd = e },
                minDateMillis = (viewModel.registrationEnd?.plus(86400000))?: todayUtcMillis(), // 86400000 is one day in milliseconds
                modifier = Modifier.testTag("bookingDates")
                )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = buildAnnotatedString {
                    append("Number of Winners ")
                    withStyle(style = SpanStyle(color = Color.Red)) {
                        append("*")
                    } },
                fontSize = 24.sp
            )
            OutlinedTextField(
                value = viewModel.numWinners,
                onValueChange = { input ->
                    if (input.isDigitsOnly() && input.isNotEmpty()) {
                        viewModel.numWinners = input
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("numWinnersField")
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text("Waitlist Capacity", fontSize = 24.sp)
            OutlinedTextField(
                value = viewModel.waitlistCap,
                onValueChange = { input ->
                    if (input.isDigitsOnly()) {
                        viewModel.waitlistCap = input
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("waitlistCapField")
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = buildAnnotatedString {
                append("Available Dates ")
                withStyle(style = SpanStyle(color = Color.Red)) {
                    append("*")
                }
            }, fontSize = 24.sp)

            DateRangePickerField(
                label = "Available Dates",
                startMillis = viewModel.availabilityStart,
                endMillis = viewModel.availabilityEnd,
                onRangeSelected = { s, e -> viewModel.availabilityStart = s; viewModel.availabilityEnd = e },
                minDateMillis = (viewModel.bookingEnd?.plus(86400000))?: todayUtcMillis(), // 86400000 is one day in milliseconds
                modifier = Modifier.testTag("availabilityDates")
                )
            (status as? SaveStatus.Error)?.let {
                Text(
                    it.message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 18.sp
                )
            }
            Button(
                onClick = {
                    viewModel.updateLottery()
                },
                enabled = status !is SaveStatus.Saving,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("saveButton"),
                colors = ButtonDefaults.buttonColors(Color(0xFFFF9800)),
            ) {
                if (status is SaveStatus.Saving) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save")
                }
            }
        }
    }
}