
package ufc.smd.meu3ds

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
