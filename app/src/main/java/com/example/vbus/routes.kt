package com.example.vbus

val routes: Map<String, List<String>> = mapOf(
    "route1" to listOf("1","2","3"),
    "route2" to listOf("4", "5"),
)

val student_routes: Map<String, Map<String, Map<String, List<String>>>> = mapOf(
    "route1" to mapOf(
        "1" to mapOf(
            "s1" to listOf("varun.g2024@vitstudent.ac.in",
                "sudharshan.m2024@vitstudent.ac.in",
                "arshad.md@vitstudent.ac.in",
                "umang.doshi2024@vitstudent.ac.in"),
            "s2" to listOf("st1","st2","st3","st4","st5","st6","st7","st8",
                "harsh.goel2024@vitstudent.ac.in"),
            "s3" to listOf("st9","st10","st11","st12","st13","st14","yash.goel2024@vitstudent.ac.in")
        )
    ),
    "route2" to mapOf(

    )
)