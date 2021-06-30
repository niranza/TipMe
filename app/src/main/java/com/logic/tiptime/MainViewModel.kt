package com.logic.tiptime

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.NumberFormat


class MainViewModel(private val application: Application) : ViewModel() {

    //two way binding
    val costOfServiceEditTextContent = MutableLiveData<String>()
    val chooseTipEditTextContent = MutableLiveData<String>()
    val splitEditTextContent = MutableLiveData<String>()
    val radioChecked = MutableLiveData<Int>()

    //one way binding
    private val _costForOneTextContent = MutableLiveData<String>()
    val costForOneTextContent: LiveData<String>
        get() = _costForOneTextContent

    private val _tipResultTextContent = MutableLiveData<String>()
    val tipResultTextContent: LiveData<String>
        get() = _tipResultTextContent

    /*
        events
    */
    private val _eventAnimateHelpButtonOut = MutableLiveData<Boolean>()
    val eventAnimateHelpButtonOut: LiveData<Boolean>
        get() = _eventAnimateHelpButtonOut

    private val _eventCloseKeyboard = MutableLiveData<Boolean>()
    val eventCloseKeyboard: LiveData<Boolean>
        get() = _eventCloseKeyboard

    private val _eventHelpButtonClicked = MutableLiveData<Boolean>()
    val eventHelpButtonClicked: LiveData<Boolean>
        get() = _eventHelpButtonClicked

    private val _eventWelcomeScreenClicked = MutableLiveData<Boolean>()
    val eventWelcomeScreenClicked: LiveData<Boolean>
        get() = _eventWelcomeScreenClicked

    //countdown time
    val timer: CountDownTimer //initialized in init

    init {
        radioChecked.value = R.id.option_twenty_percent
        chooseTipEditTextContent.value = ""
        splitEditTextContent.value = ""
        costOfServiceEditTextContent.value = ""

        timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                if (costOfServiceEditTextContent.value == "") onAnimateAttention()
            }
        }
        timer.start()
    }

    fun displayResult() { //displaying the tip in the text with the help of formatPayment fun
        val tip = getTip()
        if (tip == 0.0) {
            _tipResultTextContent.value =
                application.resources.getString(R.string.tip_amount, formatPayment(0.0))
            if (splitEditTextContent.value == "") {
                _costForOneTextContent.value =
                    application.resources.getString(R.string.total_to_pay, formatPayment(totalCost()))
                return
            } else {
                split()
                return
            }
        }
        if (splitEditTextContent.value != "") split()
        else {
            _costForOneTextContent.value =
                application.resources.getString(R.string.total_to_pay, formatPayment(totalCost()))
        }
        _tipResultTextContent.value =
            application.resources.getString(R.string.tip_amount, formatPayment(tip))
    }

    private fun split() {
        val numOfPeople = splitEditTextContent.value?.toIntOrNull()
        val cost = getCost()
        if (numOfPeople == null || numOfPeople == 0 || cost == null || cost == 0.0) { //debugging
            _costForOneTextContent.value = ""
            return
        }
        val costForEach = totalCost() / numOfPeople
        _costForOneTextContent.value =
            application.resources.getString(R.string.cost_for_each, formatPayment(costForEach))
    }

    private fun totalCost(): Double { // calculating the total cost for totalCost display
        var cost = getCost()
        if (cost == null) cost = 0.0
        return getTip() + cost
    }

    private fun getTip(): Double {
        val cost = getCost()
        if (cost == null || cost == 0.0) {
            return 0.0
        }
        val tipPercentage = if (chooseTipEditTextContent.value != "") {
            val percentage =
                chooseTipEditTextContent.value?.toDoubleOrNull() ?: return 0.0
            return percentage * cost / 100
        } else {
            when (radioChecked.value) {
                R.id.option_twenty_percent -> 0.2
                R.id.option_eighteen_percent -> 0.18
                R.id.option_fifteen_percent -> 0.15
                else -> 0.1
            }
        }
        return tipPercentage * cost
    }

    //getting the cost of service
    private fun getCost(): Double? = costOfServiceEditTextContent.value?.toDoubleOrNull()

    //Formatting a Number to currency and inserts it into a text.
    private fun formatPayment(tip: Double?): String = NumberFormat.getCurrencyInstance().format(tip)

    /*
        handlers
    */
    fun handleCalculateButton() {
        displayResult()
        if (costOfServiceEditTextContent.value == "") onAnimateAttention()
        onCloseKeyboard()
    }

    fun handleHelpButton() {
        onHelpButtonClicked()
        onCloseKeyboard()
        timer.cancel()
    }

    fun handleTipOptionRadioButton() {
        displayResult()
        onCloseKeyboard()
    }

    fun handleWelcomeScreen() {
        onWelcomeScreenClicked()
        onCloseKeyboard()
    }

    /*
        onEvent
    */
    fun onAnimateAttention() {
        _eventAnimateHelpButtonOut.value = true
    }

    fun onAnimateAttentionFinished() {
        _eventAnimateHelpButtonOut.value = false
        timer.start()
    }

    fun onCloseKeyboard() {
        _eventCloseKeyboard.value = true
    }

    fun onCloseKeyboardFinished() {
        _eventCloseKeyboard.value = false
    }

    fun onHelpButtonClicked() {
        _eventHelpButtonClicked.value = true
    }

    fun onHelpButtonClickedFinished() {
        _eventHelpButtonClicked.value = false
    }

    fun onWelcomeScreenClicked() {
        _eventWelcomeScreenClicked.value = true
    }

    fun onWelcomeScreenClickedFinished() {
        _eventWelcomeScreenClicked.value = false
    }

    companion object {

        // Countdown time interval
        private const val ONE_SECOND = 1000L

        // Total time for the game
        private const val COUNTDOWN_TIME = 6000L

    }

    override fun onCleared() { //preventing leaks
        super.onCleared()
        timer.cancel()
    }
}

