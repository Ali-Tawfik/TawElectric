package com.example.tawelectricapp
import android.animation.ObjectAnimator
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Parcel
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class MapsActivity() : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {
    private lateinit var mMap: GoogleMap
    private final val REQUEST_ENABLE_BT = 0
    private final val REQUEST_DISCOVER_BT = 1
    private lateinit var PWM: TextView
    private lateinit var dev_address: String
    private lateinit var progressBar: ProgressBar
    private lateinit var blue_adap: BluetoothAdapter
    private lateinit var mmSocket: BluetoothSocket
    private lateinit var mmDevice: BluetoothDevice
    private lateinit var mmOutputStream: OutputStream
    private lateinit var mmInputStream: InputStream
    private lateinit var progressAnimator: ObjectAnimator
    var locManager: LocationManager? = null

    //private lateinit var mConnectedThread: ConnectedThread
    var lastLocation: Location? = null
    private var mSignInClient: GoogleSignInClient? = null
    public var pwm = 0

    constructor(parcel: Parcel) : this() {
        lastLocation = parcel.readParcelable(Location::class.java.classLoader)
    }

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)


        var mbluetooth = findViewById<TextView>(R.id.speed)
        blue_adap = BluetoothAdapter.getDefaultAdapter();
        if (blue_adap == null) {
            mbluetooth.setText("BlueTooth: Not Available")
        } else {
            //openBT()

            if (blue_adap.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
            val pairedDevices: Set<BluetoothDevice> = blue_adap.getBondedDevices()
            mbluetooth.setText("BlueTooth: Available" + pairedDevices)
            Log.d("hellofam", pairedDevices.toString());

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Choose a device")
            var names = arrayListOf<String>()
// add a list
            for (currentDevice in pairedDevices) {
                if (currentDevice.name == "HC-06") {
                    dev_address = currentDevice.address
                    mmDevice = currentDevice
                }

                names.add(currentDevice.name)
            }
            val cs: Array<CharSequence> = names.toArray(arrayOfNulls<CharSequence>(names.size))
            mbluetooth.setText("BlueTooth: Available" + dev_address)
            openBT()


            /*builder.setItems(cs, object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, item: Int) {
                        val dev_address= cs[item]
                }
            })
            mbluetooth.setText("BlueTooth: Available" + dev_address)
            val dialog = builder.create()
            dialog.show()
*/
        }
        //google login for api
        /*val options =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .build()

        mSignInClient = GoogleSignIn.getClient(this, options)*/
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        /*val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
*/

        /*

        val locationListenerGPS: LocationListener = object : LocationListener {
            var lastLocation: Location? = null
            override fun onLocationChanged(location: Location) {
                Log.d("TAG", "hit location change")
                if (lastLocation!=null) {
                    var textView = findViewById<TextView>(R.id.speed)
                    var speed = round(lastLocation!!.distanceTo(location) / 1000 / ((location.time - lastLocation!!.time) / 1000))
                    textView.setText("Speed: " + speed.toString())
                }

                lastLocation = location



            }*/






        progressBar = findViewById(R.id.progressBar) as ProgressBar
        var button_down = findViewById(R.id.button_down) as Button
        var button_up = findViewById(R.id.button_up) as Button

        button_down.setOnClickListener(this);
        button_up.setOnClickListener(this);

        PWM = findViewById<TextView>(R.id.PWM)


    }

    @Throws(IOException::class)
    fun openBT() {
        val uuid =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") //Standard SerialPortService ID*/
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid)
        mmSocket.connect()
        mmOutputStream = mmSocket.getOutputStream()
        mmInputStream = mmSocket.getInputStream()
        //mConnectedThread = ConnectedThread(mmSocket,this);
        //mConnectedThread!!.start();
        var mbluetooth = findViewById<TextView>(R.id.speed)
        mbluetooth.setText("Bluetooth Opened")
    }

    @Throws(IOException::class)
    fun sendData(msg: String) {
        mmOutputStream.write(msg.toByteArray())
        var mbluetooth = findViewById<TextView>(R.id.speed)
        mbluetooth.setText("Data Sent:" + msg)
    }

    override fun onClick(v: View) {
        when (v.getId()) {
            R.id.button_down -> {
                pwm = when (pwm) {
                    in 1..100 -> pwm - 10
                    else -> pwm
                }
            }

            R.id.button_up -> {
                pwm = when (pwm) {
                    in 0..99 -> pwm + 10
                    else -> pwm
                }
            }

        }
        sendData(pwm.toString())
        //progressBar.setProgress(pwm)
        progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, pwm);
        progressAnimator.setDuration(500);
        progressAnimator.start();
        PWM.setText(pwm.toString() + "%")


    }

    override fun onMapReady(p0: GoogleMap?) {
        TODO("Not yet implemented")
    }
}
/*
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}



private class ConnectedThread(mmSocket: BluetoothSocket, context: MapsActivity) : Thread() {

    private lateinit var cont:MapsActivity
    //creation of the connect thread
    class ConnectedThread constructor(socket: BluetoothSocket, context: MapsActivity) {
      var  mmInStream:InputStream = socket.getInputStream();
      var  mmOutStream:OutputStream  = socket.getOutputStream();
        context
      cont = context;
    }


    override  fun run() {
         val buffer : ByteArray  = ByteArray(256);
         var bytes:Int ;

        // Keep looping to listen for received messages
        while (true) {
            try {
                bytes = mmInStream.read(buffer);            //read bytes from input buffer
                val readMessage:String  =  String(buffer, 0, bytes);
                // Send the obtained bytes to the UI Activity via handler
                Log.d("hellofam", readMessage);
                if (readMessage.equals(context.pwm)==false){


                }
            } catch (e:IOException) {
                break;
            }
        }
    }
}
LocationListener locationListener = new MyLocationListener();
locationManager.requestLocationUpdates(
LocationManager.GPS_PROVIDER, 5000, 10, locationListener);*/