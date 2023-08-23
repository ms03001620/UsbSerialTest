package com.zwechat.usbserialtest

import android.hardware.usb.*

class MessageHelper(
    val manager: UsbManager,
    val device: UsbDevice,
    val callback: UsbHelper.OnUsbMessageEvent
) {
    private val logger = Logger.getLogger()

    private val TIMEOUT = 0
    private val forceClaim = true

    private var endpointOut: UsbEndpoint? = null
    private var endpointIn: UsbEndpoint? = null

    lateinit var connection: UsbDeviceConnection

    var readThread: Thread? = null

    init {
        logger.d("device?.interfaceCount:${device.interfaceCount}")
        assert(device.interfaceCount == 1)
        assert(manager.hasPermission(device))

        device.getInterface(0).also { intf ->
            logger.d("device?.endpointCount:${intf.endpointCount}")

            for (endIndex in 0 until intf.endpointCount) {
                val endpoint = intf.getEndpoint(endIndex)
                logger.d("endpoint:${makeUsbEndpointInfo(endpoint)}")

                if (endpoint.direction == UsbConstants.USB_DIR_OUT && //主机到设备
                    endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK // 批量端点
                ) {
                    endpointOut = endpoint
                }

                if (endpoint.direction == UsbConstants.USB_DIR_IN && //主机到设备
                    endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK // 批量端点
                ) {
                    endpointIn = endpoint
                }
            }

            assert(endpointIn != null)
            assert(endpointOut != null)

            createConn(intf)
            try {
                createRecv()
            }catch (e: Throwable){
                e.printStackTrace()
            }

        }
    }

    private fun createRecv() {
        readThread = Thread(Runnable {
            while (readThread?.isInterrupted == false) {
                try {
                    Thread.sleep(100)
                } catch (e: Throwable) {
                    break
                }
                val bytes = ByteArray(endpointIn!!.maxPacketSize)
                val lines = connection.bulkTransfer(endpointIn, bytes, endpointIn!!.maxPacketSize, 100)
                if (lines >= 0) {
                    val recv = String(bytes, 0, lines)
                    callback.onLogs("接收字符:$recv")
                    logger.d("recv:$recv")
                }
            }
            callback.onBind("读取已关闭")
        })
        readThread?.start()
    }

    private fun createConn(intf: UsbInterface){
        connection = manager.openDevice(device)
        assert(connection != null)
        val claim = connection.claimInterface(intf, forceClaim)
        assert(claim)
    }

    fun sendMessage(message: String) {
        Thread {
            val bytes = message.toByteArray(charset = Charsets.UTF_8)
            val ref = connection.bulkTransfer(endpointOut, bytes, bytes.size, TIMEOUT) //do in another thread
            callback.onLogs("发送长度:$ref")
        }.start()
    }

    private fun makeUsbEndpointInfo(usbEndpoint: UsbEndpoint): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("type:${usbEndpoint.type}")
        stringBuilder.append(", ")
        stringBuilder.append("direction:${usbEndpoint.direction}")
        stringBuilder.append(", ")
        stringBuilder.append("attributes:${usbEndpoint.attributes}")
        return stringBuilder.toString()
    }

    fun release() {
        connection.close()
        readThread?.interrupt()
    }
}