package ufc.smd.meu3ds.data.network

import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

suspend fun mLoad(string: String): BufferedReader? {
    val url: URL = mStringToURL(string)!!
    val connection: HttpsURLConnection?
    try {
        connection = url.openConnection() as HttpsURLConnection
        connection.requestMethod= "GET"
        connection.connectTimeout= 20000
        connection.connect()

        Log.v("PDM", "Response Code: "+connection.responseCode)
        Log.v("PDM", "Response: "+connection.responseMessage)

        val inputStream: InputStream = connection.inputStream
        val bufferedInputStream = BufferedInputStream(inputStream)
        return bufferedInputStream.bufferedReader(Charsets.UTF_8)
    } catch (e: IOException) {
        e.printStackTrace()
        Log.v("PDM", "Erro de comunicação: "+e.message)

    }
    return null
}

private fun mStringToURL(string: String): URL? {
    try {
        return URL(string)
    } catch (e: MalformedURLException) {
        e.printStackTrace()
        Log.v("PDM", "Erro de formatação da URL: "+e.message)
    }
    return null
}