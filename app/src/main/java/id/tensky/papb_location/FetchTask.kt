package id.tensky.papb_location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.text.TextUtils
import android.util.Log
import java.io.IOException
import java.util.*

class FetchTask(appContext: Context, mListener:OnTaskCompleted) : AsyncTask<Location, Void, String>() {
    val geocoder = Geocoder(appContext, Locale.getDefault())
    val onTaskListener = mListener
    override fun doInBackground(vararg params: Location?): String? {
        val location = params[0]
        var resultMessage = ""
        var addresses = listOf<Address>()
        try {
            addresses = geocoder.getFromLocation(
                location!!.getLatitude(),
                location.getLongitude(),
                1);
        }catch (ioException:IOException) {
            Log.e("Locss", "not available", ioException);
        }catch (illegalArgumentException:IllegalArgumentException) {
        }

        if (addresses.size == 0) {
            if (resultMessage.isEmpty()) {
                resultMessage = "No Adress found"
                Log.e("Locss", resultMessage)
            }
        } else {
            val address = addresses[0]
            val addressParts =
                ArrayList<String?>()

            for (i in 0..address.maxAddressLineIndex) {
                addressParts.add(address.getAddressLine(i))
            }
            resultMessage = TextUtils.join(
                "\n",
                addressParts
            )
        }

        return resultMessage
    }

    override fun onPostExecute(result: String?) {
        onTaskListener.onTaskCompleted(result)
        super.onPostExecute(result)
    }
    public interface OnTaskCompleted {
        fun onTaskCompleted(result: String?)
    }
}