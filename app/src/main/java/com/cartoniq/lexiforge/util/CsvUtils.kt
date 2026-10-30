package com.cartoniq.lexiforge.util

import com.cartoniq.lexiforge.data.CardEntity

data class CsvRow(val front: String, val back: String, val example: String)

object CsvUtils {

    fun parse(rawLines: List<String>): List<CsvRow> {
        val lines = rawLines.map { it.trimEnd('\r') }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val first = lines.first().removePrefix("\uFEFF")
        val delimiter = when {
            first.contains('\t') -> '\t'
            first.count { it == ';' } > first.count { it == ',' } -> ';'
            else -> ','
        }

        val rows = ArrayList<CsvRow>()
        lines.forEachIndexed { index, line ->
            val text = if (index == 0) line.removePrefix("\uFEFF") else line
            val cells = parseLine(text, delimiter).map { it.trim() }
            if (cells.size < 2) return@forEachIndexed
            if (index == 0 && cells[0].equals("front", ignoreCase = true) &&
                cells[1].equals("back", ignoreCase = true)
            ) return@forEachIndexed
            if (cells[0].isEmpty() || cells[1].isEmpty()) return@forEachIndexed
            rows.add(CsvRow(cells[0], cells[1], cells.getOrElse(2) { "" }))
        }
        return rows
    }

    private fun parseLine(line: String, delimiter: Char): List<String> {
        val result = ArrayList<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        sb.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                } else {
                    sb.append(c)
                }
            } else if (c == '"') {
                inQuotes = true
            } else if (c == delimiter) {
                result.add(sb.toString())
                sb.setLength(0)
            } else {
                sb.append(c)
            }
            i++
        }
        result.add(sb.toString())
        return result
    }

    fun toCsv(cards: List<CardEntity>): String {
        val sb = StringBuilder("front,back,example\n")
        cards.forEach {
            sb.append(quote(it.front)).append(',')
                .append(quote(it.back)).append(',')
                .append(quote(it.example)).append('\n')
        }
        return sb.toString()
    }

    private fun quote(value: String): String =
        "\"" + value.replace("\"", "\"\"").replace("\n", " ") + "\""
}
