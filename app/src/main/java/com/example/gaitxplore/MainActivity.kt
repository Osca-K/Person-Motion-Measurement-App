package com.example.gaitxplore

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ORIENTATION
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt
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


    private lateinit var  latitude : TextView
    private lateinit var  longitude : TextView
    private lateinit var  speed : TextView

    private lateinit var sampleRate :EditText


    private lateinit var  btnActivate: Button
    private lateinit var  btnLogMotion:Button


    private var isRecording  =false
    private var isLogginData =false


    //Need Global variable to store Reading to easly transfer to DataBase

    private var xAccel:Double = 0.0
    private var yAccel:Double = 0.0
    private var zAccel:Double = 0.0


    private var xGravity:Double = 0.0
    private var yGravity:Double = 0.0
    private var zGravity:Double = 0.0



    private var zRot:Double = 0.0
    private var yRot:Double = 0.0
    private var xRot:Double = 0.0

    private var xAngVel:Double = 0.0
    private var yAngVel:Double = 0.0
    private var zAngVel:Double = 0.0

    private var gpsLatitude :Double=0.0
    private  var gpsLongitude:Double =0.0
    private var gpsSpeed:Double=0.0


    private var time: Int = 0
    private var sampleRateHz: Int = 1000


    private lateinit var handler: Handler
    private lateinit var loggingRunnable: Runnable




    //Firebase Ref

   private val firebaseDatabase = FirebaseDatabase.getInstance()
   private val sensorDataRef = firebaseDatabase.getReference("SensorData")






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

     // Trying to request permission ealry before trying to access the sensors (Was getting error before this..I should remember
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
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

      latitude=findViewById(R.id.tvLatitude)
      longitude=findViewById(R.id.tvLongitude)
      speed=findViewById(R.id.tvSpeed)
        // Button


        btnActivate=findViewById(R.id.btnRun)
        btnLogMotion=findViewById(R.id.btnLogMotion)
        sampleRate = findViewById(R.id.etnSampleRate)



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
        btnLogMotion.setOnClickListener()
        {

            if (!isLogginData) {
                isLogginData = true
                btnLogMotion.text = "Stop Log Motion"
                Toast.makeText(this, "Motion is being logged", Toast.LENGTH_SHORT).show()

                // Get sample rate from EditText and start logging
                val sampleRateInput = sampleRate.text.toString().toIntOrNull()
                sampleRateHz = sampleRateInput ?: 1000 // Default to 1 second if input is invalid

                motionLog(sampleRateHz) // Start logging motion
            } else {
                isLogginData = false
                btnLogMotion.text = "Log Motion into Database"
                Toast.makeText(this, "Motion logging stopped", Toast.LENGTH_SHORT).show()
                unLogMotion() // Stop logging motion
            }

        }

    }

    private val locationListener = object : LocationListener {
        @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        override fun onLocationChanged(location: Location) {


            gpsLatitude = location.latitude
            gpsLongitude = location.longitude
            gpsSpeed = location.speed.toDouble()



            latitude.text = String.format("%.3f", gpsLatitude)
            longitude.text = String.format("%.3f", gpsLongitude)
            speed.text = String.format("%.3f", gpsSpeed)


        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null)


            when (event.sensor.type)

            {

                Sensor.TYPE_LINEAR_ACCELERATION ->{

                    xAccel=event.values[0].toDouble()
                    yAccel=event.values[1].toDouble()
                    zAccel=event.values[2].toDouble()



                }
                Sensor.TYPE_GRAVITY ->{
                    xGravity=event.values[0].toDouble()
                    yGravity=event.values[1].toDouble()
                    zGravity=event.values[2].toDouble()

                }

                Sensor.TYPE_ACCELEROMETER ->
                    {

                        xAccel=event.values[0].toDouble()
                        yAccel=event.values[1].toDouble()
                        zAccel=event.values[2].toDouble()


                        xAcceleration.text = String.format("%.3f",xAccel)
                        yAcceleration.text = String.format("%.3f",yAccel)
                        zAcceleration.text = String.format("%.3f",zAccel)
                }

                TYPE_ORIENTATION -> {

                    val azimuth = Math.toDegrees(event.values[0].toDouble())
                    val pitch = Math.toDegrees(event.values[1].toDouble())
                    val roll = Math.toDegrees(event.values[2].toDouble())


                    xRot=roll
                    yRot=pitch
                    zRot=azimuth

                    xOrientation.text = String.format("%.3f", roll)
                    yOrientation.text = String.format("%.3f", pitch)
                    zOrientation.text = String.format("%.3f", azimuth)

                }
                Sensor.TYPE_ROTATION_VECTOR ->

                 {
                     //Roation vector didnt wqork well

                 }

                Sensor.TYPE_GYROSCOPE -> {
                    xAngVel=event.values[0].toDouble()
                    yAngVel=event.values[1].toDouble()
                    zAngVel=event.values[2].toDouble()

                    xAngularVelocity.text = String.format("%.3f", xAngVel)
                    yAngularVelocity.text = String.format("%.3f",yAngVel)
                    zAngularVelocity.text = String.format("%.3f",zAngVel)
                }

            }

        }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int)
    {

    }
    private fun startMeasurement()
    {
        isRecording = true
        btnActivate.text = "Stop Motion Sensing"

        Toast.makeText(this, "Motion Sensing is activated", Toast.LENGTH_SHORT).show()





        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_GAME)

        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            return
        }


        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0f, locationListener)
        }


    }
    private fun stopMeasurement()
    {

        isRecording = false
        "Start Motion Sensing".also { btnActivate.text = it }


        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(locationListener)

    }
    override fun onDestroy() {
        super.onDestroy()
        stopMeasurement()
        unLogMotion()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0f, locationListener)
            }
        }

    }
    private fun logDataToDataBase( xAccel: Double, yAccel: Double, zAccel: Double, xRot:Double, yRot:Double,zRot :Double, xAngVel: Double, yAngVel:Double, zAngVel:Double, lat:Double,lon:Double, speed:Double  )

    {

        val sensorDataMap = mapOf(
            "time" to time,
            "xAccel" to xAccel,
            "yAccel" to yAccel,
            "zAccel" to zAccel,
            "xRot" to xRot,
            "yRot" to yRot,
            "zRot" to zRot,
            "xAngVel" to xAngVel,
            "yAngVel" to yAngVel,
            "zAngVel" to zAngVel,
            "latitude" to lat,
            "longitude" to lon,
            "speed" to speed
        )

        sensorDataRef.push().setValue(sensorDataMap)

    }
    private fun motionLog(sampleRate: Int){

        handler = Handler(Looper.getMainLooper())
        loggingRunnable = Runnable {
            if (isLogginData) {
                logDataToDataBase(xAccel, yAccel, zAccel, xRot, yRot, zRot, xAngVel, yAngVel, zAngVel, gpsLatitude, gpsLongitude, gpsSpeed)
                time += sampleRate
                handler.postDelayed(loggingRunnable, sampleRate.toLong())
            }
        }
        handler.post(loggingRunnable)


    }
    }
    private  fun unLogMotion()
    {

    }


