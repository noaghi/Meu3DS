package ufc.smd.meu3ds

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ufc.smd.meu3ds.data.network.JogoModel
import ufc.smd.meu3ds.data.network.UserModel
import ufc.smd.meu3ds.ui.theme.Meu3DSTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : androidx.fragment.app.FragmentActivity() {
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
                            if (email.isBlank() || senha.isBlank()) {
                                Toast.makeText(this@MainActivity, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            val sucesso = logar(email, senha)
                            if (sucesso) {
                                onSuccess()
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "E-mail ou senha incorretos. Verifique os dados e tente novamente.",
                                    Toast.LENGTH_LONG
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
            val authResult = auth.createUserWithEmailAndPassword(email, senha).await()
            val uid = authResult.user?.uid

            if (uid != null) {
                val novoUsuario = UserModel(uid = uid, name = nome, email = email)
                database.getReference("users").child(uid).setValue(novoUsuario).await()

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
                    Toast.makeText(this, "E-mail de recuperação enviado com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w("PDM", "Falha ao enviar reset", task.exception)
                    Toast.makeText(this, "Erro: Não foi possível enviar o e-mail de recuperação.", Toast.LENGTH_LONG).show()
                }
            }
    }

}

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
            .background(MaterialTheme.colorScheme.background)
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Gamepad,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Meu3DS",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Gerencie sua coleção de jogos favoritos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Senha") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
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
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ElevatedButton(
            onClick = {
                onLoginClick(email, password, context) {
                    navController.navigate(Rotas.LISTA_JOGOS) {
                        popUpTo(Rotas.LOGIN) { inclusive = true }
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(text = "Entrar no App", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Não tem uma conta? ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = "Cadastre-se",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { navController.navigate(Rotas.CADASTRO_USUARIO) }
            )
        }
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
            "Sem data de lançamento"
        }
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .animateContentSize()
            .clickable { expandido = !expandido },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (expandido) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (expandido) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = jogo.nome ?: "Sem título",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dataFormatada,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (expandido) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = jogo.desc ?: "Sem descrição disponível.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isFavorito) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent
                    )
            ) {
                IconButton(onClick = onFavoritoClick) {
                    Icon(
                        imageVector = if (isFavorito) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favoritar",
                        tint = if (isFavorito) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(26.dp)
                    )
                }
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
            .background(MaterialTheme.colorScheme.background)
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Criar Conta",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Preencha os dados abaixo para começar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome Completo") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Nome") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Senha") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        ElevatedButton(
            onClick = {
                coroutineScope.launch {
                    onCadastroClick(nome, email, password) {
                        navController.navigate(Rotas.LISTA_JOGOS) {
                            popUpTo(Rotas.LOGIN) { inclusive = true }
                        }
                    }
                }
            },
            enabled = nome.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(text = "Finalizar Cadastro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Já possui uma conta? ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = "Faça Login",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
        }
    }
}

