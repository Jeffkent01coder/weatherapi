package com.jeff.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.jeff.weatherapp.api.Current
import com.jeff.weatherapp.api.Location
import com.jeff.weatherapp.api.NetworkResponse
import com.jeff.weatherapp.api.WeatherModel
import com.jeff.weatherapp.api.WeatherViewModel
import com.jeff.weatherapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val weatherViewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSearch.setOnClickListener {
            val city = binding.etCity.text.toString().trim()
            if (city.isNotEmpty()) {
                weatherViewModel.getData(city)
                hideKeyboard()
            }
        }

        weatherViewModel.weatherResult.observe(this, Observer { response ->
            when (response) {
                is NetworkResponse.Loading -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    binding.llWeatherDetails.visibility = android.view.View.GONE
                    binding.tvError.visibility = android.view.View.GONE
                }
                is NetworkResponse.Success<*> -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.llWeatherDetails.visibility = android.view.View.VISIBLE
                    binding.tvError.visibility = android.view.View.GONE

                    // Cast the data to WeatherModel
                    val data = response.data as? WeatherModel
                    if (data != null) {
                        // Assign location and current variables
                        val location: Location = data.location
                        val current: Current = data.current

                        // Update UI using the assigned variables
                        binding.tvLocationName.text = location.name
                        binding.tvCountry.text = location.country
                        binding.tvTemp.text = "${current.temp_c} °C"
                        binding.tvCondition.text = current.condition.text
                        // Load image using your preferred image loading library (e.g., Glide)
                         Glide.with(this)
                              .load("https:${current.condition.icon}".replace("64x64", "128x128"))
                              .into(binding.ivCondition)

                        binding.tvHumidity.text = current.humidity
                        binding.tvWindSpeed.text = "${current.wind_kph} km/h"
                        binding.tvUV.text = current.uv
                        binding.tvPrecip.text = "${current.precip_mm} mm"

                        // Split localtime into date and time
                        val parts = location.localtime.split(" ")
                        if (parts.size == 2) {
                            binding.tvLocalDate.text = parts[0]
                            binding.tvLocalTime.text = parts[1]
                        } else {
                            binding.tvLocalDate.text = location.localtime
                            binding.tvLocalTime.text = ""
                        }
                    }
                }
                is NetworkResponse.Error -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.llWeatherDetails.visibility = android.view.View.GONE
                    binding.tvError.visibility = android.view.View.VISIBLE
                    binding.tvError.text = response.message
                }
            }
        })
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
