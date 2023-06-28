package com.obelab.repace.service.ble

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.*
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.util.Constants
import com.obelab.repace.core.util.FileHelper
import com.obelab.repace.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


//real repace uuid
private var SERVICE_UUID =
    if (Constants.IS_TEST_EMULATOR) "0000FFE0-0000-1000-8000-00805F9B34FB" else "6e400001-b5a3-f393-e0a9-e50e24dcca9e" //real repace
private var RX_UUID =
    if (Constants.IS_TEST_EMULATOR) "0000FFE1-0000-1000-8000-00805F9B34FB" else "6e400002-b5a3-f393-e0a9-e50e24dcca9e"
private var TX_UUID =
    if (Constants.IS_TEST_EMULATOR) "0000FFE1-0000-1000-8000-00805F9B34FB" else "6e400003-b5a3-f393-e0a9-e50e24dcca9e"
private var CCC_UUID =
    if (Constants.IS_TEST_EMULATOR) "0000FFE1-0000-1000-8000-00805F9B34FB" else "00002902-0000-1000-8000-00805f9b34fb"


//test mi band
//private const val SERVICE_UUID = "00001800-0000-1000-8000-00805f9b34fb" // test mi band
//private const val RX_UUID = "00002a39-0000-1000-8000-00805f9b34fb"
//private const val TX_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e"
//private const val CCC_UUID = "00002902-0000-1000-8000-00805f9b34fb"
/////


private const val UUID_SERVICE_HEARTRATE = "0000180d-0000-1000-8000-00805f9b34fb"


class BleService : Service() {

    private val mBinder = LocalBinder()

