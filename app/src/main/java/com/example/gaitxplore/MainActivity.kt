package com.example.gaitxplore

import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.text.*


class MainActivity : AppCompatActivity() , SensorEventListener{

    private lateinit var  sensorManager: SensorManager
    private lateinit var  locationManager: LocationManager

    private lateinit var  xAcceleration : TextView
    private lateinit var  yAcceleration : TextView
    private lateinit var  zAcceleration : TextView


    private lateinit var  xOrientation : TextView
    private lateinit var  yOrientation : TextView
    private lateinit var  zOrientation : TextView

    private lateinit var  xAngularVelocity : TextView
    private lateinit var  yAngularVelocity : TextView
    private lateinit var  zAngularVelocity : TextView


    private lateinit var  Latitute : TextView
    private lateinit var  Longitude : TextView
    private lateinit var  Speed : TextView


    private lateinit var  btnActivate: Button

    private var isRecording  =false



//    private var acceleration = FloatArray(3)
//    private var orientation = FloatArray(3)
//    private var angularVelocity = FloatArray(3)
//    private var latitude: Double = 0.0
//    private var longitude: Double = 0.0
//    private var speed: Float = 0f


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main))
        { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

     // Trying to request permission ealry
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }


        //Linking the textview for displaying the results with the ID from  UI of xml

      // Accelration

        xAcceleration=findViewById(R.id.tvXacceleration)
        yAcceleration=findViewById(R.id.tvYacceleration)
        zAcceleration=findViewById(R.id.tvZacceleration)


     // Orientation
        xOrientation=findViewById(R.id.tvXorientation)
        yOrientation=findViewById(R.id.tvYorientation)
        zOrientation=findViewById(R.id.tvZorientation)


     // Angular velocity

        xAngularVelocity=findViewById(R.id.tvXangularVelocity)
        yAngularVelocity=findViewById(R.id.tvYangularVelocity)
        zAngularVelocity=findViewById(R.id.tvZangularVelocity)

    // GPS FOR POSTION AND SPEED

      Latitute=findViewById(R.id.tvLatitude)
      Longitude=findViewById(R.id.tvLongitude)
      Speed=findViewById(R.id.tvSpeed)

        // Button

        btnActivate=findViewById(R.id.btnRun)



    // Sensors and locations managers
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager




        btnActivate.setOnClickListener()
        {
            if (isRecording)
            {
                stopMeasurement()

            }
            else
            {
                startMeasurement()
            }

        }




    }

    private val locationListener = object : LocationListener {
        @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        override fun onLocationChanged(location: Location) {
            // Update GPS TextView with latitude, longitude, and speed



            Latitute.text = String.format("%.3f", location.latitude)
            Longitude.text = String.format("%.3f", location.longitude)
            Speed.text = String.format("%.3f", location.speed)


        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    xAcceleration.text = String.format("%.3f", event.values[0])
                    yAcceleration.text = String.format("%.3f", event.values[1])
                    zAcceleration.text = String.format("%.3f", event.values[2])
                }
                Sensor.TYPE_ROTATION_VECTOR -> {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                    val orientationAngles = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)

                    xOrientation.text = String.format("%.3f", Math.toDegrees(orientationAngles[0].toDouble()))
                    yOrientation.text = String.format("%.3f", Math.toDegrees(orientationAngles[1].toDouble()))
                    zOrientation.text = String.format("%.3f", Math.toDegrees(orientationAngles[2].toDouble()))
                }
                Sensor.TYPE_GYROSCOPE -> {
                    xAngularVelocity.text = String.format("%.3f", event.values[0])
                    yAngularVelocity.text = String.format("%.3f", event.values[1])
                    zAngularVelocity.text = String.format("%.3f", event.values[2])
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int)
    {
        //Empty for now
    }
    private fun startMeasurement()
    {
        isRecording = true
        btnActivate.text = "Stop"

        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL)

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            return
        }

        // Check if the provider is enabled before requesting updates
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0f, locationListener)
        } else {
            // Handle case where GPS provider is not enabled
            // For example, show a message to the user or navigate to settings to enable GPS
        }
    }
    private fun stopMeasurement()
    {

        isRecording = false
        "Start".also { btnActivate.text = it }


        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(locationListener)

    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(
                    this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0f, locationListener)
            }
        } else
        {
            // Handle permission denied case
        }

    }
}