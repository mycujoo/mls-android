package tv.mycujoo.mlsapp.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.mcls.api.HeadlessMLSBuilder

class InfoViewModel : ViewModel() {

    val eventNameLiveData = MutableLiveData<String>()

    fun getEvent(context: Context, eventId: String) {
        val headless = HeadlessMLSBuilder()
            .publicKey("PUBLIC_KEY_HERE")
            .identityToken("IDENTITY_TOKEN_HERE")
            .build(context)

        viewModelScope.launch {
            val mclsEvent = headless.getDataManager().getEventDetails(eventId)
            if (mclsEvent is Result.Success) {
                eventNameLiveData.postValue(mclsEvent.value.title)
            }
        }
    }
}