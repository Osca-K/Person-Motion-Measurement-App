package com.example.gaitxplore

import android.Manifest
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
    private var recordingJob: Job? = null

    //Need Global variable to store Reading to easly transfer to DataBase

    private var xAccel:Double = 0.0
    private var yAccel:Double = 0.0
    private var zAccel:Double = 0.0


    private var zRot:Double = 0.0
    private var yRot:Double = 0.0
    private var xRot:Double = 0.0

    private var xAngVel:Double = 0.0
    private var yAngVel:Double = 0.0
    private var zAngVel:Double = 0.0

    private var gpsLatitude :Double=0.0
    private  var gpsLongitude:Double =0.0
    private var gpsSpeed:Double=0.0



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
        btnLogMotion.setOnClickListener()
        {


            //First I need to check if the sample rate is greater than zero or I should set default value of sample-rate




            isLogginData=true
            Toast.makeText(this, "Motion is being logged", Toast.LENGTH_SHORT).show()

        }


        //DataBase Code Here



        // Write a message to the database--Testing
//        val database = Firebase.database
//        val myRef = database.getReference("message")
//
//        myRef.setValue("Testing DataBase!")

    }

    private val locationListener = object : LocationListener {
        @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        override fun onLocationChanged(location: Location) {
            // Update GPS TextView with latitude, longitude, and speed



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
                Sensor.TYPE_ACCELEROMETER ->
                    {

                        xAccel=event.values[0].toDouble()
                        yAccel=event.values[1].toDouble()
                        zAccel=event.values[2].toDouble()


                        zRot = Math.toDegrees(atan2(xAccel, sqrt(yAccel * yAccel + zAccel * zAccel)))

                        xRot = Math.toDegrees(atan2(zAccel, sqrt(xAccel * xAccel + yAccel * yAccel)))

                        xOrientation.text = String.format("%.3f", zRot)
                        yOrientation.text = String.format("%.3f", xRot)


                        xAcceleration.text = String.format("%.3f",xAccel)
                        yAcceleration.text = String.format("%.3f",yAccel)
                        zAcceleration.text = String.format("%.3f",zAccel)
                }
                Sensor.TYPE_ROTATION_VECTOR ->

                 {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                    val orientationAngles = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)

                     yRot=Math.toDegrees(orientationAngles[0].toDouble())+140.0

                     //+140 is for zeroing the system to be zero when its standing vertically as compared to the internal sensor system which is 0

                     if (yRot > 180) {
                         yRot -= 360
                     } else if (yRot < -180) {
                         yRot += 360
                     }

                     zOrientation.text = String.format("%.3f", yRot)

                     val database = Firebase.database
                     val myRef = database.getReference("message")

                     myRef.setValue(zOrientation.text)

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
        //Empty for now
    }
    private fun startMeasurement()
    {
        isRecording = true
        btnActivate.text = "Stop Motion Sensing"

        val sampleRateMs = sampleRate.text.toString().toIntOrNull() ?: 1000 // Default to 1 second



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
        } else {
            //Nothing do if the if GPS not being enabled
        }




//        val database = Firebase.database
//        val sensorDataRef = database.getReference("SensorData")
//
//// Coroutine to log sensor data
//        recordingJob = CoroutineScope(Dispatchers.IO).launch {
//            while (isRecording) {
//                // Create a SensorData object with the current sensor readings
//                val sensorData = SensorData(
//                    xAccel, yAccel, zAccel, xRot, yRot, zRot,
//                    xAngVel, yAngVel, zAngVel, gpsLatitude, gpsLongitude, gpsSpeed
//                )
//
//                // Push sensor data to Firebase
//                sensorDataRef.setValue("sensorData")
//
//                // Delay based on the sample rate (in milliseconds)
//                delay(sampleRateMs.toLong())
//            }
//        }
    }
    private fun stopMeasurement()
    {

        isRecording = false
        "Start Motion Sensing".also { btnActivate.text = it }


        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(locationListener)


        // Stop Coroutine
        recordingJob?.cancel()

    }
    override fun onDestroy() {
        super.onDestroy()
        stopMeasurement()
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
        } else
        {
            // Nothing to do if there permsion is not grantted
        }

    }
}