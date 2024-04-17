package org.feup.ticketo.ui.screens.home

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.launch
import org.feup.ticketo.data.serverMessages.ServerValidationState
import org.feup.ticketo.data.storage.Event
import org.feup.ticketo.data.storage.TicketoStorage
import org.feup.ticketo.utils.serverUrl
import java.text.SimpleDateFormat
import java.util.Locale

class HomeViewModel(private val context: Context, private val ticketoStorage: TicketoStorage) : ViewModel() {
    val serverValidationState = mutableStateOf<ServerValidationState?>(null)
    val showServerErrorToast = mutableStateOf(false)

    val events = mutableStateOf<List<Event>>(emptyList())

    fun fetchEvents() {
        serverValidationState.value = ServerValidationState.Loading("Loading events...")
        getNextEventsFromServer()
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun getNextEventsFromServer(nr_of_events: Int = 10) {
        val url = serverUrl + "next_events?nr_of_events=$nr_of_events"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val eventsList = mutableListOf<Event>()
                for (i in 0 until response.getJSONArray("events").length()) {
                    val event = response.getJSONArray("events").getJSONObject(i)
                    val eventDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(event.getString("DATE"))
                    val formattedEventDate = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(eventDate)
                    eventsList.add(
                        Event(
                            event_id = event.getInt("EVENT_ID"),
                            name = event.getString("NAME"),
                            date = formattedEventDate,
                            price = event.getDouble("PRICE").toFloat(),
                            picture = event.getString("PICTURE").hexToByteArray()
                        )
                    )
                }
                events.value = eventsList
                storeEventsInDatabase()
                serverValidationState.value = ServerValidationState.Success(response)
            },
            { error ->
                serverValidationState.value = ServerValidationState.Failure(error)
                showServerErrorToast.value = true
            }
        )

        Volley.newRequestQueue(context).add(request)
    }

    private fun storeEventsInDatabase() {
        for (event in events.value) {
            viewModelScope.launch {
                ticketoStorage.insertEvent(event)
            }
        }
    }
}