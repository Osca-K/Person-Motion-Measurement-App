package com.example.gaitxplore

import android.Manifest
import android.annotation.SuppressLint
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
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlin.math.acos
import kotlin.math.atan2
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
    private lateinit var  distance: TextView

    private lateinit var sampleRate :EditText


    private lateinit var  btnActivate: Button
    private lateinit var  btnLogMotion:Button
    private  lateinit var btnClearDataBase:Button


    private var isRecording  =false
    private var isLogginData =false


    //Need Global variable to store Reading to easly transfer to DataBase

    private var xAccel:Double = 0.0
    private var yAccel:Double = 0.0
    private var zAccel:Double = 0.0


    private var xGravity:Double = 0.0
    private var yGravity:Double = 0.0
    private var zGravity:Double = 0.0



    private var xRotPitch:Double = 0.0
    private var yRotRoll:Double = 0.0
    private var zRotYaw:Double = 0.0

    private var xAngVel:Double = 0.0
    private var yAngVel:Double = 0.0
    private var zAngVel:Double = 0.0

    private var gpsLatitude :Double=0.0
    private  var gpsLongitude:Double =0.0
    private var gpsSpeed:Double=0.0
    private var gpspreviousLocation: Location? = null
    private var gpstotalDistance: Float = 0f



    //Var for DataBase inmplementation

    private var sampleNumber: Int = 0

    private lateinit var handler: Handler
    private lateinit var loggingRunnable: Runnable



   private val firebaseDatabase = FirebaseDatabase.getInstance()

   private var sessionCounter = 1
   private var currentSessionRef: DatabaseReference? = null




    @SuppressLint("SetTextI18n")
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


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }


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
      distance=findViewById(R.id.tvDistance)

        // Button

        btnActivate=findViewById(R.id.btnRun)
        btnLogMotion=findViewById(R.id.btnLogMotion)
        btnClearDataBase=findViewById(R.id.btnClearData)


        sampleRate = findViewById(R.id.etnSampleRate)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        btnLogMotion.isEnabled = false


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


                val sampleRateInput = sampleRate.text.toString().toIntOrNull()
                val sampleRateHz = sampleRateInput ?: 50
                val sampleRateMS=(1000/sampleRateHz)
                motionLog(sampleRateMS)


            } else {
                isLogginData = false
                btnLogMotion.text = "Log Motion into Database"
                Toast.makeText(this, "Motion logging stopped", Toast.LENGTH_SHORT).show()
                sampleNumber = 0
                gpstotalDistance = 0f

            }
        }
        btnClearDataBase.setOnClickListener()
        {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Clearing Database")
            builder.setMessage("Confirm to clear database?")

            builder.setPositiveButton("Yes") { dialog, _ ->
                clearDataBase()
                dialog.dismiss()
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()

        }

    }

    private val locationListener = object : LocationListener {
        @SuppressLint("DefaultLocale")
        @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        override fun onLocationChanged(location: Location) {

            gpsLatitude = location.latitude
            gpsLongitude = location.longitude
            gpsSpeed = location.speed.toDouble()


            gpspreviousLocation?.let {
                gpstotalDistance += it.distanceTo(location)
            }


            gpspreviousLocation = location

            latitude.text = String.format("%.2f", gpsLatitude)
            longitude.text = String.format("%.2f", gpsLongitude)
            speed.text = String.format("%.2f", gpsSpeed)

        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    @SuppressLint("DefaultLocale")
    override fun onSensorChanged(event: SensorEvent?)
    {
        if (event != null)


            when (event.sensor.type)

            {

                Sensor.TYPE_LINEAR_ACCELERATION ->{

                    xAccel=event.values[0].toDouble()
                    yAccel=event.values[1].toDouble()
                    zAccel=event.values[2].toDouble()

                    xAcceleration.text = String.format("%.2f",xAccel)
                    yAcceleration.text = String.format("%.2f",yAccel)
                    zAcceleration.text = String.format("%.2f",zAccel)

                }
                Sensor.TYPE_GRAVITY ->{
                    xGravity=event.values[0].toDouble()
                    yGravity=event.values[1].toDouble()
                    zGravity=event.values[2].toDouble()

                    //To check the direction of orientation

                }

                Sensor.TYPE_ROTATION_VECTOR ->

                 {

                     val rotationMatrix = FloatArray(9)
                     SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                     val xAxisZ = rotationMatrix[6]
                     val zAxisZ = rotationMatrix[8]


                     val tiltX = Math.toDegrees(acos(xAxisZ.toDouble())).toFloat()
                     zRotYaw=-1.0*(tiltX-90.0)
                   
                   
                   
                     val tiltY = Math.toDegrees(atan2(rotationMatrix[2].toDouble(), rotationMatrix[5].toDouble()))



                     yRotRoll = if(tiltY<0)
                         -1.0*(180+tiltY)
                     else
                         1.0*(180-tiltY)

        
                     val tiltZ = Math.toDegrees(acos(zAxisZ.toDouble())).toFloat()
                     xRotPitch=tiltZ-90.0


                     xOrientation.text = String.format("%.3f",xRotPitch )
                     yOrientation.text = String.format("%.3f", yRotRoll)
                     zOrientation.text = String.format("%.3f",zRotYaw )

                 }

                Sensor.TYPE_GYROSCOPE -> {
                    xAngVel=event.values[0].toDouble()
                    yAngVel=event.values[1].toDouble()
                    zAngVel=event.values[2].toDouble()

                    xAngularVelocity.text = String.format("%.2f", xAngVel)
                    yAngularVelocity.text = String.format("%.2f",yAngVel)
                    zAngularVelocity.text = String.format("%.2f",zAngVel)
                }
            }

        }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int)
    {

    }
    @SuppressLint("SetTextI18n")
    private fun startMeasurement()
    {
        isRecording = true
        btnActivate.text = "Stop Motion Sensing"

        Toast.makeText(this, "Motion Sensing is activated", Toast.LENGTH_SHORT).show()
        btnLogMotion.isEnabled = true


        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME)


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            return
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 0f, locationListener)
        }
    }
    private fun stopMeasurement()
    {
        isRecording = false
        "Start Motion Sensing".also { btnActivate.text = it }
        btnLogMotion.isEnabled = false

        if (isLogginData) {
            isLogginData = false
            btnLogMotion.text = "Log Motion into Database"
            Toast.makeText(this, "Motion logging stopped", Toast.LENGTH_SHORT).show()
            sampleNumber = 0
            gpstotalDistance = 0f
        }


        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(locationListener)

        handler.removeCallbacks(loggingRunnable)
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

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 0f, locationListener)
            }
        }
    }
    private fun logDataToDataBase(xAccel: Double, yAccel: Double, zAccel: Double, xRotPitch:Double, yRotRoll:Double, zRotYaw :Double, xAngVel: Double, yAngVel:Double, zAngVel:Double, lat:Double, lon:Double, speed:Double, distance: Float  )

    {
        val sensorDataMap = mapOf(
            "Sample No" to sampleNumber ,
            "xAccel" to xAccel,
            "yAccel" to yAccel,
            "zAccel" to zAccel,
            "Pitch" to xRotPitch,
            "Roll" to yRotRoll,
            "Yaw" to zRotYaw,
            "xAngVel" to xAngVel,
            "yAngVel" to yAngVel,
            "zAngVel" to zAngVel,
            "latitude" to lat,
            "longitude" to lon,
            "speed" to speed,
            "distance" to distance
        )

        currentSessionRef?.push()?.setValue(sensorDataMap)

    }
    private fun motionLog(sampleRate: Int){

        handler = Handler(Looper.getMainLooper())
        loggingRunnable = Runnable {
            if (isLogginData) {
                logDataToDataBase(xAccel, yAccel, zAccel, zRotYaw, yRotRoll, xRotPitch, xAngVel, yAngVel, zAngVel, gpsLatitude, gpsLongitude, gpsSpeed,gpstotalDistance)
                sampleNumber +=1
                handler.postDelayed(loggingRunnable, sampleRate.toLong())
            }
        }
        handler.post(loggingRunnable)

        currentSessionRef = firebaseDatabase.getReference("SensorData$sessionCounter")
        sessionCounter++

    }

    }
    private  fun clearDataBase()
    {

        val firebaseDatabase = FirebaseDatabase.getInstance()
        val rootRef = firebaseDatabase.getReference("")


        rootRef.get().addOnSuccessListener { dataSnapshot ->
            for (childSnapshot in dataSnapshot.children) {

                if (childSnapshot.key?.startsWith("SensorData") == true) {

                    childSnapshot.ref.removeValue().addOnCompleteListener {

                    }
                }
            }
        }.addOnFailureListener { }

    }



