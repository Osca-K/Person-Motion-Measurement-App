package com.example.gaitxplore

import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView.Orientation

class MainActivity : AppCompatActivity() {

    private lateinit var  sensorManager: SensorManager
    private lateinit var  locationManager: SensorManager

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


    private lateinit var  btnStar: Button

    private var IsRecording  =false

    

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




    }
}