package com.example.servicetest

import android.content.Context
import android.os.Environment
import java.io.*
import java.nio.charset.StandardCharsets

internal class StorageReadWrite(context: Context) {
    private val file: File
    private var stringBuffer: StringBuffer? = null
    fun clearFile() {
        // ファイルをクリア
        writeFile("", false)

        // StringBuffer clear
        stringBuffer!!.setLength(0)
    }

    // ファイルを保存
    fun writeFile(gpsLog: String?, mode: Boolean) {
        if (isExternalStorageWritable) {
            try {
                FileOutputStream(file, mode).use { fileOutputStream ->
                    OutputStreamWriter(
                        fileOutputStream,
                        StandardCharsets.UTF_8
                    ).use { outputStreamWriter ->
                        BufferedWriter(outputStreamWriter).use { bw ->
                            bw.write(gpsLog)
                            bw.flush()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ファイルを読み出し
    fun readFile(): String {
        stringBuffer = StringBuffer()

        // 現在ストレージが読出しできるかチェック
        if (isExternalStorageReadable) {
            try {
                FileInputStream(file).use { fileInputStream ->
                    InputStreamReader(
                        fileInputStream,
                        StandardCharsets.UTF_8
                    ).use { inputStreamReader ->
                        BufferedReader(inputStreamReader).use { reader ->
                            var lineBuffer: String?
                            while (reader.readLine().also { lineBuffer = it } != null) {
                                stringBuffer!!.append(lineBuffer)
                                stringBuffer!!.append(System.getProperty("line.separator"))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                stringBuffer!!.append("error: FileInputStream")
                e.printStackTrace()
            }
        }
        return stringBuffer.toString()
    }

    /* Checks if external storage is available for read and write */
    val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

    /* Checks if external storage is available to at least read */
    val isExternalStorageReadable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
        }

    init {
        val path =
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        file = File(path, "log.txt")
    }
}