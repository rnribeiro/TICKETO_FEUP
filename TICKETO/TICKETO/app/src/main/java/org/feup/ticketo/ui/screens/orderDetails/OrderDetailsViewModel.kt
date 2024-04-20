package org.feup.ticketo.ui.screens.orderDetails

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.feup.ticketo.data.serverMessages.ServerValidationState
import org.feup.ticketo.data.serverMessages.orderValidationMessage
import org.feup.ticketo.data.storage.Customer
import org.feup.ticketo.data.storage.OrderWithProductsAndQuantityAndVouchers
import org.feup.ticketo.data.storage.TicketoStorage
import org.feup.ticketo.data.storage.getUserIdInSharedPreferences
import org.feup.ticketo.utils.generateQRCode

class OrderDetailsViewModel(
    private val orderId: Int,
    private val context: Context,
    private val ticketoStorage: TicketoStorage
) : ViewModel() {


    val fetchOrderFromDatabaseState = mutableStateOf<ServerValidationState?>(null)
    val qrCodeGenerationState = mutableStateOf<ServerValidationState?>(null)

    val order = mutableStateOf<OrderWithProductsAndQuantityAndVouchers?>(null)
    val qrCode = mutableStateOf<Bitmap?>(null)
    fun fetchOrder() {
        fetchOrderFromDatabaseState.value = ServerValidationState.Loading("Loading order...")
        fetchOrderFromDatabase()
    }

    private fun fetchOrderFromDatabase() {
        viewModelScope.launch {
            order.value = ticketoStorage.getOrderDetails(
                getUserIdInSharedPreferences(context),
                orderId
            )
        }
        Log.i("order", order.value.toString())
        fetchOrderFromDatabaseState.value = ServerValidationState.Success(null, "Order Loaded!")
    }

    fun validateOrder() {
        val ovm = orderValidationMessage(
            Customer(getUserIdInSharedPreferences(context)),
            order.value!!.orderProducts,
            order.value!!.vouchers,
            null
        )

        try {

            qrCode.value = generateQRCode(ovm)

            if (qrCode.value != null) {
                // Update order
                viewModelScope.launch {
                    ticketoStorage.deleteCustomerVouchers(getUserIdInSharedPreferences(context))
                }
                viewModelScope.launch {
                    ticketoStorage.setOrderAsPickedUp(orderId)
                }
                qrCodeGenerationState.value =
                    ServerValidationState.Success(null, "QR code generated successfully!")
            } else {
                qrCodeGenerationState.value =
                    ServerValidationState.Failure(null, "Error generating QR code")
            }

        } catch (e: Exception) {
            qrCodeGenerationState.value =
                ServerValidationState.Failure(null, "Error generating QR code")
        }


    }
}