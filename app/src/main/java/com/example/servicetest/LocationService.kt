package com.example.servicetest

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileNotFoundException
import java.util.*

class LocationService: Service(), LocationListener {

    private var locationManager: LocationManager? = null
    private var context: Context? = null

    private val MinTime = 1000 //最低更新間隔（ミリ秒）
    private val MinDistance = 1f//最低更新距離（メートル）

    override fun onCreate() {
        super.onCreate()

        context=applicationContext

        WriteFileTest(applicationContext.toString())

        // LocationManager インスタンス生成
        locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val requestCode = 0
        val channelId = "default"
        val title = "　"//context!!.getString(R.string.app_name)
        val pendingIntent = PendingIntent.getActivity(
            context, requestCode,
            intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        // ForegroundにするためNotificationが必要、Contextを設定
        val notificationManager =
            context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Notification　Channel 設定
        val channel = NotificationChannel(
            channelId, title, NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Silent Notification"
        // 通知音を消さないと毎回通知音が出てしまう
        // この辺りの設定はcleanにしてから変更
        channel.setSound(null, null)
        // 通知ランプを消す
        channel.enableLights(false)
        channel.lightColor = Color.BLUE
        // 通知バイブレーション無し
        channel.enableVibration(false)
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel)
            val notification =
                Notification.Builder(context, channelId)
                    .setContentTitle(title) // 本来なら衛星のアイコンですがandroid標準アイコンを設定
                    .setSmallIcon(android.R.drawable.btn_star)
                    //.setContentText("　"/*通知メモ入れる、GPS*/)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .build()

            // startForeground
            startForeground(1, notification)
        }
        startGPS()
        return START_NOT_STICKY
    }

    protected fun startGPS() {
        val strBuf = StringBuilder()
        strBuf.append("startGPS\n")
        val gpsEnabled =
            locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!gpsEnabled) {
            // GPSを設定するように促す
            enableLocationSettings()
        }
        if (locationManager != null) {
            try {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) !=
                    PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                locationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MinTime.toLong(), MinDistance, this
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            strBuf.append("locationManager=null\n")
        }
    }

    //GPS情報更新時処理
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onLocationChanged(location: Location) {
        val strBuf = StringBuilder()
        strBuf.append("----------\n")
        var str = """
            緯度(Y) = ${location.latitude}
            
            """.trimIndent()
        strBuf.append(str)
        str = """
            経度(X) = ${location.longitude}
            
            """.trimIndent()
        strBuf.append(str)
        str = """
            誤差 = ${location.accuracy}
            
            """.trimIndent()
        strBuf.append(str)
        str = """
            高度 = ${location.altitude}
            
            """.trimIndent()
        strBuf.append(str)
        val sdf =
            SimpleDateFormat("MM/dd HH:mm:ss")
        sdf.timeZone = TimeZone.getTimeZone("Asia/Tokyo")
        val currentTime = sdf.format(location.time)
        str = "時間 = $currentTime\n"
        strBuf.append(str)
        str = """
            速度 = ${location.speed}
            
            """.trimIndent()
        strBuf.append(str)
        str = """
            方角 = ${location.bearing}
            
            """.trimIndent()
        strBuf.append(str)
        strBuf.append("----------\n")
        WriteFileTest(strBuf.toString())
    }

    //GPSが利用不可、利用可能になった場合に呼ばれる
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // Android 6, API 23以上でパーミッシンの確認
        if (Build.VERSION.SDK_INT <= 28) {
            val strBuf = StringBuilder()
            when (status) {
                LocationProvider.AVAILABLE -> {
                }
                LocationProvider.OUT_OF_SERVICE -> strBuf.append("LocationProvider.OUT_OF_SERVICE\n")
                LocationProvider.TEMPORARILY_UNAVAILABLE -> strBuf.append("LocationProvider.TEMPORARILY_UNAVAILABLE\n")
            }
            WriteFileTest(strBuf.toString())
        }
    }

    private fun enableLocationSettings() {
        val settingsIntent =
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(settingsIntent)
    }

    private fun stopGPS() {
        if (locationManager != null) {
            // update を止める
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            locationManager!!.removeUpdates(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopGPS()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    fun WriteFileTest(str:String){
        var buf:String=""
        try{
            val file= File("$filesDir/", "log.txt")
            val scan= Scanner(file)
            while(scan.hasNextLine()){
                buf+=scan.nextLine()+"\n"
            }
            buf+="\n"+str
            file.writeText(buf)
        }catch(e: FileNotFoundException){
            val file= File("$filesDir/", "log.txt")
            file.writeText("NEW CREATE FILE")
        }
    }

}