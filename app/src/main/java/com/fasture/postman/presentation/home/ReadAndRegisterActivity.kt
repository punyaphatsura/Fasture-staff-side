package com.fasture.postman.presentation.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fasture.postman.R
import com.fasture.postman.application.repository.server.Parcel
import com.fasture.postman.application.repository.server.ParcelData
import com.fasture.postman.databinding.FragmentReadnregisterBinding
import com.google.firebase.auth.FirebaseAuth


class ReadAndRegisterActivity : AppCompatActivity(){

    private var idSendBack:String = ""
    private var status:String = ""
    private val MIME_TEXT_PLAIN = "text/plain"
    private var exist:String = "0"
    private lateinit var binding: FragmentReadnregisterBinding
    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(this)
    }
    private var buttonStatus = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentReadnregisterBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val isNfcSupported: Boolean = this.nfcAdapter != null
        if (!isNfcSupported) {
            Toast.makeText(this, "Nfc is not supported on this device", Toast.LENGTH_SHORT).show()
            finish()
        } else if (!nfcAdapter?.isEnabled!!) {
            Toast.makeText(
                this,
                "NFC disabled on this device. Turn on to proceed",
                Toast.LENGTH_SHORT
            ).show()
        }



    }

    @SuppressLint("ResourceAsColor")
    override fun onStart() {
        super.onStart()
        binding.actButton.text = "Start Read"
        binding.txtSenderExample.text = ""
        binding.txtRecvExample.text = ""
        binding.txtCoorExample.text = ""
        binding.txtAddressHouseNo.text = ""
        binding.txtAddressDistrict.text = ""
        binding.txtAddressProvince.text = ""
        binding.txtAddressCountry.text = ""
        binding.txtPostcodeExample.text = ""
        binding.statusBtn.setBackgroundResource(R.drawable.rounded_corner_disable)
        binding.submitBtn.setBackgroundResource(R.drawable.rounded_corner_disable)
//        binding.statusBtn.setTextColor(R.color.txt_disable)
//        binding.submitBtn.setTextColor(R.color.txt_disable)

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
        val policy = ThreadPolicy.Builder()
            .permitAll().build()
        StrictMode.setThreadPolicy(policy)

        if (NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
            val parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            with(parcelables) {

                val inNdefMessage = this?.get(0) as NdefMessage
                val inNdefRecords = inNdefMessage.records
                val ndefRecord_0:NdefRecord = inNdefRecords[0]
                val inMessage = String(ndefRecord_0.payload)
                var txt = inMessage.drop(3)

//                MOCK
                val parcel: Parcel = Parcel()
                val mUser = FirebaseAuth.getInstance().currentUser
                mUser!!.getIdToken(true)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token: String? = task.result.token
                            // Send token to your backend via HTTPS
                            val dat = token?.let { parcel.getParcelInfo(txt, it) }
                            // ...
                            if (dat != null) {
                                idSendBack = txt
                                showData(dat)
                            } else {
                                MOCKgetData("Error")
                            }
                        } else {
                            MOCKgetData("Error")
                            // Handle error -> task.getException();
                        }
                    }
//                if (exist=="1")idSendBack = txt

            }
            binding.actButton.text = "Read Again"
            buttonStatus = !buttonStatus
            binding.statusBtn.setBackgroundResource(R.drawable.rounded_corner_enable)
