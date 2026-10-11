package br.com.ismaelcell.ensaio

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var root: LinearLayout
    private val fields = linkedMapOf<String, EditText>()
    private val instrumentNames = listOf(
        "Violinos","Violas","Violoncelo","Oboé","Oboé D'amore","Corne Inglês",
        "Flauta Transversal","Clarinete","Clarinete Alto","Clarinete Baixo","Fagote",
        "Saxofone Soprano","Saxofone Alto","Saxofone Tenor","Saxofone Barítono",
        "Trompete","Cornet","Flugelhorn","Trompa","Trombone","Trombonito",
        "Barítono","Eufônio","Tuba","Acordeon"
    )
    private val ministryNames = listOf(
        "Anciães","Diáconos","Cooperadores","Cooperadores de Jovens",
        "Total Ministério","Encarregados Regionais","Encarregados Locais",
        "Examinadoras","Instrutores","Instrutoras","Irmandade"
    )
    private val hymnNames = (1..25).map { "Hino $it" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
    }

    private fun buildUi() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 18, 24, 18)
            setBackgroundColor(0xFFF7F8FA.toInt())
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        content.addView(TextView(this).apply {
            text = "FICHA DE CONTAGEM\nENSAIO REGIONAL"
            textSize = 23f; setTextColor(0xFF172554.toInt()); gravity = Gravity.CENTER
            setPadding(0, 8, 0, 18); paint.isFakeBoldText = true
        })
        addField(content, "localidade", "Localidade", "Sítio Água Vermelha")
        addField(content, "data", "Data (DD/MM/AAAA)", SimpleDateFormat("dd/MM/yyyy", Locale("pt","BR")).format(Date()))
        addField(content, "presidencia", "Presidência")
        addField(content, "palavra", "Palavra")
        addField(content, "regencia1", "Regência 1")
        addField(content, "regencia2", "Regência 2")

        section(content, "INSTRUMENTOS — informe as quantidades")
        instrumentNames.forEach { addField(content, "inst_$it", it, "0", true) }

        section(content, "MINISTÉRIO E PARTICIPANTES")
        ministryNames.forEach { addField(content, "min_$it", it, "0", true) }

        section(content, "HINOS ENSAIADOS")
        for (i in 1..25) addField(content, "hino_$i", "Hino $i (número ou título)", "", false)

        addField(content, "observacoes", "Observações gerais", "", false, multiline = true)
        addField(content, "total_organistas", "Total de organistas", "0", true)
        addField(content, "total_musicos_manual", "Total de músicos (deixe 0 para calcular)", "0", true)

        val totals = TextView(this).apply {
            textSize = 16f; setTextColor(0xFF172554.toInt()); setPadding(0, 12, 0, 12)
        }
        content.addView(totals)
        val updateTotals = {
            val instruments = instrumentNames.sumOf { fields["inst_$it"]?.text.toString().toIntOrNull() ?: 0 }
            val organists = fields["total_organistas"]?.text.toString().toIntOrNull() ?: 0
            val manualMusicians = fields["total_musicos_manual"]?.text.toString().toIntOrNull() ?: 0
            val musicians = if (manualMusicians > 0) manualMusicians else instruments
            val ministry = listOf("Anciães","Diáconos","Cooperadores","Cooperadores de Jovens").sumOf {
                fields["min_$it"]?.text.toString().toIntOrNull() ?: 0
            }
            val brotherhood = fields["min_Irmandade"]?.text.toString().toIntOrNull() ?: 0
            val overall = musicians + organists + ministry + brotherhood
            totals.text = "Prévia dos totais\nMúsicos: $musicians  |  Organistas: $organists\n" +
                "Ministério (soma dos quatro primeiros campos): $ministry\n" +
                "Irmandade: $brotherhood\nTotal geral calculado: $overall\n" +
                "Revise os totais antes de gerar o PDF."
        }
        fields.values.forEach { edit ->
            edit.addTextChangedListener(SimpleWatcher { updateTotals() })
        }
        updateTotals()

        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val save = Button(this).apply { text = "Salvar ficha" }
        val history = Button(this).apply { text = "Histórico" }
        val pdf = Button(this).apply { text = "Gerar PDF" }
        row.addView(save, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(history, LinearLayout.LayoutParams(0, -2, 1f))
        content.addView(row)
        content.addView(pdf)
        save.setOnClickListener { saveRecord(); toast("Ficha salva no histórico deste celular.") }
        history.setOnClickListener { showHistory() }
        pdf.setOnClickListener {
            AlertDialog.Builder(this).setTitle("Revisar antes de gerar")
                .setMessage(totals.text.toString() + "\n\nGerar PDF A4 com os dados atuais?")
                .setNegativeButton("Voltar", null)
                .setPositiveButton("Gerar PDF") { _, _ -> generatePdf() }.show()
        }
    }

    private fun addField(parent: LinearLayout, key: String, label: String, default: String = "", numeric: Boolean = false, multiline: Boolean = false) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 3, 0, 3) }
        box.addView(TextView(this).apply { text = label; textSize = 13f; setTextColor(0xFF334155.toInt()) })
        val edit = EditText(this).apply {
            setSingleLine(!multiline)
            textSize = 15f
            setPadding(12, 7, 12, 7)
            setBackgroundColor(0xFFFFFFFF.toInt())
            if (numeric) inputType = android.text.InputType.TYPE_CLASS_NUMBER
            if (multiline) minLines = 3
            setText(default)
        }
        fields[key] = edit
        box.addView(edit, LinearLayout.LayoutParams(-1, if (multiline) 100 else 48))
        parent.addView(box)
    }

    private fun section(parent: LinearLayout, title: String) {
        parent.addView(TextView(this).apply {
            text = title; textSize = 16f; paint.isFakeBoldText = true
            setTextColor(0xFF1D4ED8.toInt()); setPadding(0, 18, 0, 8)
        })
    }

    private fun currentRecord(): JSONObject {
        val obj = JSONObject()
        obj.put("savedAt", System.currentTimeMillis())
        fields.forEach { (key, edit) -> obj.put(key, edit.text.toString()) }
        return obj
    }

    private fun historyFile() = File(filesDir, "fichas_ensaio.json")
    private fun readHistory(): JSONArray = try {
        if (historyFile().exists()) JSONArray(historyFile().readText()) else JSONArray()
    } catch (_: Exception) { JSONArray() }

    private fun saveRecord() {
        val arr = readHistory()
        arr.put(currentRecord())
        historyFile().writeText(arr.toString())
    }

    private fun showHistory() {
        val arr = readHistory()
        if (arr.length() == 0) {
            AlertDialog.Builder(this).setMessage("Ainda não há fichas salvas.").setPositiveButton("OK", null).show()
            return
        }
        val labels = (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            "${o.optString("localidade")} — ${o.optString("data")} (${SimpleDateFormat("dd/MM HH:mm", Locale("pt","BR")).format(Date(o.optLong("savedAt")))})"
        }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Histórico local")
            .setItems(labels) { _, which ->
                val selected = arr.getJSONObject(which)
                fields.forEach { (key, edit) -> edit.setText(selected.optString(key, "")) }
                AlertDialog.Builder(this).setMessage("Ficha carregada. Você pode revisar e gerar o PDF.")
                    .setPositiveButton("OK", null).show()
            }.setNegativeButton("Fechar", null).show()
    }

    private fun generatePdf() {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)
        val c = page.canvas
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF111827.toInt(); textSize = 9f }
        val bold = Paint(p).apply { typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD) }
        val line = Paint(p).apply { strokeWidth = 0.7f }
        val left = 28f
        var y = 30f
        fun txt(s: String, x: Float, yy: Float, paint: Paint = p) { c.drawText(s.take(105), x, yy, paint) }
        fun rule(yy: Float) { c.drawLine(left, yy, 567f, yy, line) }
        txt("LISTA PARA CONTAGEM DO ENSAIO REGIONAL", left, y, Paint(bold).apply { textSize = 17f })
        y += 22
        txt("Localidade: ${value("localidade")}     Data: ${value("data")}", left, y, bold); y += 15
        txt("Presidência: ${value("presidencia")}", left, y); y += 13
        txt("Palavra: ${value("palavra")}", left, y); y += 13
        txt("Regência 1: ${value("regencia1")}     Regência 2: ${value("regencia2")}", left, y); y += 17
        rule(y); y += 13

        val leftX = left
        val rightX = 315f
        txt("INSTRUMENTOS", leftX, y, bold)
        txt("QTD.", 245f, y, bold)
        txt("MINISTÉRIO E PARTICIPANTES", rightX, y, bold)
        txt("QTD.", 520f, y, bold)
        y += 14
        val topY = y
        val rowH = 18f
        val instRows = instrumentNames.size
        val minRows = ministryNames.size
        val maxRows = maxOf(instRows, minRows)
        for (i in 0 until maxRows) {
            if (i < instRows) {
                txt(instrumentNames[i], leftX, y)
                txt(value("inst_${instrumentNames[i]}").ifBlank { "—" }, 248f, y)
            }
            if (i < minRows) {
                txt(ministryNames[i], rightX, y)
                txt(value("min_${ministryNames[i]}").ifBlank { "—" }, 530f, y)
            }
            rule(y + 4)
            y += rowH
        }
        y += 3
        txt("Total de músicos: ${musicians()}", leftX, y, bold); y += 14
        txt("Total de organistas: ${value("total_organistas")}", leftX, y, bold); y += 14
        txt("Total geral calculado: ${grandTotal()}", leftX, y, bold); y += 18
        rule(y); y += 14
        txt("HINOS ENSAIADOS", leftX, y, bold); y += 15
        val hymns = (1..25).mapNotNull { i -> value("hino_$i").trim().takeIf { it.isNotEmpty() }?.let { "$i. $it" } }
        if (hymns.isEmpty()) { txt("—", leftX, y); y += 14 } else {
            hymns.chunked(3).forEach { chunk ->
                txt(chunk.joinToString("     "), leftX, y); y += 13
            }
        }
        y += 4; rule(y); y += 15
        txt("OBSERVAÇÕES GERAIS", leftX, y, bold); y += 14
        val obs = value("observacoes").ifBlank { "—" }
        obs.chunked(95).take(5).forEach { txt(it, leftX, y); y += 12 }
        y += 15
        txt("Conferir os dados e totais antes de assinar ou distribuir.", leftX, minOf(y, 815f))
        doc.finishPage(page)

        val out = File(getExternalFilesDir(null), "Ficha_Ensaio_${System.currentTimeMillis()}.pdf")
        try {
            FileOutputStream(out).use { doc.writeTo(it) }
            doc.close()
            val uri = androidxUriForFile(out)
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(share, "Salvar ou compartilhar PDF"))
        } catch (e: Exception) {
            doc.close()
            AlertDialog.Builder(this).setMessage("Não foi possível gerar o PDF: ${e.message}")
                .setPositiveButton("OK", null).show()
        }
    }

    private fun androidxUriForFile(file: File): Uri {
        // O PDF fica na pasta privada externa do app; abre o seletor de documentos com cópia temporária compartilhável.
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Documents/EnsaioRegional")
        }
        val uri = contentResolver.insert(android.provider.MediaStore.Files.getContentUri("external"), contentValues)
        if (uri != null) contentResolver.openOutputStream(uri)?.use { output -> file.inputStream().use { it.copyTo(output) } }
        return uri ?: Uri.fromFile(file)
    }

    private fun value(key: String) = fields[key]?.text?.toString().orEmpty()
    private fun musicians(): Int {
        val manual = value("total_musicos_manual").toIntOrNull() ?: 0
        return if (manual > 0) manual else instrumentNames.sumOf { value("inst_$it").toIntOrNull() ?: 0 }
    }
    private fun grandTotal(): Int {
        val organists = value("total_organistas").toIntOrNull() ?: 0
        val ministry = listOf("Anciães","Diáconos","Cooperadores","Cooperadores de Jovens").sumOf { value("min_$it").toIntOrNull() ?: 0 }
        val brotherhood = value("min_Irmandade").toIntOrNull() ?: 0
        return musicians() + organists + ministry + brotherhood
    }
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}

private class SimpleWatcher(val changed: () -> Unit) : android.text.TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { changed() }
    override fun afterTextChanged(s: android.text.Editable?) {}
}
