package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.RequestCalendarModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.GetCalendarExercise
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CalendarExerciseViewModel
@Inject constructor(private val getCalendarExercise: GetCalendarExercise) : BaseViewModel() {

    private val _resCalendarExercise: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resCalendarExercise: LiveData<ResBaseModel> = _resCalendarExercise

    fun getCalendarExercise(time : RequestCalendarModel) =
        getCalendarExercise(time, viewModelScope) { it.fold(::handleFailure, ::handleGetCalendarExercise) }

    private fun handleGetCalendarExercise(resBaseModel: ResBaseModel) {
        _resCalendarExercise.value =
            ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}