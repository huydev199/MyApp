package com.obelab.repace.model

import java.util.HashMap

object RepaceData {
    const val TYPE_MEASURE_DATA = 0x01
    const val TYPE_MEASURE_DATA_RAW = 0x02
    const val TYPE_CMD = 0x0F
    const val TYPE_GAIN_DATA = 0xEE
    const val TYPE_PHOTO_DIODE_GAIN_SET = 0x0A
    const val TYPE_PHOTO_DIODE_GAIN_STATUS = 0x0B
    const val TYPE_MOTION_SENSOR_SET = 0x0C
    const val TYPE_MOTION_SENSOR_STATUS = 0x0D
    const val TYPE_LASER_DIODE_GAIN_SET = 0xAA
    const val TYPE_LASER_DIODE_GAIN_STATUS = 0xAB
    const val TYPE_STATUS_VERSION = 0x04
    const val TYPE_BATTERY_LEVEL = 0x05
    const val TYPE_END_OF_MEASURE_DATA_WITH_TOTAL_SIZE = 0x06
    const val TYPE_MEASURE_REQUEST_CMD = 0x08
    const val TYPE_ERROR = 0xFF
    const val TYPE_COMMAND = 0x0F
    const val TYPE_MODULE_PREPARE_OTA = 0x10
    const val TYPE_MODULE_PREPARE_OTA_ACK = 0x11
    const val TYPE_MODULE_OTA_DATA = 0x20
    const val TYPE_MODULE_OTA_DATA_ACK = 0x21
    const val TYPE_MODULE_OTA_FINISH = 0x30
    const val TYPE_MODULE_OTA_FINISH_ACK = 0x31 // BLE MODULE RESET
    const val TYPE_MAIN_PREPARE_OTA = 0x90
    const val TYPE_MAIN_PREPARE_OTA_ACK = 0x91
    const val TYPE_MAIN_OTA_DATA = 0xA0
    const val TYPE_MAIN_OTA_DATA_ACK = 0xA1
    const val TYPE_MAIN_OTA_FINISH = 0xB0
    const val TYPE_MAIN_OTA_FINISH_ACK = 0xB1 // MAIN MCU RESET
}