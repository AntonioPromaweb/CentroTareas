# CentroTareas

App Android (Kotlin + Jetpack Compose + Material 3) que muestra cómo mantener la pantalla fluida con corrutinas.

## Observación del Paso 1

**Qué pasa con la pantalla.** Al tocar «Tarea larga (MAL)» y enseguida «Tócame», durante 4 segundos nada responde: el contador no suma y la pantalla queda congelada hasta que termina la espera.

**Por qué no se ve «Trabajando...».** `tareaLargaMala()` cambia el mensaje a «Trabajando...» y después ejecuta `Thread.sleep(4000)` en el hilo principal. Ese mismo hilo es el que dibuja la pantalla, así que no puede actualizarla mientras duerme. Cuando por fin termina, el mensaje ya cambió a «Terminó» y es lo único que se llega a dibujar; «Trabajando...» nunca se pinta.

**Qué mostraría Android con 10 segundos.** Android mostraría el diálogo ANR («CentroTareas no responde»), con las opciones de cerrar la app o esperar, porque el hilo principal pasaría más de unos 5 segundos sin atender los toques. Lo comprobé incluso con la espera de 4 segundos: al tocar la pantalla varias veces mientras estaba congelada, apareció ese diálogo.
