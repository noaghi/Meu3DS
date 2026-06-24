package ufc.smd.meu3ds.data.network

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