    var mConnectionState: Int = STATE_DISCONNECTED
    private var isPairing = false;
    private var resultScan: ScanResult? = null
    private var connectedGatt: BluetoothGatt? = null
    private var characteristicForRead: BluetoothGattCharacteristic? = null
    private var characteristicForWrite: BluetoothGattCharacteristic? = null
    private var characteristicForIndicate: BluetoothGattCharacteristic? = null

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    companion object {


        const val STATE_DISCONNECTED = 0
        const val STATE_CONNECTING = 1
        const val STATE_CONNECTED = 2
        var connectionState = STATE_DISCONNECTED

        const val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
        const val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"
        const val TYPE_DATA = "com.example.bluetooth.le.TYPE_DATA"
        const val ACTION_GATT_SCAN_RESULT = "com.obelab.repace.service.ble.ACTION_GATT_SCAN_RESULT"
        const val ACTION_GATT_CONNECTED = "com.obelab.repace.service.ble.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED =
            "com.obelab.repace.service.ble.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.obelab.repace.service.ble.ACTION_GATT_SERVICES_DISCOVERED"
        const val TAG = "BleService";
    }

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): BleService = this@BleService
    }

    override fun onCreate() {
        super.onCreate()
        Functions.showLog(TAG, "In onCreate")
        var macAddress = ""
        try {
            macAddress = PrefManager.getDeviceMacAddress()
        } catch (e: Exception) {
            Functions.showLog(TAG, "Error Get Device Mac Address: $e")
        }
        if (macAddress != "") {
            connect(macAddress)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        Functions.showLog(TAG, "In onBind")
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Functions.showLog("onStartCommand")
        return START_STICKY
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Functions.showLog(TAG, "In onUnBind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Functions.showLog(TAG, "In onDestroy")
        super.onDestroy()
    }

    @SuppressLint("MissingPermission")
    fun bleRestartLifecycle() {
        Functions.showLog(TAG, "In bleRestartLifecycle")
        connectedGatt?.disconnect()
        bleEndLifecycle()
    }

    @SuppressLint("MissingPermission")
    fun bleEndLifecycle() {
        Functions.showLog(TAG, "In bleEndLifecycle")

        safeStopBleScan()
        connectedGatt?.close()
        setConnectedGattToNull()
    }

    @SuppressLint("MissingPermission")
    fun safeStopBleScan() {
        Functions.showLog(TAG, "In safeStopBleScan")
        if (!isPairing) {
            Functions.showLog(TAG, "Already stopped")
            return
        }
        Functions.showLog(TAG, "Stopping BLE scan")
        isPairing = false
        bluetoothAdapter.bluetoothLeScanner.stopScan(bleScanCallback)
    }

    private fun setConnectedGattToNull() {
        Functions.showLog(TAG, "In setConnectedGattToNull")
        connectedGatt = null
        characteristicForRead = null
        characteristicForWrite = null
        characteristicForIndicate = null
    }


    @SuppressLint("MissingPermission")
    fun StartBLEScan() {
        val scanFilter: ScanFilter
        if (Constants.IS_FILTER == true) {
            //Filter with UUID

            scanFilter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(UUID.fromString(SERVICE_UUID)))
                .build()

        } else {
            //No filter
            scanFilter = ScanFilter.Builder().build()
        }

        val scanSettings =
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        Functions.showLog(TAG, "Start scan")
        val serviceFilter = scanFilter.serviceUuid?.uuid.toString()
        Functions.showLog(TAG, "Starting BLE scan, filter: $serviceFilter")
        bluetoothAdapter.bluetoothLeScanner?.startScan(
            mutableListOf(scanFilter),
            scanSettings,
            bleScanCallback
        )
    }

    private val bleScanCallback: ScanCallback by lazy {
        object : ScanCallback() {
            @SuppressLint("MissingPermission")
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                val bluetoothDevice = result?.device
                if (bluetoothDevice != null) {
                    if (bluetoothDevice?.name != null) {
                        val name = bluetoothDevice?.name
                        val address = bluetoothDevice.address
                        Functions.showLog("onScanResult ${bluetoothDevice?.name} devices Address ${bluetoothDevice?.address}")
                        //result.device.connectGatt(this@BleService, false, bluetoothGattCallback)
                        resultScan = result
                        val intent = Intent(ACTION_GATT_SCAN_RESULT)
                        intent.putExtra(
                            EXTRA_DATA,
                            DeviceInfoModel(address.toString(), name.toString())
                        )
                        sendBroadcast(intent)
                    }
                }
            }
        }
    }

    //Connect device
    @SuppressLint("MissingPermission")
    fun connect(address: String): Boolean {
        Functions.showLog(TAG, "connect to device:  ${address}")
        bluetoothAdapter?.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(address)
                //connect to the GATT server on the device
                connectedGatt = device.connectGatt(
                    this,
                    true,
                    bluetoothGattCallback,
                    BluetoothDevice.TRANSPORT_LE
                )
                //connectedGatt = resultScan?.device?.connectGatt(this, false, bluetoothGattCallback)
                //resultScan?.device?.connectGatt(this, false, bluetoothGattCallback)
                Functions.showLog(TAG, "log info gatt ${connectedGatt}")
                mConnectionState = STATE_CONNECTING
                connectionState = STATE_CONNECTING
                Functions.showLog(TAG, "Device  provided address. ${device}")
                return true
            } catch (exception: IllegalArgumentException) {
                Functions.showLog(
                    TAG,
                    "Device not found with provided address.  Unable to connect."
                )
                return false
            }
        } ?: run {
            Functions.showLog("BluetoothAdapter not initialized")
            return false
        }
    }

    //BLE Gatt Callback
    private val bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Functions.showLog(TAG, "onConnectionStateChange from : ${status} to: ${newState}")
            val deviceAddress = gatt.device.address
            val deviceName = gatt.device.name
            val intentAction: String
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED
                mConnectionState = STATE_CONNECTED
                connectionState = STATE_CONNECTED
                Functions.showLog(TAG, "Connected to a $deviceName , $deviceAddress")
                PrefManager.saveDeviceMacAddress(deviceAddress)
                broadcastUpdate(intentAction)
                // TODO: bonding state
                Handler(Looper.getMainLooper()).post {
                    Functions.showLog(TAG, "discoverServices: ${gatt.toString()}")
                    gatt.discoverServices()
                }
                bluetoothAdapter.bluetoothLeScanner.stopScan(bleScanCallback)
                broadcastUpdate(intentAction)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED
                mConnectionState = STATE_DISCONNECTED
                connectionState = STATE_DISCONNECTED
                Functions.showLog(TAG, "Disconnected from ${deviceName}, $deviceAddress")
                setConnectedGattToNull()
                gatt.close()
                broadcastUpdate(intentAction)
                bleRestartLifecycle()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Functions.showLog(TAG, "onServicesDiscovered received: " + gatt)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                checkAvailableServices()
                enableTXNotification(gatt)
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            } else {
                Functions.showLog(TAG, "onServicesDiscovered received: " + status)
            }

            val service = gatt.getService(UUID.fromString(SERVICE_UUID)) ?: run {
                Functions.showLog(TAG, "ERROR: Service not found, disconnecting")
                gatt.disconnect()
                return
            }
            connectedGatt = gatt
            characteristicForWrite = service.getCharacteristic(UUID.fromString(RX_UUID))
            //characteristicForIndicate = service.getCharacteristic(UUID.fromString(CHAR_FOR_INDICATE_UUID))

        }

        private fun checkAvailableServices() {
            for (service in connectedGatt?.services.orEmpty()) {
                Functions.showLog(TAG, "onServicesDiscovered: ${service.uuid}")

                for (characteristic in service.characteristics) {
                    Functions.showLog(TAG, "  char: ${characteristic.uuid}")

                    for (descriptor in characteristic.descriptors) {
                        Functions.showLog(TAG, "    descriptor: ${descriptor.uuid}")
                    }
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int,
        ) {
            FileHelper.saveLog("On receive data from:", characteristic.uuid.toString())

            if (status == BluetoothGatt.GATT_SUCCESS) {
                FileHelper.saveLog("Broadcast update", "Update success")
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
            FileHelper.saveLog(
                "onCharacteristicRead type Array byte",
                characteristic.value.toString()
            )
            FileHelper.saveLog(
                "onCharacteristicRead type Hex string",
                Functions.toHexString(characteristic.value)
            )

            //Check uuid read
            val strValue = characteristic.value.toString(Charsets.UTF_8)
            val log = "onCharacteristicRead " + when (status) {
                BluetoothGatt.GATT_SUCCESS -> "OK, value=\"$strValue\""
                BluetoothGatt.GATT_READ_NOT_PERMITTED -> "not allowed"
                else -> "error $status"
            }
            Functions.showLog(TAG, log)
            FileHelper.saveLog("ReadData", log)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            Functions.showLog(TAG, "onCharacteristicWrite")

            if (characteristic.uuid == UUID.fromString(RX_UUID)) {
                val log: String = "onCharacteristicWrite " + when (status) {
                    BluetoothGatt.GATT_SUCCESS -> "OK"
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> "not allowed"
                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> "invalid length"
                    else -> "error $status"
                }
                Functions.showLog(TAG, log)
            } else {
                Functions.showLog(TAG, "onCharacteristicWrite unknown uuid $characteristic.uuid")
            }

            super.onCharacteristicWrite(gatt, characteristic, status)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
        ) {
            Functions.showLog(TAG, "onCharacteristicChanged")
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }
    }


    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }


    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
        Functions.showLog(TAG, "broadcastUpdate")
        val data = trimRepaceData(characteristic.value)
        if (Constants.IS_WRITE_LOG_LTTEST){
            FileHelper.saveLogData("" + Functions.toHexString(data))
        }
        Functions.showLog(TAG, "received data: ${Functions.toHexString(data)}")
        try {
            pairRepaceData(data, action)
            if (data.size > (data[4].toInt() + 6)) { // 6 is sum Header and Checksum
                val subData = Arrays.copyOfRange(data, (data[4].toInt() + 6), data.size)
                Functions.showLog(TAG, "received sub data -> ${Functions.toHexString(subData)}")
                pairRepaceData(subData, action)
            }
        } catch (e: Exception) {
            Functions.showLog("pairRepaceData Error -> $e")
        }
    }

    //Lam sau tet 
