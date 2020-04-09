package org.cs_cnu.appusagetracker

import android.app.IntentService
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.opencsv.CSVWriter
import java.io.FileWriter
import java.math.BigInteger
import java.util.*
import kotlin.collections.HashMap

/**
 * A constructor is required, and must call the super [android.app.IntentService.IntentService]
 * constructor with a name for the worker thread.
 */
class SaveUsageEventsService : IntentService("HelloIntentService") {

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    override fun onHandleIntent(intent: Intent?) {
        // Normally we would do some work here, like download a file.
        // For our sample, we just sleep for 5 seconds.
        Toast.makeText(this, "start service success", Toast.LENGTH_SHORT).show()
        this.saveTestValues()
        saveUsageEvents(getUsageEvents())

    }

    private fun saveTestValues(){
        val path = "/sdcard/"
        val filename = "servicetest.csv"
        Log.e(path,path)
        val writer = CSVWriter(FileWriter(path+filename))
        writer.writeNext(Array(3, {"aaa"}))
        writer.writeNext(Array(3, {"bbb"}))
        writer.writeNext(Array(3, {"ccc"}))
        writer.close()
    }
    private fun getUsageEvents() : UsageEvents {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -1)

        val usageStatsManager = getSystemService( Context.USAGE_STATS_SERVICE) as UsageStatsManager
        return usageStatsManager.queryEvents(cal.timeInMillis, System.currentTimeMillis())
    }
    private fun saveUsageEvents(usageEvents: UsageEvents) {
        val path = "/sdcard/"
        val filename = "appusage"+ Date(System.currentTimeMillis()).toString()+".csv"
        val writer = CSVWriter(FileWriter(path+filename))

        val started = HashMap<String, Array<String>>()
        while(usageEvents.hasNextEvent()){
            val event = UsageEvents.Event()
            usageEvents.getNextEvent(event)
            val tmp = Array(3,{""})
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