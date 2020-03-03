package org.cs_cnu.appusagetracker

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

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
                printAppUsage()
            }

        }
    }

    private fun checkForPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun printAppUsage() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -1)

        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val queryUsageStats = usageStatsManager
                .queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY, cal.timeInMillis,
                        System.currentTimeMillis()
                )

        queryUsageStats.sortWith(Comparator { right, left ->
            compareValues(left.lastTimeUsed, right.lastTimeUsed)
        })

        var tmp = ""
        queryUsageStats.forEach { it ->
            tmp += "packageName: ${it.packageName}, lastTimeUsed: ${Date(it.lastTimeUsed)}, totalTimeInForeground: ${it.totalTimeInForeground}\n"
        }
        tv.text = tmp
    }
}
