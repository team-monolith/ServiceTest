package com.example.servicetest

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi

import java.util.ArrayList;


class MainActivity : AppCompatActivity() {
    private val REQUEST_MULTI_PERMISSIONS = 101

    private var textView: TextView? = null
    private var fileReadWrite: StorageReadWrite? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val context = applicationContext
        fileReadWrite = StorageReadWrite(context)

        // Android 6, API 23以上でパーミッションの確認
        if (Build.VERSION.SDK_INT >= 23) {
            checkMultiPermissions()
        } else {
            startLocationService()
        }
    }

    // 位置情報許可の確認、外部ストレージのPermissionにも対応できるようにしておく
    private fun checkMultiPermissions() {
        // 位置情報の Permission
        val permissionLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
        // 外部ストレージ書き込みの Permission
        val permissionExtStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val reqPermissions = ArrayList<Any>()

        // 位置情報の Permission が許可されているか確認
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            // 許可済
        } else {
            // 未許可
            reqPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // 外部ストレージ書き込みが許可されているか確認
        if (permissionExtStorage == PackageManager.PERMISSION_GRANTED) {
            // 許可済
        } else {
            // 許可をリクエスト
            reqPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        // 未許可
        if (!reqPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    reqPermissions.toArray(arrayOfNulls(0)),
                    REQUEST_MULTI_PERMISSIONS)
            // 未許可あり
        } else {
            // 許可済
            startLocationService()
        }
    }

    // 結果の受け取り
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_MULTI_PERMISSIONS) {
            if (grantResults.size > 0) {
                for (i in permissions.indices) {
                    // 位置情報
                    if (permissions[i] == Manifest.permission.ACCESS_FINE_LOCATION) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            // 許可された
                        } else {
                            // それでも拒否された時の対応
                            toastMake("位置情報の許可がないので計測できません")
                        }
                    } else if (permissions[i] == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            // 許可された
                        } else {
                            // それでも拒否された時の対応
                            toastMake("外部書込の許可がないので書き込みできません")
                        }
                    }
                }
                startLocationService()
            }
        } else {
            //
        }
    }

    private fun startLocationService() {
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.log_text)
        val buttonStart: Button = findViewById(R.id.button_start)
        buttonStart.setOnClickListener(object : View.OnClickListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onClick(v: View?) {
                val intent = Intent(application, LocationService::class.java)

                // API 26 以降
                startForegroundService(intent)

                // Activityを終了させる
                finish()
            }
        })
        val buttonLog: Button = findViewById(R.id.button_log)
        buttonLog.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                //textView.setText(fileReadWrite?.readFile())
            }
        })
        val buttonReset: Button = findViewById(R.id.button_reset)
        buttonReset.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                // Serviceの停止
                val intent = Intent(application, LocationService::class.java)
                stopService(intent)
                fileReadWrite?.clearFile()
                //textView.setText("")
            }
        })
    }

    // トーストの生成
    private fun toastMake(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        // 位置調整
        toast.setGravity(Gravity.CENTER, 0, 200)
        toast.show()
    }
}