//    private fun pairMeasureData(data: ByteArray, action: String) {
//
//    }

    private fun pairRepaceData(data: ByteArray, action: String) {
        try {
            val intent = Intent(action)
            when (data[3]) {
                RepaceData.TYPE_MEASURE_DATA.toByte() -> {
                    var dataMeasure = data
                    if (Constants.IS_TEST_EMULATOR) {
                        dataMeasure = byteArrayOf(
                            0x0a,
                            0x0b,
                            0x0c,
                            0x01,
                            0x42,
                            0x00,
                            0x00,
                            0x0f,
                            0xb9.toByte(),
                            0x00,
                            0x68,
                            0x00,
                            0x23,
                            0x00,
                            0x00,
                            0x00,
                            0x02,
                            0x00,
                            0x38,
                            0x00,
                            0x13,
                            0xff.toByte(),
                            0xf0.toByte(),
                            0xff.toByte(),
                            0xfe.toByte(),
                            0x00,
                            0x1d,
                            0x00,
                            0x0c,
                            0x00,
                            0x00,
                            0x00,
                            0x00,
                            0x00,
                            0x2c,
                            0x00,
                            0x16,
                            0xff.toByte(),
                            0xfa.toByte(),
                            0x00,
                            0x00,
                            0x00,
                            0xa9.toByte(),
                            0x00,
                            0x5b,
                            0x00,
                            0x00,
                            0x00,
                            0x00,
                            0x00,
                            0x8a.toByte(),
                            0x00,
                            0x37,
                            0xff.toByte(),
                            0xfc.toByte(),
                            0xff.toByte(),
                            0xff.toByte(),
                            0x04,
                            0x0e,
                            0xff.toByte(),
                            0xd7.toByte(),
                            0x00,
                            0x1d.toByte(),
                            0xf8.toByte(),
                            0xff.toByte(),
                            0x01,
                            0x31,
                            0xfe.toByte(),
                            0xdd.toByte(),
                            0xff.toByte(),
                            0xc8.toByte(),
                            0x7e.toByte()
                        )
                        dataMeasure[57] = data[5]
                        dataMeasure[58] = data[6]
                    }
                    val length = dataMeasure[4]
                    val sequence = arrayListOf<Byte>()
                    val rSO2 = arrayListOf<Byte>()
                    val accel = arrayListOf<Byte>()
                    val gyro = arrayListOf<Byte>()
                    val rawData = ByteArray(48)
                    val channel1 = arrayListOf<Byte>()
                    val channel2 = arrayListOf<Byte>()
                    val channel3 = arrayListOf<Byte>()
                    val channel4 = arrayListOf<Byte>()
                    val channel5 = arrayListOf<Byte>()
                    val channel6 = arrayListOf<Byte>()

                    for (i in 5 until 8) {
                        sequence.add(dataMeasure[i])
                    }

                    for (i in 9 until 56) {
                        rawData[i - 9] = dataMeasure[i]
                    }

                    for (i in 9 until 16) {
                        channel1.add(dataMeasure[i])
                    }

                    for (i in 17 until 24) {
                        channel2.add(dataMeasure[i])
                    }

                    for (i in 25 until 32) {
                        channel3.add(dataMeasure[i])
                    }

                    for (i in 33 until 40) {
                        channel4.add(dataMeasure[i])
                    }

                    for (i in 41 until 48) {
                        channel5.add(dataMeasure[i])
                    }

                    for (i in 49 until 56) {
                        channel6.add(dataMeasure[i])
                    }

                    rSO2.add(dataMeasure[57])
                    rSO2.add(dataMeasure[58])

                    for (i in 59 until 64) {
                        accel.add(dataMeasure[i])
                    }

                    for (i in 65 until 70) {
                        gyro.add(dataMeasure[i])
                    }

                    val result = RepaceMeasure(
                        Constants.TYPE_MEASURE_DATA,
                        dataMeasure,
                        rawData,
                        length,
                        sequence,
                        channel1,
                        channel2,
                        channel3,
                        channel4,
                        channel5,
                        channel6,
                        rSO2,
                        accel,
                        gyro
                    )
                    Functions.showLog(
                        TAG,
                        result.toString() + ", Sm02: ${Functions.convertRSO2(rSO2)}"
                    )
                    intent.putExtra(TYPE_DATA, Constants.TYPE_MEASURE_DATA)
                    intent.putExtra(EXTRA_DATA, result)
                }
                RepaceData.TYPE_BATTERY_LEVEL.toByte() -> {
                    val rawData = arrayListOf<Byte>()
                    val lenght = data[4]
                    val status = if (data[5].toInt() == 0x01) "discharge" else "charging"
                    rawData.add(data[6])
                    rawData.add(data[7])
                    val level = data[8]
                    val result = RepaceBattery(
                        Constants.TYPE_BATTERY_LEVEL,
                        data,
                        lenght,
                        status,
                        rawData,
                        level
                    )
                    Functions.showLog(TAG, "RepaceBattery: ${result.toString()}")
                    intent.putExtra(TYPE_DATA, Constants.TYPE_BATTERY_LEVEL)
                    intent.putExtra(EXTRA_DATA, result)
                }
                RepaceData.TYPE_STATUS_VERSION.toByte() -> {
                    val dataType = Constants.TYPE_STATUS_VERSION
                    val data = data
                    var length = data[4]
                    val status = if (data[5].toInt() == 0x00) "Normal" else "OTA mode"
                    val version = arrayListOf<Byte>()
                    for (i in 6 until 13) {
                        version.add(data[i])
                    }

                    val result = RepaceStatus(dataType, data, length, status, version)
                    Functions.showLog(TAG, "RepaceStatus: ${result.toString()}")
                    intent.putExtra(TYPE_DATA, Constants.TYPE_STATUS_VERSION)
                    intent.putExtra(EXTRA_DATA, result)
                }
                RepaceData.TYPE_GAIN_DATA.toByte() -> {
                    val dataType = Constants.TYPE_GAIN_DATA
                    val data = data
                    val length = data[4]
                    val result =
                        if (data[5].toInt() == 0x01) "Success" else "Fail" // 0x01-> Success   0x02-> Fail
                    val repaceGain = RepaceGain(dataType, data, length, result)
                    Functions.showLog(TAG, "RepaceGain: ${repaceGain.toString()}")
                    if (result == "Success") {
                        intent.putExtra(TYPE_DATA, Constants.TYPE_GAIN_DATA)
                        intent.putExtra(EXTRA_DATA, repaceGain)
                    }
                }
                RepaceData.TYPE_ERROR.toByte() -> {
                    val dataType = Constants.TYPE_ERROR
                    val data = data
                    val lastReceive = data[4]
                    val errorCode = data[5]
                    val result = RepaceError(dataType, data, lastReceive, errorCode)
                    Functions.showLog(TAG, "RepaceError: ${result.toString()}")
                    intent.putExtra(TYPE_DATA, Constants.TYPE_ERROR)
                    intent.putExtra(EXTRA_DATA, result)
                }
            }
            sendBroadcast(intent)
        } catch (e: Exception) {
            Functions.showLog("pairRepaceDataError -> $e")
        }
    }

    @SuppressLint("MissingPermission")
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        if (bluetoothAdapter == null || connectedGatt == null) {
            Functions.showLog(TAG, "BluetoothAdapter not initialized")
            return
        }
        connectedGatt!!.readCharacteristic(characteristic)
    }


    @SuppressLint("MissingPermission")
    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean,
    ) {
        if (bluetoothAdapter == null || connectedGatt == null) {
            Functions.showLog(TAG, "BluetoothAdapter not initialized")
            return
        }
        connectedGatt!!.setCharacteristicNotification(characteristic, enabled)

        // This is specific to Heart Rate Measurement.
        if (UUID.fromString(SERVICE_UUID) == characteristic.uuid) {
            val descriptor = characteristic.getDescriptor(
                UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG)
            )
            descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            connectedGatt!!.writeDescriptor(descriptor)
        }
    }

    fun enableTXNotification(gatt: BluetoothGatt) {
        Functions.showLog(TAG, "enableTXNotification")
        val RxService: BluetoothGattService = gatt.getService(UUID.fromString(SERVICE_UUID))
        if (RxService == null) {

            Functions.showLog(TAG, "\"Rx service not found!\"")
            return
        }
        val txChar = RxService
            .getCharacteristic(UUID.fromString(TX_UUID))
        if (txChar == null) {
            Functions.showLog(TAG, "Tx charateristic not found!")
            return
        }
        gatt.setCharacteristicNotification(txChar, true)
        val descriptor = txChar.getDescriptor(UUID.fromString(CCC_UUID))
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        gatt.writeDescriptor(descriptor)
    }


    val supportedGattServices: List<BluetoothGattService>?
        get() {
            if (connectedGatt == null) return null

            return connectedGatt!!.services
        }

    fun trimRepaceData(byteArrayData: ByteArray): ByteArray {
        var correctArrayData: ByteArray = byteArrayData
        if (byteArrayData.count() > 6) {
            for (index in 0..byteArrayData.count() - 3) {
                if (byteArrayData[index] == (0x0a).toByte() && byteArrayData[index + 1] == (0x0b).toByte() && byteArrayData[index + 2] == (0x0c).toByte()) {
                    correctArrayData = byteArrayData.sliceArray(index..byteArrayData.size - 1)
                    return correctArrayData
                }
            }
        }
        return correctArrayData
    }

    @SuppressLint("MissingPermission")
    fun sendCmd(data: ByteArray) {

        Functions.showLog(TAG, "send Cmd: ${Functions.toHexString(data)}")

        //Open testSavefile use to test
        if (Constants.IS_TEST == true) {
            testSaveFile()
        }
        Functions.showLog(TAG, "log info gatt ${connectedGatt}")
        var gatt = connectedGatt ?: run {
            Functions.showLog(TAG, "ERROR: sendCmd failed, no connected device")

            return
        }

        var characteristic = characteristicForWrite ?: run {
            Functions.showLog(TAG, "ERROR: sendCmd failed, characteristic unavailable")
            return
        }

        if (!characteristic.isWriteable()) {
            Functions.showLog(
                TAG,
                "ERROR: write failed, characteristic not writeable $RX_UUID"
            )
            return
        }

        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        characteristic.value = data
        gatt.writeCharacteristic(characteristic)
    }

    fun sendMiHeartRate() {
        Functions.showLog(TAG, "sendMiHeartRate")
        //Open testSavefile use to test
        if (Constants.IS_TEST == true) {
            testSaveFile()
        }
        Functions.showLog(TAG, "log info gatt ${connectedGatt}")
        var gatt = connectedGatt ?: run {
            Functions.showLog(TAG, "ERROR: sendCmd failed, no connected device")

            return
        }
        val service = connectedGatt?.getService(UUID.fromString(UUID_SERVICE_HEARTRATE))
        var characteristic: BluetoothGattCharacteristic? = null
        if (service != null) {
            characteristic = service.getCharacteristic(UUID.fromString(RX_UUID))
        }
        characteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        Functions.showLog(TAG, "send Cmd: {Functions.toHexString(data)}")

        val START_HEART_RATE_SCAN = byteArrayOf(21, 2, 1)
        characteristic?.value = START_HEART_RATE_SCAN
        gatt.writeCharacteristic(characteristic)
    }


    fun sendStartMeasureCmd() {
        Functions.showLog(TAG, "sendStartMeasureCmd")
        val data = startMeasure()
        sendCmd(data)
    }

    fun sendStopMeasureCmd() {
        Functions.showLog(TAG, "sendStopMeasureCmd")
        val data = stopMeasure()
        sendCmd(data)
    }

    fun sendGainDataTest() {
        // Send Gain data
        Functions.showLog("sendGainDataTest")
        if (Constants.IS_TEST) {
            val timer = object : CountDownTimer(2000, 1000) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    val data = byteArrayOf(0x0A, 0x0B, 0x0C, 0xEE.toByte(), 0x01, 0x01, 0x01)
                    val data2 = byteArrayOf(
                        0x0a,
                        0x0b,
                        0x0c,
                        0x0b,
                        0x16,
                        0x1f,
                        0x00,
                        0x1f,
                        0x00,
                        0x1f,
                        0x02,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x5f,
                        0x0a,
                        0x0b,
                        0x0c,
                        0xee.toByte(),
                        0x01,
                        0x01,
                        0x01
                    )
                    val characteristic: BluetoothGattCharacteristic? =
                        BluetoothGattCharacteristic(null, 1, 1)
                    if (characteristic != null) {
                        characteristic.value = data2
                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
                    }
                }
            }
            timer.start()
        }
    }

    fun sendStartGainCmd() {
        Functions.showLog(TAG, "sendStartGainCmd")
        val data = startGain()
        sendCmd(data)
    }

    // Test save file
    private fun testSaveFile() {
        // Send Measure data
        GlobalScope.launch(Dispatchers.Main) {
            // Switch to a background (IO) thread
            withContext(Dispatchers.IO) {
                val mainHandler = Handler(Looper.getMainLooper())
                mainHandler.post(object : Runnable {
                    override fun run() {
                        val rands = (0..10).random()
                        val data = ByteArray(72)
                        data[0] = 0x0A
                        data[1] = 0x0B
                        data[2] = 0x0C
                        data[3] = 0x01
                        data[4] = 0x42
                        data[5] = 0x00
                        data[6] = 0x00
                        data[7] = 0x00
                        data[8] = 0x01
                        data[9] = 0x16
                        for (i in 10 until 42) {
                            data[i] = 0x00
                        }
                        for (i in 43 until 56) {
                            data[i] = rands.toByte()
                        }
                        data[57] = rands.toByte()
                        data[58] = rands.toByte()
                        for (i in 59 until 71) {
                            data[i] = rands.toByte()
                        }
                        val characteristic: BluetoothGattCharacteristic? =
                            BluetoothGattCharacteristic(null, 1, 1)
                        if (characteristic != null) {
                            characteristic.value = data
                            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
                        }
                        mainHandler.postDelayed(this, 800)
                    }
                })
            }
        }
    }

    private fun startMeasure(): ByteArray {
        val data = ByteArray(7)
        data[0] = 0x0A
        data[1] = 0x0B
        data[2] = 0x0C
        data[3] = RepaceRequestType.TYPE_CMD.toByte()
        data[4] = 0x01 //length: 1
        data[5] = RepaceCmd.CMD_MEASURE_START.toByte()
        data[6] = 0x01 //checksum
        return data
    }

    private fun stopMeasure(): ByteArray {
        val data = ByteArray(7)
        data[0] = 0x0A
        data[1] = 0x0B
        data[2] = 0x0C
        data[3] = RepaceRequestType.TYPE_CMD.toByte()
        data[4] = 0x01 //length: 1
        data[5] = RepaceCmd.CMD_MEASURE_STOP.toByte()
        data[6] = 0x01 //checksum
        return data
    }

    private fun startGain(): ByteArray {
        return byteArrayOf(
            0x0A,
            0x0B,
            0x0C,
            RepaceRequestType.TYPE_CMD.toByte(),
            0x01,
            RepaceCmd.CMD_GAIN_START.toByte(),
            0x01
        )
    }

    @SuppressLint("MissingPermission")
    fun onStop() {
        Functions.showLog(TAG, "Stop scan")
        bluetoothAdapter.bluetoothLeScanner.stopScan(bleScanCallback)
    }

    //region BluetoothGattCharacteristic extension
    fun BluetoothGattCharacteristic.isReadable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

    fun BluetoothGattCharacteristic.isWriteable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    fun BluetoothGattCharacteristic.isWriteableWithoutResponse(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

    private fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
        return (properties and property) != 0
    }
}