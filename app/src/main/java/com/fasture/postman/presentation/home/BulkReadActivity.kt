package com.fasture.postman.presentation.home

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import com.fasture.postman.R
import com.fasture.postman.databinding.FragmentBulkreadBinding


class BulkReadActivity : AppCompatActivity(),NfcAdapter.ReaderCallback {


    private val MIME_TEXT_PLAIN = "text/plain"
    private lateinit var binding: FragmentBulkreadBinding
    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(this)
    }
    private var txtId: String? = null
    private var txtCode:String? = null
    var buttonStatus = false
    private var collectIDCode: Map<String?, String?> = mapOf()

    var num = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentBulkreadBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setSupportActionBar(findViewById(R.id.a))

//        var adapter:ArrayAdapter<String> = ArrayAdapter(this, R.layout.simple_list_item_1, idcodelist)
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

        binding.resetBtn.setOnClickListener {
            if (collectIDCode.isNotEmpty()) {
                val intent = intent
                finish()
                startActivity(intent)
            }
        }


    }

    override fun onStart() {
        super.onStart()

        binding.resetBtn.setBackgroundResource(R.drawable.rounded_corner_disable)
        binding.submitBtn.setBackgroundResource(R.drawable.rounded_corner_disable)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // also reading NFC message from here in case this activity is already started in order
        // not to start another instance of this activity
        if (buttonStatus) {
//            receiveMessageFromDevice(intent)
            enableReaderMode(intent)
        }
    }

        private fun receiveMessageFromDevice(intent: Intent) {
            var collectData: List<String> = listOf()
            val action = intent.action

            if (NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
                val parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                with(parcelables) {
                    val inNdefMessage = this?.get(0) as NdefMessage
                    val inNdefRecords = inNdefMessage.records
                    val parcelId = inNdefRecords[0]
                    val parcelCode = inNdefRecords[1]
                    val id = String(parcelId.payload)
                    val code = String(parcelCode.payload)
                    txtCode = code.drop(3)
                    txtId = id.drop(3)


                }
            }

        }

        private fun makeText(id: String?, code: String?) {
//        idcodelist.add("\t"+id+"\t"+code)
//        binding.listView.adapter =  adapter
//        adapter.notifyDataSetChanged()
            if (id==null && code==null) return

            val txtnum = TextView(ContextThemeWrapper(this, R.style.Num))
            val txtid = TextView(ContextThemeWrapper(this, R.style.ID))
            val txtcode = TextView(ContextThemeWrapper(this, R.style.PostCode))
            txtnum.text = "$num"
            if (id != null) {
                if (id.length>21)
                    txtid.text = "${id.slice(0..21)}..."
            }else txtid.text = id
            txtcode.text = code
            num++
            val tr = TableRow(ContextThemeWrapper(this, R.style.BodyRow))

            tr.gravity = Gravity.CENTER_HORIZONTAL
            tr.addView(txtnum)
            tr.addView(txtid)
            tr.addView(txtcode)
            binding.showTable.addView(tr)
        }


        private fun enableForegroundDispatch(activity: AppCompatActivity, adapter: NfcAdapter?) {

            // here we are setting up receiving activity for a foreground dispatch
            // thus if activity is already started it will take precedence over any other activity or app
            // with the same intent filters

            val intent = Intent(activity.applicationContext, activity.javaClass)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

            val pendingIntent =
                PendingIntent.getActivity(activity.applicationContext, 0, intent, FLAG_MUTABLE)

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

        private fun enableReaderMode(intent: Intent) {
            val adapter = NfcAdapter.getDefaultAdapter(this)

            if (!adapter.isEnabled) {
                Toast.makeText(
                    applicationContext,
                    "Please enable NFC to test this.",
                    Toast.LENGTH_LONG
                )
                    .show()
            }

            val options = Bundle()
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 50)

            adapter.enableReaderMode(
                this,
                this,
                NfcAdapter.FLAG_READER_NFC_V,
                options
            )

        }

        override fun onResume() {
            super.onResume()

            // foreground dispatch should be enabled here, as onResume is the guaranteed place where app
            // is in the foreground

//        enableReaderMode(intent)
            binding.button.setOnClickListener {
                buttonStatus = !buttonStatus
                if (buttonStatus) {
//                enableForegroundDispatch(this, this.nfcAdapter)
//                receiveMessageFromDevice(intent)
                    enableReaderMode(intent)
                    if (txtId !in collectIDCode && txtId!=null &&txtCode!= null) {
                        collectIDCode += Pair(txtId, txtCode)
                        makeText(txtId, txtCode)
                    }
                    binding.button.text = "Stop Reading"
                } else {
                    binding.button.text = "Continue"
                    nfcAdapter?.disableReaderMode(this)
//                disableForegroundDispatch(this,this.nfcAdapter)
                }

            }
            if (buttonStatus) {
//            enableForegroundDispatch(this, this.nfcAdapter)
//            receiveMessageFromDevice(intent)
                enableReaderMode(intent)
            }

        }

//        private fun disableForegroundDispatch(activity: AppCompatActivity, adapter: NfcAdapter?) {
//            adapter?.disableForegroundDispatch(activity)
//        }


        override fun onPause() {
            super.onPause()
//        disableForegroundDispatch(this, this.nfcAdapter)
            nfcAdapter?.disableReaderMode(this)
        }

        override fun onTagDiscovered(tag: Tag?) {
            // Read and or write to Tag here to the appropriate Tag Technology type class
            // in this example the card should be an Ndef Technology Type
            val mNdef = Ndef.get(tag)

            // Check that it is an Ndef capable card
            if (mNdef != null) {

                // If we want to read
                // As we did not turn on the NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
                // We can get the cached Ndef message the system read for us.

                val mNdefMessage: NdefMessage = mNdef.cachedNdefMessage
                val inNdefRecords = mNdefMessage.records
                val parcelId = inNdefRecords[0]
                val parcelCode = inNdefRecords[1]
                val id = String(parcelId.payload)
                val code = String(parcelCode.payload)
                var txtCode = code.drop(3)
                var txtId = id.drop(3)

                if (txtId !in collectIDCode) {
                    collectIDCode += Pair(txtId, txtCode)
                    runOnUiThread {
                        makeText(txtId,txtCode)
                    }
                }
            }
            binding.resetBtn.setBackgroundResource(R.drawable.rounded_corner_enable)
            binding.submitBtn.setBackgroundResource(R.drawable.rounded_corner_enable)
        }
     }
