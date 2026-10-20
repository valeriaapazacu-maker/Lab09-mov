package com.example.lab09

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

@Composable
fun Etiqueta(texto: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun TarjetaReceta(receta: RecetaModel, alHacerClic: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { alHacerClic() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box {
            AsyncImage(
                model = receta.image,
                contentDescription = receta.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${receta.rating ?: 0.0}",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = receta.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Etiqueta(receta.cuisine ?: "Sin tipo")
                Etiqueta(receta.difficulty ?: "-")
                Etiqueta("${(receta.prepTimeMinutes ?: 0) + (receta.cookTimeMinutes ?: 0)} min")
            }
        }
    }
}

@Composable
fun ScreenRecetas(navController: NavHostController, api: RecetaApiService) {
    var busqueda by remember { mutableStateOf("") }
    var recetas by remember { mutableStateOf<List<RecetaModel>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(busqueda) {
        if (busqueda.isNotBlank()) delay(400)
        cargando = true
        error = null
        try {
            val respuesta = if (busqueda.isBlank()) {
                api.getRecetas(30)
            } else {
                api.buscarRecetas(busqueda.trim())
            }
            recetas = respuesta.recipes
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("RECETAS", "Error al cargar recetas: ${e.message}")
            error = "No se pudieron cargar las recetas. Revisa tu conexión."
        }
        cargando = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            label = { Text("Buscar receta") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Buscar") },
            singleLine = true,
            shape = RoundedCornerShape(28.dp)
        )
        if (cargando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        } else if (recetas.isEmpty()) {
            Text("No se encontraron recetas.", modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(recetas, key = { it.id }) { receta ->
                    TarjetaReceta(receta) { navController.navigate("receta/${receta.id}") }
                }
            }
        }
    }
}

@Composable
fun ScreenReceta(navController: NavHostController, api: RecetaApiService, id: Int) {
    var receta by remember { mutableStateOf<RecetaModel?>(null) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(id) {
        try {
            receta = api.getReceta(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("RECETAS", "Error al cargar la receta $id: ${e.message}")
        }
        cargando = false
    }

    val r = receta
    if (cargando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (r == null) {
        Column(Modifier.padding(16.dp)) {
            TextButton(onClick = { navController.popBackStack() }) { Text("← Volver") }
            Text("No se pudo cargar la receta.")
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box {
                AsyncImage(
                    model = r.image,
                    contentDescription = r.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )
                Surface(
                    modifier = Modifier.padding(10.dp),
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ) {
                    TextButton(onClick = { navController.popBackStack() }) { Text("← Volver") }
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = r.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Etiqueta(r.cuisine ?: "-")
                    Etiqueta(r.difficulty ?: "-")
                    Etiqueta("${(r.prepTimeMinutes ?: 0) + (r.cookTimeMinutes ?: 0)} min")
                    Etiqueta("${r.servings ?: 0} porciones")
                    Etiqueta("${r.caloriesPerServing ?: 0} kcal")
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Ingredientes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(6.dp))
                r.ingredients.orEmpty().forEach { ingrediente ->
                    Text("• $ingrediente", modifier = Modifier.padding(vertical = 2.dp))
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Preparación",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(6.dp))
                r.instructions.orEmpty().forEachIndexed { indice, paso ->
                    Text("${indice + 1}. $paso", modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}