package ufc.smd.meu3ds.data.network

import com.google.gson.annotations.SerializedName

data class JogoModel(
    val id: Int,

    @SerializedName("name")
    val nome: String? = "Sem título disponível",

    @SerializedName("first_release_date")
    val data: Long? = null,

    @SerializedName("summary")
    val desc: String? = "Sem descrição disponível"
)
