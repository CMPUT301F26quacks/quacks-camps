package com.example.quackapp.View.Admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quackapp.Model.NavBarLists
import com.example.quackapp.R
import com.example.quackapp.View.NavBar

/** Orange used for admin titles and buttons in the mockup. */
val AdminOrange = Color(0xFFF16B00)

/** Yellow used for the stats box and logo text. */
val AdminYellow = Color(0xFFF2BA00)

/** Blue used for list cards and the Switch Profile button. */
val AdminBlue = Color(0xFF2196F3)

/**
 * Provides the admin navigation bar (NavBarLists.admin) to all Admin screens.
 *
 * Same pattern as OwnerScaffold.
 *
 * @param navController used to allow navigation between screens.
 * @param title optional orange title shown next to the duck logo, like the mockup headers.
 * @param content screen content, given the scaffold padding.
 */
@Composable
fun AdminScaffold(
    navController: NavController,
    title: String? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = { if (title != null) AdminHeader(title) },
        bottomBar = { NavBar(navController, NavBarLists.admin) },
        content = content
    )
}

/**
 * Duck logo with an orange title, used at the top of admin screens.
 *
 * @param title text shown beside the logo, e.g. "Image Gallery".
 */
@Composable
fun AdminHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.duck),
            contentDescription = "Logo",
            modifier = Modifier.size(40.dp)
        )
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AdminOrange)
    }
}
