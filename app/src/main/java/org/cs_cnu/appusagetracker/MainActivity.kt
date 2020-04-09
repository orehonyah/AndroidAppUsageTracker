package org.cs_cnu.appusagetracker

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.opencsv.CSVWriter
import com.opencsv.exceptions.CsvBadConverterException
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileWriter
import java.math.BigInteger
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener() {
            if (!checkForPermission()) {
                Toast.makeText(
                        this,
                        "This app need to enable access for App Usage Statistics." +
                        "Go to <Settings - Security - Apps with usage access",
                        Toast.LENGTH_LONG
                ).show()
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            } else {
                //val usageStats = getUsuageStats()
                //printUsageStats(usageStats)
                var usageEvents = getUsageEvents()
                printUsageEvents(usageEvents)//프린팅
                //saveUsageStats(usageStats)
                /*
                usageEvents = getUsageEvents()
                saveUsageEvents(usageEvents)
                 */
                sendBroadcast(Intent("org.cs_cnu.appusagetracker.testaction"))
            }

        }


    }

    private fun checkForPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        val perms:Array<String> = arrayOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE","android.permission.READ_INTERNAL_STORAGE", "android.permission.WRITE_INTERNAL_STORAGE")

        var permsRequestCode = 200
        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.M){
            requestPermissions(perms, permsRequestCode)
        }
        return mode == AppOpsManager.MODE_ALLOWED

        }
    private fun getUsageEvents() : UsageEvents{
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -1)

        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        return usageStatsManager.queryEvents(cal.timeInMillis, System.currentTimeMillis());

    }/*
    private fun getUsuageStats() : List<UsageStats>{
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -1)

        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val queryUsageStats = usageStatsManager
            .queryUsageStats(
                UsageStatsManager.INTERVAL_MONTHLY, cal.timeInMillis,
                System.currentTimeMillis()
            )

        queryUsageStats.sortWith(Comparator { right, left ->
            compareValues(left.lastTimeUsed, right.lastTimeUsed)
        })
        return queryUsageStats
    }
    private fun printUsageStats(queryUsageStats:List<UsageStats>) {
        var tmp = ""
        queryUsageStats.forEach { it ->
            tmp += "packageName: ${it.packageName}, lastTimeUsed: ${Date(it.lastTimeUsed)}, totalTimeInForeground: ${it.totalTimeInForeground}\n"
        }
        tv.text = tmp
    }*/
    private fun printUsageEvents(usageEvents:UsageEvents) {
        var tmp = "ACTIVITY_PAUSED : "+UsageEvents.Event.ACTIVITY_PAUSED+
                "\nACTIVITY_RESUMED : "+UsageEvents.Event.ACTIVITY_RESUMED +
                "\nACTIVITY_STOPPED : "+UsageEvents.Event.ACTIVITY_STOPPED +
                "\nMOVE_TO_BACKGROUND : "+UsageEvents.Event.MOVE_TO_BACKGROUND +
                "\nMOVE_TO_FOREGROUND : "+UsageEvents.Event.MOVE_TO_FOREGROUND +
                "\nCONFIGURATION_CHANGE : "+UsageEvents.Event.CONFIGURATION_CHANGE +
                "\nFOREGROUND_SERVICE_START : "+UsageEvents.Event.FOREGROUND_SERVICE_START+
                "\nFOREGROUND_SERVICE_STOP : "+UsageEvents.Event.FOREGROUND_SERVICE_STOP+
                "\nKEYGUARD_HIDDEN : "+UsageEvents.Event.KEYGUARD_HIDDEN+
                "\nKEYGUARD_SHOWN : "+UsageEvents.Event.KEYGUARD_SHOWN+
                "\nDEVICE_SHUTDOWN : "+UsageEvents.Event.DEVICE_SHUTDOWN+
                "\nDEVICE_STARTUP : "+UsageEvents.Event.DEVICE_STARTUP+
                "\nNONE : "+UsageEvents.Event.NONE+
                "\nSCREEN_INTERACTIVE : "+UsageEvents.Event.SCREEN_INTERACTIVE+
                "\nSCREEN_NON_INTERACTIVE : "+UsageEvents.Event.SCREEN_NON_INTERACTIVE+
                "\nSHORTCUT_INVOCATION : "+UsageEvents.Event.SHORTCUT_INVOCATION+
                "\nSTANDBY_BUCKET_CHANGED : "+UsageEvents.Event.STANDBY_BUCKET_CHANGED+
                "\nUSER_INTERACTION : "+UsageEvents.Event.USER_INTERACTION

        while(usageEvents.hasNextEvent()){
            val event = UsageEvents.Event()
            usageEvents.getNextEvent(event)
            tmp = tmp+"\n"+event.eventType + " "+event.packageName + " "+Date(event.timeStamp)
        }
        tv1.text = tmp
    }/*
    private fun saveUsageStats(queryUsageStats:List<UsageStats>) {
        val path :String = "/storage/emulated/0/"
        val filename:String = "appusage.csv"
        val writer : CSVWriter = CSVWriter(FileWriter(path+filename))
        queryUsageStats.forEach { it ->
            var tmp = Array<String>(3,{""});
            tmp[0] = it.packageName
            tmp[1] = Date(it.lastTimeUsed).toString()
            tmp[2] = it.totalTimeInForeground.toString()
            writer.writeNext(tmp)
        }
        writer.close()
    }*/

    private fun saveUsageEvents(usageEvents:UsageEvents) {
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
