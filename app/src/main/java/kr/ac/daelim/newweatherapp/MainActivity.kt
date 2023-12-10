package kr.ac.daelim.newweatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kr.ac.daelim.newweatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var isRefreshing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            if (!isRefreshing) {
                isRefreshing = true
                fetchWeatherData("Suwon")
            }
        }

        SearchCity()
        fetchWeatherData("Suwon")
    }

    private fun SearchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(cityName, "91badfada80424a85d04e28c349d62d4", "metric")
        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    val temperature = responseBody.main.temp.toDouble()
                    val humidity = responseBody.main.humidity
                    val windSpeed = responseBody.wind.speed
                    val sunRise = responseBody.sys.sunrise.toLong()
                    val sunSet = responseBody.sys.sunset.toLong()
                    val seaLevel = responseBody.main.pressure
                    val condition = responseBody.weather.firstOrNull()?.main?: "unknown"
                    val maxTemp = responseBody.main.temp_max.toDouble()
                    val minTemp = responseBody.main.temp_min.toDouble()

                    binding.temp.text = "${temperature.roundToInt()} °C"
                    binding.weather.text = condition
                    binding.maxTemp.text = "최고 : ${maxTemp.roundToInt()} °C"
                    binding.minTemp.text = "최저 : ${minTemp.roundToInt()} °C"
                    binding.humidity.text = "${humidity} %"
                    binding.windSpeed.text = "${windSpeed} m/s"
                    binding.sunrise.text = "${time(sunRise)}"
                    binding.sunset.text = "${time(sunSet)}"
                    binding.sea.text = "${seaLevel} hPa"
                    binding.conditions.text = condition

                    if (dayName(System.currentTimeMillis()) == "Sunday") {
                        binding.day.text = "일요일"
                    } else if (dayName(System.currentTimeMillis()) == "Monday") {
                        binding.day.text = "월요일"
                    } else if (dayName(System.currentTimeMillis()) == "Tuesday") {
                        binding.day.text = "화요일"
                    } else if (dayName(System.currentTimeMillis()) == "Wednesday") {
                        binding.day.text = "수요일"
                    } else if (dayName(System.currentTimeMillis()) == "Thursday") {
                        binding.day.text = "목요일"
                    } else if (dayName(System.currentTimeMillis()) == "Friday") {
                        binding.day.text = "금요일"
                    } else if (dayName(System.currentTimeMillis()) == "Saturday") {
                        binding.day.text = "토요일"
                    }

                    binding.date.text = date()
                    binding.cityName.text = "${cityName}"

                    changeImagesWeaher(condition)
                }
                // 스와이프 리프레시가 완료되면 플래그를 다시 false로 설정
                isRefreshing = false
                val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
                swipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                // 스와이프 리프레시가 실패하더라도 플래그를 false로 설정
                isRefreshing = false
                val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
                swipeRefreshLayout.isRefreshing = false
            }
        })
    }

    private fun changeImagesWeaher(conditions: String) {
        when (conditions) {

            "Haze", "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy", "Fog" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }

            "Clear Sky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }

            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain", "Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }

            "Light Snow", "Moderate Snow", "Blizzard", "Heavy Snow", "Snow" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }

    private fun time(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:MM", Locale.getDefault())
        return sdf.format((Date(timestamp * 1000)))
    }

    private fun date(): String {
        val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
        return sdf.format((Date()))
    }

    fun dayName(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }
}
