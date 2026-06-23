package ufc.smd.meu3ds.data.network

//import android.util.Log
//import java.io.BufferedInputStream
//import java.io.BufferedReader
//import java.io.IOException
//import java.io.InputStream
//import java.net.MalformedURLException
//import java.net.URL
//import javax.net.ssl.HttpsURLConnection
//
////método de conexão Java, usando BufferedReader
//suspend fun mLoad(
//    string: String,
//    clientId: String? = null,
//    token: String? = null,
//    body: String? = null
//): BufferedReader? {
//    val url: URL = mStringToURL(string)!!
//    val connection: HttpsURLConnection?
//    try {
//        connection = url.openConnection() as HttpsURLConnection
//        connection.requestMethod= "POST"
//        connection.connectTimeout= 20000
//
//        if (clientId != null) {
//            connection.setRequestProperty("Client-ID", clientId)
//        }
//        if (token != null) {
//            connection.setRequestProperty("Authorization", "Bearer $token")
//        }
//
//        if (body != null) {
//            connection.setRequestProperty("Content-Type", "text/plain") // Tipo exigido pela IGDB
//            connection.doOutput = true
//
//            connection.outputStream.use { outputStream ->
//                outputStream.write(body.toByteArray(Charsets.UTF_8))
//                outputStream.flush()
//            }
//        }
//
//        connection.connect()
//
//        Log.v("PDM", "Response Code: "+connection.responseCode)
//        Log.v("PDM", "Response: "+connection.responseMessage)
//
//        if (connection.responseCode != HttpsURLConnection.HTTP_OK) {
//            val errorText = connection.errorStream?.bufferedReader()?.readText()
//            Log.v("PDM", "Erro da API: $errorText")
//            return null
//        }
//
//        val inputStream: InputStream = connection.inputStream
//        val bufferedInputStream = BufferedInputStream(inputStream)
//        return bufferedInputStream.bufferedReader(Charsets.UTF_8)
//    } catch (e: IOException) {
//        e.printStackTrace()
//        Log.v("PDM", "Erro de comunicação: "+e.message)
//
//    }
//    return null
//}
//
//private fun mStringToURL(string: String): URL? {
//    try {
//        return URL(string)
//    } catch (e: MalformedURLException) {
//        e.printStackTrace()
//        Log.v("PDM", "Erro de formatação da URL: "+e.message)
//    }
//    return null
//}

import okhttp3.RequestBody
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.http.Body

interface IGDBApiService {
    @POST("games")

    suspend fun buscarJogos(
        @Header("Client-ID") clientId: String,
        @Header("Authorization") authorization: String,
        @Body query: RequestBody
    ): List<JogoModel>
}