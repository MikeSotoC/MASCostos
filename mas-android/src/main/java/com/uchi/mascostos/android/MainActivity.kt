package com.uchi.mascostos.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val message = remember {
                    mutableStateOf(
                        "Base Android lista. Próximo paso: conectar gateway remoto/local para proyectos y presupuestos."
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("MASCostos Android")
                    Text(message.value)
                    Button(onClick = {
                        message.value = "Pendiente: listar proyectos + presupuesto activo + exportar reporte CSV/PDF"
                    }) {
                        Text("Ver siguiente acción")
                    }
                }
            }
        }
    }
}
