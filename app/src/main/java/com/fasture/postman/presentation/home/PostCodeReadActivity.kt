package com.fasture.postman.presentation.home

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.fasture.postman.R
import com.fasture.postman.databinding.FragmentCodereadBinding

class PostCodeReadActivity : AppCompatActivity(){


    private val MIME_TEXT_PLAIN = "text/plain"
    private lateinit var binding: FragmentCodereadBinding
    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(this)
    }
    var buttonStatus = false
    var result:List<String> = listOf()



    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = FragmentCodereadBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val isNfcSupported: Boolean = this.nfcAdapter != null
        if (!isNfcSupported) {
            Toast.makeText(this, "Nfc is not supported on this device", Toast.LENGTH_SHORT).show()
            finish()
        }else if (!nfcAdapter?.isEnabled!!) {
            Toast.makeText(
                this,
                "NFC disabled on this device. Turn on to proceed",
                Toast.LENGTH_SHORT
            ).show()
        }


    }

    override fun onStart() {
        super.onStart()
        binding.btn.text = "Start Read"
        binding.postcode.text = ""
    }
//    var tvIncomingMessage = findViewById<TextView>(R.id.txtAddressExample)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // also reading NFC message from here in case this activity is already started in order
        // not to start another instance of this activity
        if (buttonStatus) {
            receiveMessageFromDevice(intent)
        }
    }

    private fun receiveMessageFromDevice(intent: Intent) {
        var collectData:List<String> = listOf()
        val action = intent.action

        if (NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
            val parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            with(parcelables) {

                val inNdefMessage = this?.get(0) as NdefMessage
                val inNdefRecords = inNdefMessage.records
                val ndefRecord_0:NdefRecord = inNdefRecords[1]
                val inMessage = String(ndefRecord_0.payload)
                var txt = inMessage.drop(3)
                binding.postcode.text = txt

            }
            binding.btn.text = "Read Again"
            buttonStatus = !buttonStatus
//            Toast.makeText(this, "Reading Successful",Toast.LENGTH_SHORT).show()
        }
    }

    private fun enableForegroundDispatch(activity: AppCompatActivity, adapter: NfcAdapter?) {

        // here we are setting up receiving activity for a foreground dispatch
        // thus if activity is already started it will take precedence over any other activity or app
        // with the same intent filters

        val intent = Intent(activity.applicationContext, activity.javaClass)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent.getActivity(activity.applicationContext, 0, intent, 0)

        val filters = arrayOfNulls<IntentFilter>(1)
        val techList = arrayOf<Array<String>>()

        filters[0] = IntentFilter()
        with(filters[0]) {
            this?.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED)
            this?.addCategory(Intent.CATEGORY_DEFAULT)
            try {
                this?.addDataType(MIME_TEXT_PLAIN)
            } catch (ex: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("Check your MIME type")
            }
        }

        adapter?.enableForegroundDispatch(activity, pendingIntent, filters, techList)
    }

    override fun onResume() {
        super.onResume()

        // foreground dispatch should be enabled here, as onResume is the guaranteed place where app
        // is in the foreground

        binding.btn.setOnClickListener {
            buttonStatus = !buttonStatus
            if (buttonStatus){
                binding.postcode.text = ""
                binding.btn.text = "Stop Reading"
                enableForegroundDispatch(this, this.nfcAdapter)
                receiveMessageFromDevice(intent)
            }else{
                binding.btn.text = "Start Read"
                disableForegroundDispatch(this,this.nfcAdapter)
            }

        }
    }

    private fun disableForegroundDispatch(activity: AppCompatActivity, adapter: NfcAdapter?) {
        adapter?.disableForegroundDispatch(activity)
    }


    override fun onPause() {
        super.onPause()
        disableForegroundDispatch(this, this.nfcAdapter)
    }

    override fun onRestart() {
        super.onRestart()
        disableForegroundDispatch(this, this.nfcAdapter)
    }
}
