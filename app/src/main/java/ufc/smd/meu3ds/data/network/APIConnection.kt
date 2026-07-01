package ufc.smd.meu3ds.data.network

import androidx.compose.runtime.remember
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

object RetrofitClient {
    private val retrofit = Retrofit.Builder()
            .baseUrl("https://api.igdb.com/v4/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val apiService: IGDBApiService = retrofit.create(IGDBApiService::class.java)
}