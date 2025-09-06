
package com.example.app2

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Chronometer
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private var pontuacaoTimeA = 0
    private var pontuacaoTimeB = 0
    private var partidaIniciada = false

    private lateinit var pTimeA: TextView
    private lateinit var pTimeB: TextView
    private lateinit var cronometro: Chronometer
    private lateinit var btnIniciar: Button

    // Listas de gols por time
    private lateinit var listaGolsA: ListView
    private lateinit var listaGolsB: ListView
    private lateinit var adapterA: ArrayAdapter<String>
    private lateinit var adapterB: ArrayAdapter<String>
    private val golsA = mutableListOf<String>()
    private val golsB = mutableListOf<String>()

    private lateinit var handler: Handler

    private val checkRunnable = object : Runnable {
        override fun run() {
            if (partidaIniciada) {
                checarPartidaTerminou()
                handler.postDelayed(this, 1000) // Check again after 1 second
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.layout_main)

        handler = Handler(Looper.getMainLooper())

        // Views de placar
        pTimeA = findViewById(R.id.placarTimeA)
        pTimeB = findViewById(R.id.placarTimeB)

        // Cronômetro e iniciar
        cronometro = findViewById(R.id.cronometro)
        btnIniciar = findViewById(R.id.iniciarPartida)

        // ListViews e adapters
        listaGolsA = findViewById(R.id.listaGolsTimeA)
        listaGolsB = findViewById(R.id.listaGolsTimeB)
        adapterA = ArrayAdapter(this, android.R.layout.simple_list_item_1, golsA)
        adapterB = ArrayAdapter(this, android.R.layout.simple_list_item_1, golsB)
        listaGolsA.adapter = adapterA
        listaGolsB.adapter = adapterB

        // Botões Time A
        val bTresPontosTimeA: Button = findViewById(R.id.tresPontosA)
        val bDoisPontosTimeA: Button = findViewById(R.id.doisPontosA)
        val bTiroLivreTimeA: Button = findViewById(R.id.tiroLivreA)

        // Botões Time B
        val bTresPontosTimeB: Button = findViewById(R.id.tresPontosB)
        val bDoisPontosTimeB: Button = findViewById(R.id.doisPontosB)
        val bTiroLivreTimeB: Button = findViewById(R.id.tiroLivreB)

        // Reiniciar
        val bReiniciar: Button = findViewById(R.id.pararPartida)

        // Iniciar partida
        btnIniciar.setOnClickListener {
            if (!partidaIniciada) {
                cronometro.base = SystemClock.elapsedRealtime()
                cronometro.start()
                partidaIniciada = true
                startPeriodicCheck() // Start periodic check
                Toast.makeText(this, "Partida iniciada!", Toast.LENGTH_SHORT).show()
            }
        }

        // Listeners de pontuação
        bTresPontosTimeA.setOnClickListener { adicionarPontos(3, "A") }
        bDoisPontosTimeA.setOnClickListener { adicionarPontos(2, "A") }
        bTiroLivreTimeA.setOnClickListener { adicionarPontos(1, "A") }

        bTresPontosTimeB.setOnClickListener { adicionarPontos(3, "B") }
        bDoisPontosTimeB.setOnClickListener { adicionarPontos(2, "B") }
        bTiroLivreTimeB.setOnClickListener { adicionarPontos(1, "B") }

        bReiniciar.setOnClickListener { finalizarPartida() }
    }

    private fun startPeriodicCheck() {
        handler.post(checkRunnable)
    }

    private fun stopPeriodicCheck() {
        handler.removeCallbacks(checkRunnable)
    }

    private fun adicionarPontos(pontos: Int, time: String) {
        if (!partidaIniciada) {
            Toast.makeText(this, "Inicie a partida primeiro!", Toast.LENGTH_SHORT).show()
            return
        }

        val tempoSeg = ((SystemClock.elapsedRealtime() - cronometro.base) / 1000).toInt()
        val tempoFormatado = String.format("%02d:%02d", tempoSeg / 60, tempoSeg % 60)

        if (time == "A") {
            pontuacaoTimeA += pontos
            pTimeA.text = pontuacaoTimeA.toString()
            golsA.add("+$pontos pt aos $tempoFormatado")
            adapterA.notifyDataSetChanged()
            listaGolsA.smoothScrollToPosition(golsA.size - 1)
        } else {
            pontuacaoTimeB += pontos
            pTimeB.text = pontuacaoTimeB.toString()
            golsB.add("+$pontos pt aos $tempoFormatado")
            adapterB.notifyDataSetChanged()
            listaGolsB.smoothScrollToPosition(golsB.size - 1)
        }

         checarPartidaTerminou()
    }
    private fun checarPartidaTerminou() {
        val tempoSeg = ((SystemClock.elapsedRealtime() - cronometro.base) / 1000).toInt()

        if (!partidaIniciada) return

        val deveTerminar = tempoSeg >= 10 || pontuacaoTimeA >= 20 || pontuacaoTimeB >= 20

        if (!deveTerminar) {
            return
        }

        if(pontuacaoTimeA == pontuacaoTimeB){
            Toast.makeText(this, "Partida terminada! Empate!", Toast.LENGTH_LONG).show()
        }
        else{
            val vencedor = if (pontuacaoTimeA > pontuacaoTimeB) "Time A" else "Time B"
            Toast.makeText(this, "Partida terminada! Vencedor: $vencedor", Toast.LENGTH_LONG).show()
        }

        finalizarPartida()
    }
    private fun finalizarPartida() {
        stopPeriodicCheck()

        // Zera placares
        pontuacaoTimeA = 0
        pontuacaoTimeB = 0
        pTimeA.text = "0"
        pTimeB.text = "0"

        // Limpa listas
        golsA.clear()
        golsB.clear()
        adapterA.notifyDataSetChanged()
        adapterB.notifyDataSetChanged()

        // Reseta cronômetro
        cronometro.stop()
        cronometro.base = SystemClock.elapsedRealtime()
        partidaIniciada = false

        Toast.makeText(this, "Partida finalizada", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPeriodicCheck()
    }
}
