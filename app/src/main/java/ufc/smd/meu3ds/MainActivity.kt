package ufc.smd.meu3ds

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ufc.smd.meu3ds.data.network.IGDBApiService
import ufc.smd.meu3ds.data.network.JogoModel
import ufc.smd.meu3ds.data.network.UserModel
import ufc.smd.meu3ds.ui.theme.Meu3DSTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.listOf
import androidx.compose.material.icons.filled.Person

class MainActivity : ComponentActivity() {
    // Escopo de classe para permitir acesso em qualquer lugar
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var userLog by mutableStateOf<UserModel?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = Firebase.auth
        database = Firebase.database
        userLog = UserModel("", "", "")

        val isUserLoggedIn = auth.currentUser != null
        Log.v("PDM", "Usuário está logado? $isUserLoggedIn")

        // Se o usuário já estiver logado ao abrir o app, busca os dados dele
        if (isUserLoggedIn) {
            auth.currentUser?.uid?.let { readUserFirebase(it) }
        }

        setContent {
            Meu3DSTheme {
                val navController = rememberNavController()
                val composeScope = rememberCoroutineScope()

                AppNavigation(
                    isUserLoggedIn = isUserLoggedIn,
                    navController = navController,
                    onLoginClick = { email, senha, context, onSuccess ->
                        composeScope.launch {
                            val sucesso = logar(email, senha)
                            if (sucesso) {
                                onSuccess()
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "E-mail ou senha incorretos.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    onResetPasswordClick = { email ->
                        resetarSenha(email)
                    },
                    onCadastroClick = { nome, email, senha, onSuccess ->
                        composeScope.launch {
                            val sucesso = cadastrarUsuario(nome, email, senha)
                            if (sucesso) onSuccess()
                        }
                    },
                    userLog = userLog,
                    database = database
                )
            }
        }


    }

    // Mover funções para fora do onCreate para estarem acessíveis
    private fun readUserFirebase(uid: String) {
        Log.v("PDM", "readUserFirebase: $uid")
        database.getReference("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Log.i("PDM", "Got value ${snapshot.value}")
                        userLog = UserModel(
                            snapshot.child("uid").value.toString(),
                            snapshot.child("name").value.toString(),
                            snapshot.child("email").value.toString()
                        )
                        Log.i("PDM", "Nome: ${userLog?.name}")
                    } else {
                        Log.w("PDM", "Usuário não existe no nó 'users'")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("PDM", "Erro no Firebase: ${error.message}", error.toException())
                }
            })
    }

    private suspend fun logar(login: String, senha: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(login, senha).await()
            Log.d("PDM", "signInWithEmail:success")
            auth.currentUser?.uid?.let { readUserFirebase(it) }
            true
        } catch (e: Exception) {
            Log.w("PDM", "signInWithEmail:failure", e)
            false
        }
    }

    private suspend fun cadastrarUsuario(nome: String, email: String, senha: String): Boolean {
        return try {
            // 1. Cria o usuário no Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, senha).await()
            val uid = authResult.user?.uid

            if (uid != null) {
                // 2. Salva o nome e email no Realtime Database (Nó 'users')
                // Sem salvar a senha, conforme as boas práticas de segurança!
                val novoUsuario = UserModel(uid = uid, name = nome, email = email)
                database.getReference("users").child(uid).setValue(novoUsuario).await()

                // Atualiza o estado local do app
                userLog = novoUsuario
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("PDM", "Erro ao cadastrar usuário", e)
            false
        }
    }


    private fun resetarSenha(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("PDM", "Reset enviado com sucesso para: $email")
                } else {
                    Log.w("PDM", "Falha ao enviar reset", task.exception)
                }
            }
    }
}

// --- COMPONENTES DE TELA E NAVEGAÇÃO ---

object Rotas {
    const val LOGIN = "login"
    const val CADASTRO_USUARIO = "cadastro_usuario"
    const val LISTA_JOGOS = "lista_jogos"
    const val PERFIL = "perfil"
}

