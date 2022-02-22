package com.abhishek.employeemanagment.ui.activities

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.abhishek.employeemanagment.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    /**
     * Function to handle the back press in fragments.
     */
    override fun onBackPressed() {
        val stackFragmentValue = binding.myNavHostFragment.findNavController().backQueue.size
        if (stackFragmentValue <= 2 || stackFragmentValue == 4) finish()
        super.onBackPressed()
    }

    override fun finish() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.finishAndRemoveTask()
        } else {
            super.finish()
        }
    }
}
