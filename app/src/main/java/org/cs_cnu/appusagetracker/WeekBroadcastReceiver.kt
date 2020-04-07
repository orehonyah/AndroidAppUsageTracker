package org.cs_cnu.appusagetracker

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.getSystemService
import com.opencsv.CSVWriter
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileWriter
import java.math.BigInteger
import java.util.*
import kotlin.collections.HashMap

class WeekBroadcastReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        saveUsageEvents(getUsageEvents())
    }

    private fun getUsageEvents() : UsageEvents {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -1)

        val usageStatsManager = MainActivity().getSystemService( Context.USAGE_STATS_SERVICE) as UsageStatsManager
        return usageStatsManager.queryEvents(cal.timeInMillis, System.currentTimeMillis());

    }
    private fun saveUsageEvents(usageEvents: UsageEvents) {
        val path :String = "/sdcard/"
        val filename:String = "appusage.csv"
        Log.e(path,path)
        val writer = CSVWriter(FileWriter(path+filename))

        val started = HashMap<String, Array<String>>()
        while(usageEvents.hasNextEvent()){
            val event = UsageEvents.Event()
            usageEvents.getNextEvent(event)
            val tmp = Array<String>(3,{""})
            tmp[0] = event.packageName
            tmp[1] = event.timeStamp.toString()
            tmp[2] = event.eventType.toString()
            if(event.eventType == UsageEvents.Event.ACTIVITY_RESUMED){
                started[event.packageName] = tmp
            }
            else if(event.eventType == UsageEvents.Event.ACTIVITY_PAUSED){
                val tmp1 = started[event.packageName] ?: continue
                tmp[2] = (event.timeStamp - BigInteger(tmp1[1]).toLong()).toString()
                started.remove(event.packageName)
                writer.writeNext(tmp)
            }
        }
        writer.close()
    }
}