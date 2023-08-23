package com.zwechat.usbserialtest

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

class UsbHelper(
    private val manager: UsbManager,
    private val callback: OnUsbMessageEvent
) {
    private var usbDevice: UsbDevice? = null
    private var helper: MessageHelper? = null

    fun bindDevice() {
        var target: UsbDevice? = null
        manager.deviceList?.forEach {
            //mVendorId=1659, mProductId=9123
            if (it.value.vendorId == 1659 && it.value.productId == 9123) {
                target = it.value
            }
        }

        if (target == null) {
            callback.onBind("未找到Usb设备请检查插线是否松动")
        } else {
            callback.onBind("设备已绑定: ${target!!.deviceName}")
        }
        usbDevice = target
        initMessageHelper()
    }

    private fun initMessageHelper() {
        usbDevice?.let {
            helper = MessageHelper(manager, usbDevice!!, callback)
        }
    }

    fun sendMessage(message: String) {
        helper?.sendMessage(message)
    }

    fun release() {
        helper?.release()
        helper = null
    }

    interface OnUsbMessageEvent {
        fun onBind(message: String)
        fun onLogs(log: String)
    }
}