package ufc.smd.meu3ds

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import ufc.smd.meu3ds.data.network.JogoModel
import ufc.smd.meu3ds.data.network.UserModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPerfil(
    usuario: UserModel?,
    database: FirebaseDatabase,
    onVoltarClick: () -> Unit
) {
    var listaAmigos by remember { mutableStateOf(listOf<UserModel>()) }
    var carregandoAmigos by remember { mutableStateOf(true) }
    var emailBusca by rememberSaveable { mutableStateOf("") }
    var processandoBusca by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var amgSelecionadoForJogos by remember { mutableStateOf<UserModel?>(null) }
    var jogosDoAmigo by remember { mutableStateOf(listOf<JogoModel>()) }

    var modoEdicaoLiberado by remember { mutableStateOf(false) }
    var nomeEditado by rememberSaveable(usuario) { mutableStateOf(usuario?.name ?: "") }
    var emailEditado by rememberSaveable(usuario) { mutableStateOf(usuario?.email ?: "") }

    val atualizarListaAmigos = {
        if (usuario?.uid != null) {
            carregandoAmigos = true
            database.getReference("users").child(usuario.uid).child("friends")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val amigos = mutableListOf<UserModel>()
                        for (amigoSnapshot in snapshot.children) {
                            amigos.add(
                                UserModel(
                                    uid = amigoSnapshot.key,
                                    name = amigoSnapshot.child("name").value?.toString() ?: "Sem nome",
                                    email = amigoSnapshot.child("email").value?.toString() ?: ""
                                )
                            )
                        }
                        listaAmigos = amigos
                        carregandoAmigos = false
                    }
                    override fun onCancelled(error: DatabaseError) {
                        carregandoAmigos = false
                    }
                })
        } else {
            carregandoAmigos = false
        }
    }

    LaunchedEffect(usuario?.uid) {
        atualizarListaAmigos()
    }

    LaunchedEffect(amgSelecionadoForJogos) {
        if (amgSelecionadoForJogos?.uid != null) {
            database.getReference("users")
                .child(amgSelecionadoForJogos!!.uid!!)
                .child("favorites")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val lista = snapshot.children.map { jSnap ->
                            JogoModel(
                                id = jSnap.child("id").value?.toString()?.toInt() ?: 0,
                                nome = jSnap.child("name").value?.toString(),
                                data = jSnap.child("first_release_date").value?.toString()?.toLongOrNull(),
                                desc = jSnap.child("summary").value?.toString()
                            )
                        }
                        jogosDoAmigo = lista
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Meu Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onVoltarClick) {
                        Text("Voltar", fontWeight = FontWeight.Bold)
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
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Dados Cadastrais",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = nomeEditado,
                        onValueChange = { nomeEditado = it },
                        label = { Text("Nome Completo") },
                        enabled = modoEdicaoLiberado,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = emailEditado,
                        onValueChange = { emailEditado = it },
                        label = { Text("E-mail") },
                        enabled = modoEdicaoLiberado,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (!modoEdicaoLiberado) {
                                autenticarBiometria(
                                    context = context,
                                    onSucesso = {
                                        modoEdicaoLiberado = true
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Identidade confirmada! Edição liberada.")
                                        }
                                    },
                                    onErro = { erro ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Falha na segurança: $erro")
                                        }
                                    }
                                )
                            } else {
                                if (usuario?.uid != null && nomeEditado.isNotBlank() && emailEditado.isNotBlank()) {
                                    val dadosAtualizados = mapOf(
                                        "name" to nomeEditado.trim(),
                                        "email" to emailEditado.trim()
                                    )
                                    database.getReference("users").child(usuario.uid)
                                        .updateChildren(dadosAtualizados)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                modoEdicaoLiberado = false
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Dados salvos com sucesso!")
                                                }
                                            } else {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Erro ao salvar no banco de dados.")
                                                }
                                            }
                                        }
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (modoEdicaoLiberado) "Salvar Alterações" else "Liberar Edição com Biometria",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Adicionar novo amigo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = emailBusca,
                    onValueChange = { emailBusca = it },
                    label = { Text("E-mail do seu amigo") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                    enabled = !processandoBusca
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (usuario?.uid != null && emailBusca.isNotBlank()) {
                            processandoBusca = true
                            coroutineScope.launch {
                                val resultado = adicionarAmigoPorEmail(database, usuario.uid, emailBusca)
                                snackbarHostState.showSnackbar(resultado)
                                if (resultado.contains("sucesso")) {
                                    emailBusca = ""
                                    atualizarListaAmigos()
                                }
                                processandoBusca = false
                            }
                        }
                    },
                    enabled = emailBusca.isNotBlank() && !processandoBusca,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    if (processandoBusca) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Adicionar", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Meus Amigos (${listaAmigos.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (carregandoAmigos) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (listaAmigos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Nenhum amigo adicionado ainda.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(listaAmigos) { amigo ->
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { amgSelecionadoForJogos = amigo },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = amigo.name ?: "Sem nome",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                text = amigo.email ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            if (usuario?.uid != null && amigo.uid != null) {
                                                coroutineScope.launch {
                                                    val sucesso = removerAmigoDoFirebase(
                                                        database,
                                                        usuario.uid,
                                                        amigo.uid
                                                    )
                                                    if (sucesso) {
                                                        snackbarHostState.showSnackbar("Amigo removido.")
                                                        atualizarListaAmigos()
                                                    } else {
                                                        snackbarHostState.showSnackbar("Erro ao remover amigo.")
                                                    }
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remover",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (amgSelecionadoForJogos != null) {
        AlertDialog(
            onDismissRequest = { amgSelecionadoForJogos = null },
            title = {
                Text(
                    text = "Favoritos de ${amgSelecionadoForJogos?.name}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                if (jogosDoAmigo.isEmpty()) {
                    Text("Este amigo ainda não favoritou nenhum jogo.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                        items(jogosDoAmigo) { jogo ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Text(
                                    text = jogo.nome ?: "Sem título",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(top = 6.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { amgSelecionadoForJogos = null }) {
                    Text("Fechar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
