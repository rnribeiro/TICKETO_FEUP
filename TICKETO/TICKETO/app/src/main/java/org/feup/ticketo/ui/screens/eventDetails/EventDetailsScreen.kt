package org.feup.ticketo.ui.screens.eventDetails

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.android.volley.VolleyError
import org.feup.ticketo.data.serverMessages.ServerValidationState
import org.feup.ticketo.ui.theme.md_theme_light_background
import org.feup.ticketo.ui.theme.md_theme_light_onPrimary
import org.feup.ticketo.ui.theme.md_theme_light_primary
import org.feup.ticketo.utils.getServerResponseErrorMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(navController: NavHostController, viewModel: EventDetailsViewModel) {

    LaunchedEffect(viewModel) {
        viewModel.fetchEventFromServerState.value =
            ServerValidationState.Loading("Loading event details...")
        viewModel.fetchEvent()
    }

    when {
        viewModel.fetchEventFromServerState.value is ServerValidationState.Success -> {
            EventDetails(viewModel, navController)
        }
    }
    when {
        viewModel.fetchEventFromServerState.value is ServerValidationState.Failure -> {
            LoadingEventDetailsFailedDialog(
                (viewModel.fetchEventFromServerState.value as ServerValidationState.Failure).error,
                viewModel
            )
        }
    }
    when {
        viewModel.fetchEventFromServerState.value is ServerValidationState.Loading -> {
            LoadingEventDetailsText(
                (viewModel.fetchEventFromServerState.value as ServerValidationState.Loading).message,
                navController
            )
        }
    }
    when {
        viewModel.purchaseTicketsInServerState.value is ServerValidationState.Success -> {
            PurchaseSuccessfulDialog(viewModel.purchaseTicketsInServerState)
        }
    }
    when {
        viewModel.purchaseTicketsInServerState.value is ServerValidationState.Failure -> {
            PurchaseFailedDialog(
                (viewModel.purchaseTicketsInServerState.value as ServerValidationState.Failure).error,
                viewModel.purchaseTicketsInServerState
            )
        }
    }
    when {
        viewModel.purchaseTicketsInServerState.value is ServerValidationState.Loading -> {
            LoadingPurchaseDialog()
        }

    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingEventDetailsText(message: String, navController: NavHostController) {
    Column(
        Modifier
            .fillMaxSize()
            .background(color = md_theme_light_background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = md_theme_light_primary,
                actionIconContentColor = md_theme_light_onPrimary,
                navigationIconContentColor = md_theme_light_onPrimary,
                titleContentColor = md_theme_light_onPrimary,
                scrolledContainerColor = md_theme_light_primary
            ),
            title = {
                Text("Event Details")
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(50.dp),
                color = Color.Blue
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = message,
                style = TextStyle(
                    color = md_theme_light_primary,
                    fontSize = 22.sp
                )
            )
        }

    }
}

@Composable
fun LoadingEventDetailsFailedDialog(error: VolleyError, viewModel: EventDetailsViewModel) {
    AlertDialog(
        icon = {
            Icon(Icons.Default.Close, contentDescription = "Failed to load event details")
        },
        title = {
            Text(text = "Failed to load event details", textAlign = TextAlign.Center)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Something went wrong", textAlign = TextAlign.Center)
                Text(
                    text = getServerResponseErrorMessage(error).orEmpty(),
                    textAlign = TextAlign.Center
                )
            }
        },
        onDismissRequest = {},
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.fetchEvent()
                }
            ) {
                Text("Retry")
            }
        }
    )

}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun EventDetails(
    viewModel: EventDetailsViewModel,
    navController: NavHostController
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(color = md_theme_light_background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = md_theme_light_primary,
                actionIconContentColor = md_theme_light_onPrimary,
                navigationIconContentColor = md_theme_light_onPrimary,
                titleContentColor = md_theme_light_onPrimary,
                scrolledContainerColor = md_theme_light_primary
            ),
            title = {
                Text(viewModel.event.name.orEmpty())
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        )
        viewModel.event.picture?.let { BitmapFactory.decodeByteArray(viewModel.event.picture, 0, it.size).asImageBitmap() }
            ?.let {
                Image(
                    contentDescription = null,
                    modifier = Modifier
                        .size(400.dp)
                        .fillMaxWidth(),
                    bitmap = it
                )
            }
        Text(viewModel.event.name.orEmpty())
        Text(viewModel.event.date.orEmpty())
        Text(viewModel.event.price.toString())
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.decreaseTickets() },
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = null
                )
            }
            Text(viewModel.numberTickets.toString())
            IconButton(
                onClick = { viewModel.increaseTickets() },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        }
        when {
            viewModel.numberTickets > 0 -> {
                Button(
                    onClick = { viewModel.checkout() }
                ) {
                    Text("Buy")
                }
            }
        }
    }
}

@Composable
fun PurchaseFailedDialog(
    error: VolleyError,
    purchaseTicketsInServerState: MutableState<ServerValidationState?>
) {
    val errorMessage = getServerResponseErrorMessage(error)
    if (errorMessage != null) {
        Log.i("error", errorMessage)
    }
    AlertDialog(
        icon = {
            Icon(Icons.Default.Close, contentDescription = "Purchase Failed")
        },
        title = {
            Text(text = "Failed to buy tickets", textAlign = TextAlign.Center)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Something went wrong", textAlign = TextAlign.Center)
                if (errorMessage != null) {
                    Text(text = errorMessage, textAlign = TextAlign.Center)
                }
            }
        },
        onDismissRequest = {},
        confirmButton = {
            TextButton(
                onClick = {
                    purchaseTicketsInServerState.value = null
                }
            ) {
                Text("Confirm")
            }
        }
    )
}

@Composable
fun PurchaseSuccessfulDialog(purchaseTicketsInServerState: MutableState<ServerValidationState?>) {
    AlertDialog(
        icon = {
            Icon(Icons.Default.CheckCircle, contentDescription = "Purchase Completed")
        },
        title = {
            Text(text = "Tickets Purchase Successfully", textAlign = TextAlign.Center)
        },
        text = {},
        onDismissRequest = {},
        confirmButton = {
            TextButton(
                onClick = {
                    purchaseTicketsInServerState.value = null
                }
            ) {
                Text("Confirm")
            }
        }
    )
}

@Composable
fun LoadingPurchaseDialog() {
    Dialog(onDismissRequest = { /*TODO*/ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row (
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = Color.Blue
                )
                Text(
                    text = "Purchasing tickets...",
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

}