//            Toast.makeText(this, "Reading Successful",Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDataMOCK(data: Array<String>) {
        binding.txtSenderExample.text = data[0]
        binding.txtRecvExample.text = data[1]
        binding.txtCoorExample.text = data[2]
        binding.txtAddressHouseNo.text = data[3]
        binding.txtPostcodeExample.text = data[4]
    }

    private fun showData(data: ParcelData) {
        binding.txtSenderExample.text = data.sender.name
        binding.txtRecvExample.text = data.receiver.name
        binding.txtCoorExample.text = "Lat: ${data.destination.receiverAddress?.geoLocation?.latitude.toString()}\n Long: ${data.destination.receiverAddress?.geoLocation?.longitude.toString()}"
        binding.txtAddressHouseNo.text = data.destination.receiverAddress?.houseNo
        binding.txtAddressDistrict.text = data.destination.receiverAddress?.district
        binding.txtAddressProvince.text = data.destination.receiverAddress?.province
        binding.txtAddressCountry.text = data.destination.receiverAddress?.country
        binding.txtPostcodeExample.text = data.destination.receiverAddress?.postalCode

    }

    private fun MOCKgetData(txt: String): Array<String> {
        var recv:String
        var sndr:String
        var coor:String
        var address:String
        var code:String
        when(txt){
            "Error" -> {
                recv = "Error"
                sndr = "Error"
                coor = "Error"
                address = "Error"
                code ="Error"
            }
            "P1" -> {
                recv = "Joe"
                sndr = "Jack"
                coor = "13.75085°N\n100.55130°E"
                address = "Bangkeaw\nBandPhli\nSamut-Prakarn\nThailand"
                code = "10540"
                exist = "1"
            }
            "P2" -> {
                recv = "Jee"
                sndr = "Jum"
                coor = "96.35210°N\n101.36521°E"
                address = "Makkasan\nRatchathewi\nBangkok\nThailand"
                code = "76000"
                exist = "1"
            }
            "P3" -> {
                recv = "Jub"
                sndr = "Joy"
                coor = "12.65984°N\n100.36527°E"
                address = "Puttamonton\nSalaya\nNakorn-Patom\nThailand"
                code = "00000"
                exist = "1"
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
                idSendBack = ""
                status = ""
                binding.txtSenderExample.text = ""
                binding.txtRecvExample.text = ""
                binding.txtCoorExample.text = ""
                binding.txtAddressHouseNo.text = ""
                binding.txtAddressDistrict.text = ""
                binding.txtAddressProvince.text = ""
                binding.txtAddressCountry.text = ""
                binding.txtPostcodeExample.text = ""
                binding.statusBtn.text = "Update Status"
                binding.actButton.text = "Stop Reading"
                binding.statusBtn.setBackgroundResource(R.drawable.rounded_corner_disable)
                binding.submitBtn.setBackgroundResource(R.drawable.rounded_corner_disable)
                enableForegroundDispatch(this, this.nfcAdapter)
                receiveMessageFromDevice(intent)
            }else{
                binding.actButton.text = "Start Read"
                disableForegroundDispatch(this,this.nfcAdapter)
            }

        }
        binding.submitBtn.setOnClickListener {
            if (idSendBack!="" && status!=""){
                statusUpdate(idSendBack,status)
                Toast.makeText(this,"Parcel ID: $idSendBack status updated",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this,"Please Select Status First",Toast.LENGTH_SHORT).show()
            }
        }

        binding.statusBtn.setOnClickListener {
            if (idSendBack!="") {
                val listItems = arrayOf("Paid", "On Delivery", "Sent")
                val mBuilder = AlertDialog.Builder(this@ReadAndRegisterActivity)
                mBuilder.setTitle("Choose Status")
                mBuilder.setSingleChoiceItems(listItems, -1) { dialogInterface, i ->
                    binding.statusBtn.text = "Status: ${listItems[i]}"
                    status = listItems[i]
                    if (status!="")binding.submitBtn.setBackgroundResource(R.drawable.rounded_corner_enable)
                    dialogInterface.dismiss()
                }
                // Set the neutral/cancel button click listener
                mBuilder.setNeutralButton("Cancel") { dialog, which ->
                    // Do something when click the neutral button
                    dialog.cancel()
                }

                val mDialog = mBuilder.create()
                mDialog.show()
            }else{
                Toast.makeText(this,"Please Scan Stamp First",Toast.LENGTH_SHORT).show()
            }


        }
    }

    private fun statusUpdate(idSendBack: String, status:String) {

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
