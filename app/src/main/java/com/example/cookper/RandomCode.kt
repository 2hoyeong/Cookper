package com.example.cookper

import kotlin.random.Random

class RandomCode {

    private val code = listOf(
        "0", "1", "2", "3",
        "4", "5", "6", "7",
        "8", "9", "A", "B",
        "C", "D", "E", "F",
        "G", "H", "I", "J"
    )

    fun codeGenerator() : String {
        var result = ""

        while(result.length < 6) {
            result += code[Random.nextInt(code.size)]
        }
        return result
    }
}