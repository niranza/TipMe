package com.logic.tiptime

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdView
import com.logic.tiptime.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mAdView: AdView
    private lateinit var viewModel: MainViewModel
    private lateinit var viewModelFactory: MainViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //initialized
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModelFactory = MainViewModelFactory(requireNotNull(this).application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        //bind
        binding.mainViewModel = viewModel
        binding.lifecycleOwner = this

        //observers
        viewModel.eventAnimateHelpButtonOut.observe(this, { needAnimate ->
            if (needAnimate) animateHelpButtonAttention()
        })
        viewModel.eventCloseKeyboard.observe(this, { needCloseKeyboard ->
            if (needCloseKeyboard) closeKeyboard()
        })
        viewModel.eventHelpButtonClicked.observe(this, { needActivateHelpButton ->
            if (needActivateHelpButton) helpButtonHandler()
        })
        viewModel.eventWelcomeScreenClicked.observe(this, { needActivateWelcomeScreen ->
            if (needActivateWelcomeScreen) welcomeScreenHandler()
        })
        val editTextList = listOf(
            viewModel.chooseTipEditTextContent,
            viewModel.costOfServiceEditTextContent,
            viewModel.splitEditTextContent
        )
        for (text in editTextList) {
            text.observe(this, {
                viewModel.displayResult()
                if (text == viewModel.chooseTipEditTextContent) determineTipOptionsAvailability(text.value)
                if (text == viewModel.costOfServiceEditTextContent && //animating help button attention
                    viewModel.costOfServiceEditTextContent.value == ""
                ) viewModel.timer.start()
            })
        }

        /*//section for banner ad
        MobileAds.initialize(this) {}
        mAdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        //end of section*/

        animateHelpButtonIn()
    }

    //--------------------------------------------VIEW----------------------------------------------

    private fun welcomeScreenHandler() { //called at eventWelcomeScreenClicked observer
        binding.layoutWelcomeScreen.isVisible = false
//        binding.calculateButton.isVisible = true
        fadeOutWelcomeScreen()
        viewModel.onWelcomeScreenClickedFinished()
    }

    private fun helpButtonHandler() { //called at eventHelpButtonClicked observer
        binding.layoutWelcomeScreen.isVisible = true
        binding.calculateButton.isVisible = false
        fadeInWelcomeScreen()
        viewModel.onHelpButtonClickedFinished()
    }

    //-------------------------Taking care of the share system in the app---------------------------

    private fun getShareIntent(): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("text/plain").putExtra(
            Intent.EXTRA_TEXT, getString(R.string.share_text)
        )
        return shareIntent
    }

    private fun shareSuccess() {
        startActivity(getShareIntent())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.share_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.shareItem -> shareSuccess()
        }
        return super.onOptionsItemSelected(item)
    }

    //---------------------------------------ANIMATION----------------------------------------------

    private fun fadeInWelcomeScreen() { //called at helpButtonHandler
        val inFade: Animation = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)
        val outFade: Animation = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out)
        binding.layoutWelcomeScreen.startAnimation(inFade)
        binding.calculateButton.startAnimation(outFade)
    }

    private fun fadeOutWelcomeScreen() { //called at welcomeScreenHandler
        val inFade: Animation = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)
        val outFade: Animation = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out)
        binding.layoutWelcomeScreen.startAnimation(outFade)
        binding.calculateButton.startAnimation(inFade)
    }

    private fun animateHelpButtonAttention() { //called at viewModel.handleCalculateButton
        val scale = AnimationUtils.loadAnimation(this, R.anim.scale)
        binding.helpButton.startAnimation(scale)
        viewModel.onAnimateAttentionFinished()
    }

    private fun animateHelpButtonIn() { //called at onCreate
        val scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in)
        binding.helpButton.startAnimation(scaleIn)
    }

    //-------------------------------------HELPER_FUNCTIONS-----------------------------------------

    private fun closeKeyboard() { //closing the keyboard
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.costOfServiceEditText.windowToken, 0)
        viewModel.onCloseKeyboardFinished()
    }

    private fun determineTipOptionsAvailability(string: String?) { //disable or enable tipOptions
        val radioButtonList = mutableListOf<RadioButton>()
        for (i in 0 until binding.tipOptions.childCount)
            radioButtonList.add(binding.tipOptions.getChildAt(i) as RadioButton)

        if (string != "")
            for (button in radioButtonList) {
                button.isEnabled = false
            }
        else for (button in radioButtonList) {
            button.isEnabled = true
        }
    }
}
