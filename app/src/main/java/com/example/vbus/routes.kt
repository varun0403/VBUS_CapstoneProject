package com.example.vbus

val routes: Map<String, List<String>> = mapOf(
    "route1" to listOf("1","2","3"),
    "route2" to listOf("4", "5"),
)

val bus_checkpoints: Map<String, List<String>> = mapOf(
    "route1" to setOf("A","B","C","D","E","F").toList(),
    "route2" to setOf("G","D","B","C","E","F").toList(),
    "route3" to setOf("H","A","I","E","F").toList()
)