@Composable
fun AppNavigation(
    isUserLoggedIn: Boolean,
    navController: NavHostController,
    onLoginClick: (String, String, android.content.Context, () -> Unit) -> Unit,
    onResetPasswordClick: (String) -> Unit,
    onCadastroClick: (String, String, String, () -> Unit) -> Unit,
    userLog: UserModel?,
    database: FirebaseDatabase
) {
    val startScreen = if (isUserLoggedIn) Rotas.LISTA_JOGOS else Rotas.LOGIN

    NavHost(
        navController = navController,
        startDestination = startScreen
    ) {
        composable(route = Rotas.LOGIN) {
            TelaLogin(
                navController = navController,
                onLoginClick = onLoginClick,
                onResetPasswordClick = onResetPasswordClick
            )
        }

        composable(route = Rotas.CADASTRO_USUARIO) {
            TelaCadastroUsuario(
                navController = navController,
                onCadastroClick = onCadastroClick
            )
        }

        composable(route = Rotas.LISTA_JOGOS) {
            TelaListaJogos(
                onLogoutClick = {
                    Firebase.auth.signOut()
                    navController.navigate(Rotas.LOGIN) {
                        popUpTo(Rotas.LISTA_JOGOS) { inclusive = true }
                    }
                },
                onPerfilClick = {
                    navController.navigate(Rotas.PERFIL)
                },
                database = database,
                uidUsuario = userLog?.uid
            )
        }

        composable(route = Rotas.PERFIL) {
            TelaPerfil(
                usuario = userLog,
                database = database,
                onVoltarClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}


@Composable
fun TelaLogin(
    navController: NavHostController,
    onLoginClick: (String, String, android.content.Context, () -> Unit) -> Unit,
    onResetPasswordClick: (String) -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Meu3DS",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Faça login para continuar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = { onResetPasswordClick(email) },
                enabled = email.isNotBlank()
            ) {
                Text(
                    text = "Esqueceu a senha?",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onLoginClick(email, password, context) {
                    navController.navigate(Rotas.LISTA_JOGOS) {
                        popUpTo(Rotas.LOGIN) { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Logar", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically)
        {
            Text(text = "Não tem cadastro? ",
                style = MaterialTheme.typography.bodyMedium)
            Text(text = "Clique aqui.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {navController.navigate(Rotas.CADASTRO_USUARIO)
                }
            )
        }
    }
}
@Composable
fun TelaListaJogos(
    onLogoutClick: () -> Unit,
    onPerfilClick: () -> Unit,
    database: FirebaseDatabase,
    uidUsuario: String?
) {
    var jogos by remember { mutableStateOf(listOf<JogoModel>()) }
    var carregando by remember { mutableStateOf(false) }
    var listaFavoritosIds by remember { mutableStateOf(listOf<Int>()) } // IDs favoritados
    val coroutineScope = rememberCoroutineScope()

    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("https://api.igdb.com/v4/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val apiService = remember { retrofit.create(IGDBApiService::class.java) }

    LaunchedEffect(uidUsuario) {
        if (uidUsuario != null) {
            escutarFavoritos(database, uidUsuario) { ids ->
                listaFavoritosIds = ids
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Meu3DS") },
                actions = {
                    IconButton(onClick = onPerfilClick) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Ver Perfil",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    TextButton(onClick = onLogoutClick) {
                        Text("Sair", color = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
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

@Composable
fun BotaoConsultaListaGeral(onClick: () -> Unit) {
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
fun JogoCard(
    jogo: JogoModel,
    isFavorito: Boolean,
    onFavoritoClick: () -> Unit
) {
    var expandido by rememberSaveable { mutableStateOf(false) }

    val dataFormatada = remember(jogo.data) {
        if (jogo.data != null) {
            val ms = jogo.data * 1000
            val data = Date(ms)
            val formatador = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
            formatador.format(data)
        } else {
            "Sem data"
        }
    }

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
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Informações do Jogo
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = jogo.nome ?: "Sem título",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = dataFormatada, style = MaterialTheme.typography.bodySmall)

                if (expandido) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = jogo.desc ?: "Sem descrição", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Botão de Favorito (Estrela)
            IconButton(onClick = onFavoritoClick) {
                Icon(
                    imageVector = if (isFavorito) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favoritar",
                    tint = if (isFavorito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TelaCadastroUsuario(
    navController: NavHostController,
    onCadastroClick: (String, String, String, () -> Unit) -> Unit
) {
    var nome by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Criar Conta",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Insira seus dados para começar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        // Campo Nome
        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome Completo") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User Icon") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo E-mail
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Senha
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botão Cadastrar
        Button(
            onClick = {
                coroutineScope.launch {
                    onCadastroClick(nome, email, password) {
                        // Callback de sucesso: manda o usuário para a lista de jogos
                        navController.navigate(Rotas.LISTA_JOGOS) {
                            // Limpa o histórico para o usuário não voltar para o cadastro ao apertar 'Voltar'
                            popUpTo(Rotas.LOGIN) { inclusive = true }
                        }
                    }
                }
            },
            // Só ativa o botão se nenhum campo estiver em branco
            enabled = nome.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Cadastrar", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Link para Voltar ao Login
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Já tem uma conta? ", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Faça login.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    navController.popBackStack() // Volta para a tela anterior (Login)
                }
            )
        }
    }
}
