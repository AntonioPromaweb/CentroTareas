package com.vasquez.centrotareas.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vasquez.centrotareas.data.TareasRepository
import com.vasquez.centrotareas.model.CargaUi
import com.vasquez.centrotareas.model.ComparacionTiempos
import com.vasquez.centrotareas.model.PrimosUi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TareasViewModel(private val repo: TareasRepository) : ViewModel() {

    // ----- Tarea con progreso (pasos 1 a 4)
    private val _mensaje = MutableStateFlow("Listo")
    val mensaje: StateFlow<String> = _mensaje.asStateFlow()

    // Paso 3: progreso de 0f a 1f. Se expone solo la versión de lectura (StateFlow);
    // el único que lo modifica es el ViewModel.
    private val _progreso = MutableStateFlow(0f)
    val progreso: StateFlow<Float> = _progreso.asStateFlow()

    // Paso 4: guardamos el Job que devuelve launch para poder cancelarlo, y un estado
    // "corriendo" que la pantalla usa para habilitar o deshabilitar los botones.
    private var trabajo: Job? = null
    private val _corriendo = MutableStateFlow(false)
    val corriendo: StateFlow<Boolean> = _corriendo.asStateFlow()

    // Pasos 2 a 4: viewModelScope.launch arranca una corrutina (empieza en el hilo principal)
    // y withContext cambia a un hilo de segundo plano para el trabajo. delay SUSPENDE la
    // corrutina sin bloquear ningún hilo, así que la pantalla sigue dibujando y atendiendo
    // toques. 100 vueltas x 40 ms = 4 segundos, y cada vuelta avanza la barra un 1 %.
    fun tareaLarga() {
        // Doble inicio: si ya hay una tarea corriendo, no lanzamos otra
        if (_corriendo.value) return
        _corriendo.value = true
        trabajo = viewModelScope.launch {
            try {
                _progreso.value = 0f
                _mensaje.value = "Trabajando..."
                Log.d("HILO", "Antes: ${Thread.currentThread().name}")
                withContext(Dispatchers.Default) {
                    Log.d("HILO", "Dentro: ${Thread.currentThread().name}")
                    for (i in 1..100) {
                        delay(40) // delay revisa la cancelación en cada vuelta
                        _progreso.value = i / 100f
                    }
                }
                Log.d("HILO", "Despues: ${Thread.currentThread().name}")
                _mensaje.value = "Terminó"
            } finally {
                // Se ejecuta siempre: si terminó bien o si la cancelaron
                _corriendo.value = false
            }
        }
    }

    // Cancela el Job: la barra se queda donde estaba y el mensaje pasa a Cancelado
    fun cancelar() {
        trabajo?.cancel()
        _mensaje.value = "Cancelado"
    }

    // ----- Contar primos (paso 5 + progreso)
    private val _primos = MutableStateFlow<PrimosUi>(PrimosUi.Inicial)
    val primos: StateFlow<PrimosUi> = _primos.asStateFlow()

    // Job propio para poder cancelar el conteo sin afectar a la otra tarea
    private var trabajoPrimos: Job? = null

    // Cuenta primos hasta "hasta", publica progreso (Avance) y al finalizar Listo o Cancelado
    fun contar(hasta: Int) {
        if (_primos.value is PrimosUi.Calculando || _primos.value is PrimosUi.Avance) return // evita doble inicio
        _primos.value = PrimosUi.Calculando
        trabajoPrimos = viewModelScope.launch {
            val inicio = System.currentTimeMillis()
            try {
                repo.contarPrimosConProgreso(hasta).collect { avance ->
                    if (avance.porcentaje < 100) {
                        _primos.value = PrimosUi.Avance(avance.porcentaje, avance.cantidad)
                    } else {
                        val duracion = System.currentTimeMillis() - inicio
                        _primos.value = PrimosUi.Listo(avance.cantidad, duracion)
                    }
                }
            } catch (e: CancellationException) {
                _primos.value = PrimosUi.Cancelado
                throw e // siempre se vuelve a lanzar para no romper la cancelación
            }
        }
    }

    fun cancelarPrimos() {
        trabajoPrimos?.cancel()
    }

    // ----- Dos cargas (pasos 6 y 7 + reintentos automáticos + comparación de tiempos)
    private val _carga = MutableStateFlow<CargaUi>(CargaUi.Inicial)
    val carga: StateFlow<CargaUi> = _carga.asStateFlow()

    private val _comparacion = MutableStateFlow(ComparacionTiempos())
    val comparacion: StateFlow<ComparacionTiempos> = _comparacion.asStateFlow()

    // Paso 7: interruptor para forzar que cargarPedidos falle (para probar el manejo de errores)
    private val _simularError = MutableStateFlow(false)
    val simularError: StateFlow<Boolean> = _simularError.asStateFlow()

    fun cambiarSimularError(valor: Boolean) {
        _simularError.value = valor
    }

    // Reintento automático: ejecuta el bloque hasta "maximo" veces. Entre un intento y el
    // siguiente espera 1 s, 2 s... (espera creciente) con delay, que no bloquea ningún hilo.
    // Si agota los intentos, relanza el último error para que lo muestre el catch de afuera.
    private suspend fun <T> conReintentos(
        maximo: Int = 3,
        alReintentar: (Int) -> Unit,
        bloque: suspend () -> T
    ): T {
        var intento = 1
        while (true) {
            try {
                return bloque()
            } catch (e: CancellationException) {
                throw e // La cancelación se relanza inmediatamente sin reintentar
            } catch (e: Exception) {
                if (intento < maximo) {
                    val siguienteIntento = intento + 1
                    alReintentar(siguienteIntento)
                    delay(1000L * intento)
                    intento = siguienteIntento
                } else {
                    throw e
                }
            }
        }
    }

    // Una por una: la segunda espera a que termine la primera -> ~5 s (2 + 3)
    fun cargarUnaPorUna() {
        if (_carga.value is CargaUi.Cargando || _carga.value is CargaUi.Reintentando) return // evita doble inicio
        _carga.value = CargaUi.Cargando
        viewModelScope.launch {
            val inicio = System.currentTimeMillis()
            try {
                val perfil = repo.cargarPerfil()                         // espera 2 s (sin reintentos)
                val pedidos = conReintentos(
                    maximo = 3,
                    alReintentar = { intento -> _carga.value = CargaUi.Reintentando(intento, 3) }
                ) {
                    repo.cargarPedidos(_simularError.value)
                }
                val duracion = System.currentTimeMillis() - inicio
                _carga.value = CargaUi.Exito(
                    modo = "Una por una",
                    perfil = perfil,
                    pedidos = pedidos.size,
                    milisegundos = duracion
                )
                // Acumula la medición exitosa conservando solo las últimas 5
                _comparacion.value = _comparacion.value.copy(
                    unaPorUna = (_comparacion.value.unaPorUna + duracion).takeLast(5)
                )
            } catch (e: CancellationException) {
                throw e // la cancelación NO es un error: se vuelve a lanzar
            } catch (e: Exception) {
                _carga.value = CargaUi.Error("No se pudo cargar: " + e.message)
            }
        }
    }

    // A la vez: async lanza las dos cargas al mismo tiempo y await() espera los resultados
    // -> ~3 s (lo que tarda la más lenta). coroutineScope hace que, si una falla o se
    // cancela, la otra también se cancele. La excepción sale por await() y se atrapa fuera.
    fun cargarALaVez() {
        if (_carga.value is CargaUi.Cargando || _carga.value is CargaUi.Reintentando) return
        _carga.value = CargaUi.Cargando
        viewModelScope.launch {
            val inicio = System.currentTimeMillis()
            try {
                coroutineScope {
                    val perfil = async { repo.cargarPerfil() }
                    val pedidos = async {
                        conReintentos(
                            maximo = 3,
                            alReintentar = { intento -> _carga.value = CargaUi.Reintentando(intento, 3) }
                        ) {
                            repo.cargarPedidos(_simularError.value)
                        }
                    }
                    // Primero se espera a las dos cargas y recién después se mide el tiempo
                    val perfilCargado = perfil.await()
                    val pedidosCargados = pedidos.await()
                    val duracion = System.currentTimeMillis() - inicio
                    _carga.value = CargaUi.Exito(
                        modo = "A la vez",
                        perfil = perfilCargado,
                        pedidos = pedidosCargados.size,
                        milisegundos = duracion
                    )
                    // Acumula la medición exitosa conservando solo las últimas 5
                    _comparacion.value = _comparacion.value.copy(
                        aLaVez = (_comparacion.value.aLaVez + duracion).takeLast(5)
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _carga.value = CargaUi.Error("No se pudo cargar: " + e.message)
            }
        }
    }

    // Reintentar: vuelve a ejecutar la carga "A la vez"
    fun reintentar() {
        cargarALaVez()
    }
}
