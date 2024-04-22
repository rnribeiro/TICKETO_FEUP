package org.feup.ticketo.ui.screens.pastOrders

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.volley.VolleyError
import org.feup.ticketo.data.serverMessages.ServerValidationState
import org.feup.ticketo.data.storage.OrderProductWithProduct
import org.feup.ticketo.data.storage.OrderWithProductsAndQuantityAndVouchers
import org.feup.ticketo.data.storage.Voucher
import org.feup.ticketo.ui.theme.md_theme_light_background
import org.feup.ticketo.ui.theme.md_theme_light_onPrimary
import org.feup.ticketo.ui.theme.md_theme_light_primary
import org.feup.ticketo.utils.getServerResponseErrorMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastOrdersScreen(
    navController: NavController,
    viewModel: PastOrdersViewModel,
    context: Context
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(color = md_theme_light_background),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
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
                    Text("Past Orders")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
            )
            // PAGE CONTENT
            LaunchedEffect(viewModel) {
                viewModel.fetchOrders()
            }
            when (viewModel.fetchOrdersFromServerState.value) {
                is ServerValidationState.Loading -> {
                    LoadingOrders()
                }

                is ServerValidationState.Success -> {
                    Orders(viewModel = viewModel)
                }

                is ServerValidationState.Failure -> {
                    LoadingOrdersFailed(
                        (viewModel.fetchOrdersFromServerState.value as ServerValidationState.Failure).error,
                        viewModel
                    )
                }
            }

        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun Orders(
    viewModel: PastOrdersViewModel
) {
    LazyColumn {
        items(viewModel.orders.value.size) { order ->
            OrderCard(viewModel.orders.value[order], viewModel)
        }
    }
}

@Composable
fun OrderCard(
    order: OrderWithProductsAndQuantityAndVouchers,
    viewModel: PastOrdersViewModel
) {
    var expanded by remember { mutableStateOf(false) }

    val modifier = if (expanded) {
        Modifier
            .padding(10.dp)
    } else {
        Modifier
            .padding(10.dp)
            .height(100.dp)
    }

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = md_theme_light_onPrimary
        ),
        modifier = modifier.clickable { if (!expanded) expanded = !expanded },

        ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.wrapContentHeight()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp, bottom = 15.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Date", fontWeight = FontWeight.Bold)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = order.order.date.orEmpty().substring(0, 10),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = order.order.date.orEmpty().substring(10),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Total", fontWeight = FontWeight.Bold)
                    Text(
                        text = order.order.total_price.toString() + " €",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                        contentDescription = null
                    )
                }
            }
            // Display tickets and vouchers when expanded
            if (expanded) {
                UserInfo(viewModel)
                ProductsList(order.orderProducts)
                VouchersList(order.vouchers)
            }
        }
    }
}


@Composable
fun UserInfo(viewModel: PastOrdersViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 15.dp, bottom = 15.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = "Customer", fontWeight = FontWeight.Bold)
            Text(
                text = viewModel.customerName.value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = "Tax Number", fontWeight = FontWeight.Bold)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = viewModel.taxNumber.value,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun VouchersList(vouchers: List<Voucher>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Vouchers",
            style = TextStyle(fontSize = 20.sp),
            fontWeight = FontWeight.Bold
        )
        if (vouchers.isEmpty()) Text(text = "No vouchers associated with this purchase.")

        vouchers.forEach {
            VoucherItem(voucher = it)
        }
    }

}

@Composable
fun VoucherItem(voucher: Voucher) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 30.dp, top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = voucher.description.toString())
    }
}

@Composable
private fun ProductsList(products: List<OrderProductWithProduct>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tickets",
            style = TextStyle(fontSize = 20.sp),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        products.forEach { product ->
            ProductCard(product)
        }
    }
}

@Composable
fun ProductCard(orderProduct: OrderProductWithProduct) {
    var icon = Icons.Default.ChevronRight
    when {
        orderProduct.product.name.toString().contains("Coffee") -> icon = Icons.Default.Coffee
        orderProduct.product.name.toString().contains("Popcorn") -> icon = Icons.Default.Fastfood
        orderProduct.product.name.toString().contains("Soda") -> icon = Icons.Default.LocalDrink
        orderProduct.product.name.toString().contains("Sandwich") -> icon =
            Icons.Default.LunchDining
    }
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = md_theme_light_onPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 15.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp, 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = "Product Icon")
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Product", fontWeight = FontWeight.Bold)
                Text(
                    text = orderProduct.product.name.toString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Price", fontWeight = FontWeight.Bold)
                Text(
                    text = orderProduct.product.price.toString() + " €",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Quantity", fontWeight = FontWeight.Bold)
                Text(text = orderProduct.orderProduct.quantity.toString())
            }
        }
    }
}

@Composable
fun LoadingOrders() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(50.dp),
            color = md_theme_light_primary
        )
    }
}

@Composable
fun LoadingOrdersFailed(error: VolleyError?, viewModel: PastOrdersViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Failed to load past orders")
        Text(getServerResponseErrorMessage(error).orEmpty())
    }
}