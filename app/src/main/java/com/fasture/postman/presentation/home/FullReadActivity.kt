package com.fasture.postman.presentation.home

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.fasture.postman.application.repository.server.Parcel
import com.fasture.postman.application.repository.server.ParcelData
import com.fasture.postman.databinding.FragmentFullreadBinding
import com.google.firebase.auth.FirebaseAuth

class FullReadActivity : AppCompatActivity(){


    private val MIME_TEXT_PLAIN = "text/plain"
    private lateinit var binding: FragmentFullreadBinding
    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(this)
    }
    var buttonStatus = false



    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = FragmentFullreadBinding.inflate(layoutInflater)
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
        binding.actButton.text = "Start Read"
        binding.txtSenderExample.text = ""
        binding.txtRecvExample.text = ""
        binding.txtCoorExample.text = ""
        binding.txtAddressHouseNo.text = ""
        binding.txtPostcodeExample.text = ""

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
                val ndefRecord_0:NdefRecord = inNdefRecords[0]
                val inMessage = String(ndefRecord_0.payload)
                val txt = inMessage.drop(3)
                val data = MOCKgetData(txt)
                val parcel = Parcel()
                val mUser = FirebaseAuth.getInstance().currentUser
                mUser!!.getIdToken(true)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token: String? = task.result.token
                            // Send token to your backend via HTTPS
                            val dat = token?.let { parcel.getParcelInfo(txt, it) }
                            // ...
                            if (dat != null) {
                                showData(dat)
                            }
                        } else {
                            MOCKgetData("0")
                            // Handle error -> task.getException();
                        }
                    }





            }
            binding.actButton.text = "Read Again"
            buttonStatus = !buttonStatus
            Toast.makeText(this, "Reading Successful",Toast.LENGTH_SHORT).show()
        }
    }

    private fun showData(data: ParcelData?) {
        if (data !=null) {
            binding.txtSenderExample.text = data.sender.name
            binding.txtRecvExample.text = data.receiver.name
            binding.txtCoorExample.text = "Lat: ${data.destination.receiverAddress?.geoLocation?.latitude.toString()} Long: ${data.destination.receiverAddress?.geoLocation?.longitude.toString()}"
            binding.txtAddressHouseNo.text = "HouseNO: ${data.destination.receiverAddress?.houseNo}"
            binding.txtPostcodeExample.text = data.destination.receiverAddress?.postalCode

        } else {
            MOCKgetData("0")
        }
    }

    private fun MOCKgetData(txt: String): Array<String> {
        var recv:String
        var sndr:String
        var coor:String
        var address:String
        var code:String
        when(txt){
            "0" -> {
                recv = "Error"
                sndr = "Error"
                coor = "Error"
                address = "Error"
                code ="Error"
            }
            "1" -> {
                recv = "Joe"
                sndr = "Jack"
                coor = "13.75085°N\n100.55130°E"
                address = "Bangkeaw\nBandPhli\nSamut-Prakarn\nThailand"
                code = "10540"
            }
            "2" -> {
                recv = "Jee"
                sndr = "Jum"
                coor = "96.35210°N\n101.36521°E"
                address = "Makkasan\nRatchathewi\nBangkok\nThailand"
                code = "76000"
            }
            "3" -> {
                recv = "Jub"
                sndr = "Joy"
                coor = "12.65984°N\n100.36527°E"
                address = "Puttamonton\nSalaya\nNakorn-Patom\nThailand"
                code = "00000"
            }
            else -> {
                recv = "ID NOT FOUND!!"
                sndr = "ID NOT FOUND!!"
                coor = "ID NOT FOUND!!"
                address = "ID NOT FOUND!!"
                code = "ID NOT FOUND!!"
            }
        }
        return arrayOf(sndr,recv,coor,address,code)
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

        binding.actButton.setOnClickListener {
            buttonStatus = !buttonStatus
            if (buttonStatus){
                binding.txtSenderExample.text = ""
                binding.txtRecvExample.text = ""
                binding.txtCoorExample.text = ""
                binding.txtAddressHouseNo.text = ""
                binding.txtPostcodeExample.text = ""
                binding.actButton.text = "Stop Reading"
                enableForegroundDispatch(this, this.nfcAdapter)
                receiveMessageFromDevice(intent)
            }else{
                binding.actButton.text = "Start Read"
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
