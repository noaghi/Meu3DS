
package ufc.smd.meu3ds

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
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
            // Acessa o nó do amigo dentro da lista de amigos do usuário atual e o remove
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
                // Se já era favorito, remove
                ref.removeValue().await()
            } else {
                // Se não era, salva os dados básicos do jogo
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

// 2. Escuta os IDs dos favoritos do usuário em tempo real
fun escutarFavoritos(
    database: FirebaseDatabase,
    uidUsuario: String,
    onResult: (List<Int>) -> Unit
) {
    database.getReference("users")
        .child(uidUsuario)
        .child("favorites")
        .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaIds = snapshot.children.mapNotNull { it.key?.toIntOrNull() }
                onResult(listaIds)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
}