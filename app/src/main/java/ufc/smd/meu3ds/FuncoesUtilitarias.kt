package ufc.smd.meu3ds

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ufc.smd.meu3ds.data.network.JogoModel

suspend fun adicionarAmigoPorEmail(
    database: FirebaseDatabase,
    uidUsuarioAtual: String,
    emailAmigo: String
): String {
    return withContext(Dispatchers.IO) {
        try {
            val snapshot = database.getReference("users")
                .orderByChild("email")
                .equalTo(emailAmigo.trim())
                .get()
                .await()

            if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                return@withContext "Usuário não encontrado."
            }

            val amigoSnapshot = snapshot.children.first()
            val uidAmigo = amigoSnapshot.key ?: return@withContext "Erro ao processar ID do amigo."

            if (uidAmigo == uidUsuarioAtual) {
                return@withContext "Você não pode adicionar a si mesmo."
            }

            val nomeAmigo = amigoSnapshot.child("name").value?.toString() ?: "Sem nome"
            val emailAmigoEncontrado = amigoSnapshot.child("email").value?.toString() ?: ""

            val dadosAmigo = mapOf(
                "name" to nomeAmigo,
                "email" to emailAmigoEncontrado
            )

            database.getReference("users")
                .child(uidUsuarioAtual)
                .child("friends")
                .child(uidAmigo)
                .setValue(dadosAmigo)
                .await()

            "Amigo adicionado com sucesso!"
        } catch (e: Exception) {
            Log.e("PDM", "Erro ao adicionar amigo", e)
            return@withContext "Erro: ${e.localizedMessage}"
        }
    }
}

suspend fun removerAmigoDoFirebase(
    database: FirebaseDatabase,
    uidUsuarioAtual: String,
    uidAmigo: String
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            database.getReference("users")
                .child(uidUsuarioAtual)
                .child("friends")
                .child(uidAmigo)
                .removeValue()
                .await()
            true
        } catch (e: Exception) {
            Log.e("PDM", "Erro ao remover amigo", e)
            false
        }
    }
}

suspend fun alternarFavoritoFirebase(
    database: FirebaseDatabase,
    uidUsuario: String,
    jogo: JogoModel,
    isFavorito: Boolean
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val ref = database.getReference("users")
                .child(uidUsuario)
                .child("favorites")
                .child(jogo.id.toString())

            if (isFavorito) {
                ref.removeValue().await()
            } else {
                val dadosJogo = mapOf(
                    "id" to jogo.id,
                    "name" to (jogo.nome ?: "Sem título"),
                    "first_release_date" to (jogo.data ?: 0L),
                    "summary" to (jogo.desc ?: "")
                )
                ref.setValue(dadosJogo).await()
            }
            true
        } catch (e: Exception) {
            Log.e("PDM", "Erro ao atualizar favorito", e)
            false
        }
    }
}

fun escutarFavoritos(
    database: FirebaseDatabase,
    uidUsuario: String,
    onResult: (List<JogoModel>) -> Unit
) {
    database.getReference("users")
        .child(uidUsuario)
        .child("favorites")
        .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaJogos = snapshot.children.map { jSnap ->
                    JogoModel(
                        id = jSnap.child("id").value?.toString()?.toInt() ?: 0,
                        nome = jSnap.child("name").value?.toString(),
                        data = jSnap.child("first_release_date").value?.toString()?.toLongOrNull(),
                        desc = jSnap.child("summary").value?.toString()
                    )
                }
                onResult(listaJogos)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
}

fun autenticarBiometria(
    context: Context,
    onSucesso: () -> Unit,
    onErro: (String) -> Unit
) {
    val biometricManager = BiometricManager.from(context)
    val autenticadoresPermitidos = BiometricManager.Authenticators.BIOMETRIC_STRONG or
    BiometricManager.Authenticators.DEVICE_CREDENTIAL

    when (biometricManager.canAuthenticate(autenticadoresPermitidos)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            val activity = context as? FragmentActivity
            if (activity == null) {
                onErro("Erro de contexto do sistema.")
                return
            }

            val executor = ContextCompat.getMainExecutor(context)

            val biometricPrompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSucesso()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        onErro(errString.toString())
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onErro("Autenticação falhou. Tente novamente.")
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Confirme sua identidade")
                .setSubtitle("Autentique-se para liberar a edição dos seus dados")
                .setAllowedAuthenticators(autenticadoresPermitidos)
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
        else -> {
            onSucesso()
        }
    }
}