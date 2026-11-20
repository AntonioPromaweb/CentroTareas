package com.vasquez.centrotareas.data

import com.vasquez.centrotareas.model.AvancePrimos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class TareasRepositoryLocal : TareasRepository {

    override suspend fun cargarPerfil(): String {
        delay(2000)
        return "Ana Torres (ficticia)"
    }

    override suspend fun cargarPedidos(simularError: Boolean): List<String> {
        delay(3000)
        if (simularError) throw IOException("Sin conexión")
        return listOf("Pedido 101", "Pedido 102", "Pedido 103")
    }

    // Paso 5: trabajo pesado de CPU. withContext(Default) lo saca del hilo principal,
    // así el Repository nunca bloquea a quien lo llama (regla de la guía).
    override suspend fun contarPrimos(hasta: Int): Int = withContext(Dispatchers.Default) {
        var cantidad = 0
        for (n in 2..hasta) {
            // Un bucle de CPU no tiene puntos de suspensión: ensureActive() lanza
            // CancellationException si la tarea fue cancelada, y así se puede cortar.
            ensureActive()
            if (esPrimo(n)) cantidad++
        }
        cantidad
    }

    override fun contarPrimosConProgreso(hasta: Int): Flow<AvancePrimos> = flow {
        var cantidad = 0
        var ultimoPorcentaje = -1

        for (n in 2..hasta) {
            currentCoroutineContext().ensureActive()
            if (esPrimo(n)) {
                cantidad++
            }
            val porcentaje = ((n.toLong() * 100) / hasta).toInt()
            if (porcentaje != ultimoPorcentaje && porcentaje < 100) {
                ultimoPorcentaje = porcentaje
                emit(AvancePrimos(porcentaje, cantidad))
            }
        }
        emit(AvancePrimos(100, cantidad))
    }.flowOn(Dispatchers.Default)

    // Divide solo hasta la raíz de n. d.toLong() * d evita el desbordamiento de Int.
    private fun esPrimo(n: Int): Boolean {
        if (n < 2) return false
        var d = 2
        while (d.toLong() * d <= n) {
            if (n % d == 0) return false
            d++
        }
        return true
    }
}
