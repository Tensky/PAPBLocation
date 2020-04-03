package id.tensky.papb_location

import android.Manifest
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    val REQUEST_LOCATION_PERMISSION = 1
    val TRACKING_LOCATION_KEY = "tracking key"
    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    lateinit var mLastLocation :Location
    lateinit var mRotateAnim:AnimatorSet
    private var mTrackingLocation = false
    private lateinit var mLocationCallback: LocationCallback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getLocationPermission()
        mFusedLocationProviderClient =LocationServices.getFusedLocationProviderClient(this)



        // Set up the animation.
        mRotateAnim = AnimatorInflater.loadAnimator(this, R.animator.rotate) as AnimatorSet
        mRotateAnim.setTarget(imageview_android)

        // Restore the state if the activity is recreated.
        if (savedInstanceState != null) {
            mTrackingLocation = savedInstanceState.getBoolean(
                TRACKING_LOCATION_KEY
            )
        }
        button_location.setOnClickListener{
            if (!mTrackingLocation) {
                startTrackingLocation()
            } else {
                stopTrackingLocation()
            }
        }

        // Initialize the location callbacks.
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // If tracking is turned on, reverse geocode into an address
                if (mTrackingLocation) {
                    FetchTask(applicationContext, onFetchTask)
                        .execute(locationResult.lastLocation)
                }
            }
        }
    }

    private val onFetchTask = object : FetchTask.OnTaskCompleted {
        override fun onTaskCompleted(result: String?) {
            if (mTrackingLocation) {
                // Update the UI
                textview_location.setText(
                    getString(
                        R.string.address_text,
                        result, System.currentTimeMillis()
                    )
                )
            }
        }

    }

    private fun startTrackingLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            mTrackingLocation = true
            mFusedLocationProviderClient.requestLocationUpdates(
                getLocationRequest(),
                mLocationCallback,
                null /* Looper */
            )

            // Set a loading text while you wait for the address to be
            // returned
            textview_location.setText(
                getString(
                    R.string.address_text,
                    getString(R.string.loading),
                    System.currentTimeMillis()
                )
            )
            button_location.setText("Stop")
            mRotateAnim.start()
        }
    }

    private fun stopTrackingLocation() {
        if (mTrackingLocation) {
            mTrackingLocation = false
            button_location.setText("Start")
            textview_location.setText(R.string.textview_hint)
            mRotateAnim.end()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(TRACKING_LOCATION_KEY, mTrackingLocation)
        super.onSaveInstanceState(outState)
    }

    private fun getLocationRequest(): LocationRequest? {
        val locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }

    private fun getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            Log.d("locss", "getLocation: permissions granted")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            REQUEST_LOCATION_PERMISSION->{
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocationPermission();
                } else {
                    Toast.makeText(this,
                        "Jangan di deny pak..",
                        Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPause() {
        if (mTrackingLocation) {
            stopTrackingLocation()
            mTrackingLocation = true
        }
        super.onPause()
    }

    override fun onResume() {
        if (mTrackingLocation) {
            startTrackingLocation()
        }
        super.onResume()
    }
}
