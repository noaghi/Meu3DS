package ufc.smd.meu3ds

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import ufc.smd.meu3ds.data.network.mLoad
import ufc.smd.meu3ds.ui.theme.Meu3DSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var console by remember { mutableStateOf("Carregando...") }
            var desc by remember { mutableStateOf("É um console muito legal!") }

            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    val retorno = mLoad("https://api.rawg.io/api/platforms/8?key=aa77e5572949488db43d4cb8efa3665f")
                    val texto = retorno?.readText() ?: "vazio"
                    val jsonObject = JSONObject(texto)
                    console = jsonObject.getString("name")
                    desc = jsonObject.getString("description")
                }
            }

            Meu3DSTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                    ) {
                        ListaTodosOsJogos({})
                        Text(console)
                        Text(desc)
                    }
                }
            }
        }
    }
}

@Composable
fun ListaTodosOsJogos(onClick: () -> Unit){
    Button(onClick = onClick) {
        Text("lista geral")
    }
}
