package com.afoxxvi.alopex.util

import android.content.Context
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PrintStream
import java.nio.charset.StandardCharsets

object FoxFiles {
    fun readStream(inputStream: InputStream): String {
        try {
            val reader = InputStreamReader(inputStream, StandardCharsets.UTF_8)
            val buff = CharArray(1024)
            var hasRead: Int
            val stringBuilder = StringBuilder()
            while (reader.read(buff).also { hasRead = it } > 0) {
                stringBuilder.append(String(buff, 0, hasRead))
            }
            inputStream.close()
            return stringBuilder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun getFileStream(context: Context?, fileName: String?): InputStream? {
        return try {
            FileInputStream(fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun inputStream(inputStream: InputStream): String {
        try {
            val reader = InputStreamReader(inputStream, StandardCharsets.UTF_8)
            val buff = CharArray(1024)
            var hasRead = 0
            val stringBuilder = StringBuilder()
            while (reader.read(buff).also { hasRead = it } > 0) {
                stringBuilder.append(String(buff, 0, hasRead))
            }
            inputStream.close()
            return stringBuilder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun inputFile(context: Context, fileName: String?): String {
        try {
            val inputStream = context.openFileInput(fileName)
            val reader = InputStreamReader(inputStream, StandardCharsets.UTF_8)
            val buff = CharArray(1024)
            var hasRead = 0
            val stringBuilder = StringBuilder()
            while (reader.read(buff).also { hasRead = it } > 0) {
                stringBuilder.append(String(buff, 0, hasRead))
            }
            inputStream.close()
            return stringBuilder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun outputFile(context: Context, fileName: String?, document: String?) {
        try {
            val outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            val printStream = PrintStream(outputStream)
            printStream.print(document)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getOutputStream(context: Context, fileName: String?): PrintStream? {
        return try {
            val outputStream = context.openFileOutput(fileName, Context.MODE_APPEND)
            PrintStream(outputStream, true, "UTF-8")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}