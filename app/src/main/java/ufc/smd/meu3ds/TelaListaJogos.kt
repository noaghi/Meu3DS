package ufc.smd.meu3ds

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ufc.smd.meu3ds.data.network.IGDBApiService
import ufc.smd.meu3ds.data.network.JogoModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaListaJogos(
    onLogoutClick: () -> Unit,
    onPerfilClick: () -> Unit,
    database: FirebaseDatabase,
    uidUsuario: String?
) {
    var jogosOriginal by remember { mutableStateOf(listOf<JogoModel>()) }
    var carregando by remember { mutableStateOf(false) }
    var listaFavoritosIds by remember { mutableStateOf(listOf<Int>()) }

    var textoBusca by rememberSaveable { mutableStateOf("") }
    var abaSelecionada by rememberSaveable { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("https://api.igdb.com/v4/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val apiService = remember { retrofit.create(IGDBApiService::class.java) }

    val buscarDadosDoServidor = {
        carregando = true
        coroutineScope.launch(Dispatchers.IO) {
            val meuClientId = "i8vwlcdm21hkn8ovfswk9hy3en61di"
            val meuToken = "Bearer 99fbmzghpmrnd0daxr17acjmsm1udh"
            val textoDoFiltro = "fields name, first_release_date, summary; where platforms = 37; sort total_rating desc; limit 100;"
            val mediaType = okhttp3.MediaType.parse("text/plain")
            val corpoRequisicao = okhttp3.RequestBody.create(mediaType, textoDoFiltro)

            try {
                val respostaApi = apiService.buscarJogos(meuClientId, meuToken, corpoRequisicao)
                withContext(Dispatchers.Main) {
                    jogosOriginal = respostaApi
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

    LaunchedEffect(Unit) {
        buscarDadosDoServidor()
    }

    LaunchedEffect(uidUsuario) {
        if (uidUsuario != null) {
            escutarFavoritos(database, uidUsuario) { ids ->
                listaFavoritosIds = ids
            }
        }
    }

    val jogosExibidos = remember(textoBusca, jogosOriginal, abaSelecionada, listaFavoritosIds) {
        val listaBase = if (abaSelecionada == 0) {
            jogosOriginal
        } else {
            jogosOriginal.filter { listaFavoritosIds.contains(it.id) }
        }

        if (textoBusca.isBlank()) {
            listaBase
        } else {
            listaBase.filter { jogo ->
                jogo.nome?.contains(textoBusca, ignoreCase = true) == true
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Meu3DS", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                },
                actions = {
                    IconButton(onClick = onPerfilClick) {
                        Icon(Icons.Default.Person, contentDescription = "Ver Perfil", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sair", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TabRow(
                selectedTabIndex = abaSelecionada,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = abaSelecionada == 0,
                    onClick = { abaSelecionada = 0 },
                    text = { Text("Todos os Jogos", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Gamepad, contentDescription = null) }
                )
                Tab(
                    selected = abaSelecionada == 1,
                    onClick = { abaSelecionada = 1 },
                    text = { Text("Meus Favoritos", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = textoBusca,
                    onValueChange = { textoBusca = it },
                    placeholder = {
                        Text(if (abaSelecionada == 0) "Buscar na lista geral..." else "Buscar nos meus favoritos...")
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    trailingIcon = {
                        if (textoBusca.isNotBlank()) {
                            IconButton(onClick = { textoBusca = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                if (carregando) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Buscando jogos no servidor...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else if (jogosExibidos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (abaSelecionada == 1 && textoBusca.isBlank())
                                "Você ainda não favoritou nenhum jogo."
                            else "Nenhum jogo encontrado.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(jogosExibidos) { jogo ->
                            val eFavorito = listaFavoritosIds.contains(jogo.id)

                            JogoCard(
                                jogo = jogo,
                                isFavorito = eFavorito,
                                onFavoritoClick = {
                                    if (uidUsuario != null) {
                                        coroutineScope.launch {
                                            alternarFavoritoFirebase(
                                                database,
                                                uidUsuario,
                                                jogo,
                                                isFavorito = eFavorito
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
