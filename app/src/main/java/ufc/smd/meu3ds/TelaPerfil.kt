package ufc.smd.meu3ds

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import ufc.smd.meu3ds.data.network.UserModel

@Composable
fun TelaPerfil(
    usuario: UserModel?,
    database: FirebaseDatabase,
    onVoltarClick: () -> Unit
) {
    var listaAmigos by remember { mutableStateOf(listOf<UserModel>()) }
    var carregandoAmigos by remember { mutableStateOf(true) }

    // Estados para o campo de adicionar amigo
    var emailBusca by rememberSaveable { mutableStateOf("") }
    var processandoBusca by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Função interna para recarregar a lista de amigos localmente
    val atualizarListaAmigos = {
        if (usuario?.uid != null) {
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

    // Busca inicial ao abrir a tela
    LaunchedEffect(usuario?.uid) {
        atualizarListaAmigos()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Meu Perfil") },
                navigationIcon = {
                    TextButton(onClick = onVoltarClick) {
                        Text("Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dados básicos do usuário atual
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = usuario?.name ?: "Sem Nome", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = usuario?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Formulário de Adicionar Amigo
            Text(
                text = "Adicionar Amigo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
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
                    label = { Text("E-mail do amigo") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    enabled = !processandoBusca
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (usuario?.uid != null && emailBusca.isNotBlank()) {
                            processandoBusca = true
                            coroutineScope.launch {
                                // Referencia a função externa criada na MainActivity
                                val resultado = adicionarAmigoPorEmail(
                                    database,
                                    uidUsuarioAtual = usuario.uid,
                                    emailAmigo = emailBusca

                                )
                                snackbarHostState.showSnackbar(resultado)

                                if (resultado.contains("sucesso")) {
                                    emailBusca = ""
                                    atualizarListaAmigos() // Recarrega a lista na tela
                                }
                                processandoBusca = false
                            }
                        }
                    },
                    enabled = emailBusca.isNotBlank() && !processandoBusca,
                    modifier = Modifier.height(56.dp)
                ) {
                    if (processandoBusca) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Add")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Lista de Amigos Existentes
            Text(
                text = "Meus Amigos (${listaAmigos.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (carregandoAmigos) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            } else if (listaAmigos.isEmpty()) {
                Text(
                    text = "Nenhum amigo adicionado ainda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(listaAmigos) { amigo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = amigo.name ?: "Sem nome", fontWeight = FontWeight.Bold)
                                    Text(text = amigo.email ?: "", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
