package com.tuempresa.premiumplayer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase Application de PremiumPlayer.
 *
 * Al heredar de Application, esta clase se ejecuta
 * automáticamente cuando la app arranca, ANTES que cualquier
 * Activity o Service. Aquí inicializamos Hilt para que
 * toda la app pueda usar inyección de dependencias.
 */
@HiltAndroidApp
class PremiumPlayerApp : Application()
