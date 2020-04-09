package org.cs_cnu.appusagetracker

import android.app.IntentService
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.opencsv.CSVWriter
import java.io.FileWriter
import java.math.BigInteger
import java.util.*
import kotlin.collections.HashMap


class WeekBroadcastReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        val actionName = intent!!.action
        Toast.makeText(context, "받은 액션 : $actionName", Toast.LENGTH_SHORT).show()
        val intent1 = Intent(context,SaveUsageEventsService::class.java)
        context?.startService(intent1)
        saveTestValues()
    }

    private fun saveTestValues(){
        val path = "/sdcard/"
        val filename = "test.csv"
        Log.e(path,path)
        val writer = CSVWriter(FileWriter(path+filename))
        writer.writeNext(Array(3, {"aaa"}))
        writer.writeNext(Array(3, {"bbb"}))
        writer.writeNext(Array(3, {"ccc"}))
        writer.close()
    }
}