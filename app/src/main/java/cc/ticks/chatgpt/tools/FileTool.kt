package cc.ticks.chatgpt.tools

import android.content.Context
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object FileTool {

    fun readPrivateFile(context: Context, filename: String): String {
        var data = ""
        var inputStream: FileInputStream? = null
        try {
            inputStream = context.openFileInput(filename)
            BufferedReader(InputStreamReader(inputStream)).use {
                var line: String
                while (true) {
                    line = it.readLine() ?: break
                    data += line
                }
            }
        } catch (exp: Exception) {
            exp.printStackTrace()
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (exp: Exception) {
                    exp.printStackTrace()
                }
            }
        }
        return data
    }

    fun writePrivateFile(context: Context, filename: String, data: String) {
        var outputStream: FileOutputStream? = null
        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE)
            BufferedWriter(OutputStreamWriter(outputStream)).use {
                it.write(data)
                it.flush()
            }
        } catch (exp: Exception) {
            exp.printStackTrace()
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close()
                } catch (exp: Exception) {
                    exp.printStackTrace()
                }
            }
        }
    }
}