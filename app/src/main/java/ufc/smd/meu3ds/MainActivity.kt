package ufc.smd.meu3ds

//import android.os.Bundle
//import android.util.Log
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.Button
//import androidx.compose.material3.HorizontalDivider
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import org.json.JSONArray
//import org.json.JSONObject
//import ufc.smd.meu3ds.data.network.mLoad
//import ufc.smd.meu3ds.ui.theme.Meu3DSTheme

//essa MainActivity inteira é somente para demonstrar a conexão por BufferedReader no dia da apresentação
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            var jogos by remember { mutableStateOf(listOf<String>()) }
//            var carregando by remember { mutableStateOf(false) }
//
//            val coroutineScope = rememberCoroutineScope()
//
//            Meu3DSTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Column(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(innerPadding)
//                            .padding(16.dp),
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Button(
//                            onClick = {
//                                carregando = true
//                                jogos = emptyList()
//
//                                coroutineScope.launch(Dispatchers.IO) {
//                                    val urlIGDB = "https://api.igdb.com/v4/games"
//                                    val meuClientId = "u6rjgzn82nz0rd3yi5zl9eqoofc2rt"
//                                    val meuToken = "k2ktyy51lk9vjm85hubtf6o6r9efhf"
//
//                                    val retorno = mLoad(urlIGDB, meuClientId, meuToken)
//                                    val texto = retorno?.readText() ?: "vazio"
//
//                                    try {
//                                        val jsonArray = JSONArray(texto)
//                                        val listaTemporaria = mutableListOf<String>()
//
//                                        for (i in 0 until jsonArray.length()) {
//                                            val jsonObject = jsonArray.getJSONObject(i)
//                                            val id = jsonObject.optInt("id")
//
//                                            listaTemporaria.add(id.toString())
//                                        }
//
//                                        withContext(Dispatchers.Main) {
//                                            jogos = listaTemporaria
//                                            carregando = false
//                                        }
//
//                                    } catch (e: Exception) {
//                                        Log.v("PDM", "Erro ao processar JSON: ${e.message}")
//                                        withContext(Dispatchers.Main) {
//                                            jogos = listOf("Erro ao carregar dados.")
//                                            carregando = false
//                                        }
//                                    }
//                                }
//
//                            },
//                            modifier = Modifier.padding(bottom = 16.dp)
//                        ) {
//                            Text(text = "Buscar IDs no IGDB")
//                        }
//                        if (carregando) {
//                            Text("Carregando...")
//                        } else if (jogos.isEmpty()) {
//                            Text("Nenhum dado. Toque no botão acima.")
//                        } else {
//                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
//                                items(jogos) { jogo ->
//                                    Text(
//                                        text = jogo,
//                                        modifier = Modifier.padding(innerPadding)
//                                    )
//                                    HorizontalDivider()
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ufc.smd.meu3ds.data.network.IGDBApiService
import ufc.smd.meu3ds.data.network.JogoModel
import ufc.smd.meu3ds.ui.theme.Meu3DSTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var jogos by remember { mutableStateOf(listOf<JogoModel>()) }
            var carregando by remember { mutableStateOf(false) }

            val coroutineScope = rememberCoroutineScope()

            val retrofit = remember {
                Retrofit.Builder()
                    .baseUrl("https://api.igdb.com/v4/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }

            val apiService = remember { retrofit.create(IGDBApiService::class.java) }

            Meu3DSTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BotaoConsultaListaGeral {
                            carregando = true
                            jogos = emptyList()

                            coroutineScope.launch(Dispatchers.IO) {
                                val meuClientId = "u6rjgzn82nz0rd3yi5zl9eqoofc2rt"
                                val meuToken = "Bearer k2ktyy51lk9vjm85hubtf6o6r9efhf"
                                val textoDoFiltro = "fields name, first_release_date, summary; where platforms = 37; sort total_rating desc;"
                                val mediaType = okhttp3.MediaType.parse("text/plain")
                                val corpoRequisicao = okhttp3.RequestBody.create(mediaType, textoDoFiltro)

                                try {
                                    val respostaApi = apiService.buscarJogos(meuClientId, meuToken, corpoRequisicao)
                                    Log.v("respostaAPI", respostaApi.toString())

                                    withContext(Dispatchers.Main) {
                                        jogos = respostaApi
                                        carregando = false
                                    }
                                } catch (e: Exception) {
                                    Log.e("erro", "Erro na requisição: ${e.message}")
                                    withContext(Dispatchers.Main) {
                                        carregando = false
                                    }
                                }
                            }
                        }

                        if (carregando) {
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                            Text("Carregando com Retrofit...")
                        } else if (jogos.isEmpty()) {
                            Text("Nenhum dado. Toque no botão acima.")
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(jogos) { jogo ->
                                    JogoCard(jogo)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BotaoConsultaListaGeral( onClick: () -> Unit ) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text("Buscar no IGDB com retrofit")
    }
}

@Composable
fun JogoCard(jogo: JogoModel) {
    val dataFormatada = remember(jogo.data) {
        if (jogo.data != null) {
            val ms = jogo.data * 1000
            val data = Date(ms)

            val formatador = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            formatador.format(data)
        } else {
            "Sem data"
        }
    }

    var expandido by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize()
            .clickable { expandido = !expandido },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = jogo.nome ?: "Sem título", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(dataFormatada)
            if (expandido) {
                Text(jogo.desc ?: "Sem descrição")
            }
        }
    }
}