package com.zwechat.usbserialtest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

class MainActivity : AppCompatActivity() {
    private lateinit var textLogs: TextView
    private lateinit var editSend: EditText
    private lateinit var btnSend: Button
    private var index = 0
    private val logger = Logger.getLogger()
    private lateinit var usbHelper: UsbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        initHelper()
        reg()
        appendLogs("程序已启动")
        usbHelper.bindDevice()
    }

    private fun initViews() {
        textLogs = findViewById(R.id.text_logs)
        editSend = findViewById(R.id.edit_send)
        btnSend = findViewById(R.id.btn_send)

        btnSend.setOnClickListener {
            if (editSend.text.isEmpty()) {
                appendLogs("无法发送空消息")
            } else {
                val message = editSend.text.toString()
                usbHelper.sendMessage(message)
            }
        }

        findViewById<View>(R.id.btn_release).setOnClickListener {
            usbHelper.release()
        }

        findViewById<View>(R.id.btn_exit).setOnClickListener {
            finish()
        }
    }

    private fun initHelper() {
        usbHelper =
            UsbHelper(getSystemService(Context.USB_SERVICE) as UsbManager, object : UsbHelper.OnUsbMessageEvent {
                override fun onBind(message: String) {
                    runOnUiThread {
                        editSend.isEnabled = message.contains("已绑定")
                        btnSend.isEnabled = message.contains("已绑定")
                    }
                    appendLogs(message)
                }

                override fun onLogs(log: String) {
                    appendLogs(log)
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        usbHelper.release()
        unregisterReceiver(usbReceiver)
    }

    private fun appendLogs(text: String) {
        textLogs.postDelayed({
            val old = textLogs.text
            textLogs.text = "$old\n${++index}.  $text"
        }, 100)
    }

    private fun reg() {
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        registerReceiver(usbReceiver, filter)
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            logger.d("onReceive action:${intent.action}")
            if (ACTION_USB_PERMISSION == intent.action) {
                appendLogs("USB使用权限授权成功")
            }
            if (UsbManager.ACTION_USB_DEVICE_DETACHED == intent.action) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                appendLogs("USB设备移除 ${device?.deviceName ?: "null"}")
                usbHelper.release()
            }

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED == intent.action) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                appendLogs("USB设备添加 ${device?.deviceName ?: "null"}")
                usbHelper.bindDevice()
            }
        }
    }
}