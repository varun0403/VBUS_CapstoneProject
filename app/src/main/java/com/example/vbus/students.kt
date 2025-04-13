package com.example.vbus

val students: Map<String,List<String>> = mapOf(
    "1" to listOf("varun.g2024@vitstudent.ac.in","arshad.md2024@vitstudent.ac.in", "sudharshan.m2024@vitstudent.ac.in",
        "st1","st2","st3","st4","st5","st6","st7","st8","st9","st10","st11","st12","st13","st14",
        "harsh.goel2024@vitstudent.ac.in","umang.doshi2024@vitstudent.ac.in","yash.goel2024@vitstudent.ac.in"),
    "2" to listOf("dhanushya.b2024@vitstudent.ac.in","surya.k2024@vitstudent.ac.in","fida.fahima2024@vitstudent.ac.in",
        "st1","st2","st3","st4","st5","st6","st7","st8","st9","st10","st11","st12","st13","st14",
        "bianca.joshini2024@vitstudent.ac.in","kala.r2024@vitstudent.ac.in","aksa.biju2024@vitstudent.ac.in"),
    "3" to listOf("arya.s2024@vitstudent.ac.in","hami.yasir2024@vitstudent.ac.in","jeffrey.thomas2024@vitstudent.ac.in",
        "st1","st2","st3","st4","st5","st6","st7","st8","st9","st10","st11","st12","st13","st14",
        "gokul.n2024@vitstudent.ac.in","hari.raju2024@vitstudent.ac.in","sivas.d2024@vitstudent.ac.in")
)

val studentBoardingStatus: Map<String, MutableMap<String, Int>> = mapOf(
    "1" to mutableMapOf(
        "varun.g2024@vitstudent.ac.in" to 0,
        "arshad.md2024@vitstudent.ac.in" to 0,
        "sudharshan.m2024@vitstudent.ac.in" to 0,
        "st1" to 0, "st2" to 0, "st3" to 0, "st4" to 0, "st5" to 0,
        "st6" to 0, "st7" to 0, "st8" to 0, "st9" to 0, "st10" to 0,
        "st11" to 0, "st12" to 0, "st13" to 0, "st14" to 0,
        "harsh.goel2024@vitstudent.ac.in" to 0,
        "umang.doshi2024@vitstudent.ac.in" to 0,
        "yash.goel2024@vitstudent.ac.in" to 0
    ),
    "2" to mutableMapOf(
        "dhanushya.b2024@vitstudent.ac.in" to 0,
        "surya.k2024@vitstudent.ac.in" to 0,
        "fida.fahima2024@vitstudent.ac.in" to 0,
        "bianca.joshini2024@vitstudent.ac.in" to 0,
        "kala.r2024@vitstudent.ac.in" to 0,
        "aksa.biju2024@vitstudent.ac.in" to 0,
        "st1" to 0, "st2" to 0, "st3" to 0, "st4" to 0, "st5" to 0,
        "st6" to 0, "st7" to 0, "st8" to 0, "st9" to 0, "st10" to 0,
        "st11" to 0, "st12" to 0, "st13" to 0, "st14" to 0
    ),
    "3" to mutableMapOf(
        "arya.s2024@vitstudent.ac.in" to 0,
        "hami.yasir2024@vitstudent.ac.in" to 0,
        "jeffrey.thomas2024@vitstudent.ac.in" to 0,
        "gokul.n2024@vitstudent.ac.in" to 0,
        "hari.raju2024@vitstudent.ac.in" to 0,
        "sivas.d2024@vitstudent.ac.in" to 0,
        "st1" to 0, "st2" to 0, "st3" to 0, "st4" to 0, "st5" to 0,
        "st6" to 0, "st7" to 0, "st8" to 0, "st9" to 0, "st10" to 0,
        "st11" to 0, "st12" to 0, "st13" to 0, "st14" to 0
    )
)

val student_stops: Map<String, MutableMap<String, MutableList<String>>> = mapOf(
    "1" to mutableMapOf(
        "s1" to mutableListOf("varun.g2024@vitstudent.ac.in","arshad.md2024@vitstudent.ac.in",
            "sudharshan.m2024@vitstudent.ac.in","harsh.goel2024@vitstudent.ac.in",
            "umang.doshi2024@vitstudent.ac.in","yash.goel2024@vitstudent.ac.in"),
        "s2" to mutableListOf("st1","st2","st3", "st4","st5","st6","st7"),
        "s3" to mutableListOf("st8","st9","st10","st11","st12","st13","st14")
    ),
    "2" to mutableMapOf(
        "s1" to mutableListOf(
            "dhanushya.b2024@vitstudent.ac.in","surya.k2024@vitstudent.ac.in",
            "fida.fahima2024@vitstudent.ac.in, bianca.joshini2024@vitstudent.ac.in",
            "kala.r2024@vitstudent.ac.in","aksa.biju2024@vitstudent.ac.in"),
        "s2" to mutableListOf(
            "st1","st2","st3", "st4","st5","st6","st7","st8","st9","st10","st11","st12","st13","st14"
        )
    ),
    "3" to mutableMapOf(
        "s1" to mutableListOf(
            "arya.s2024@vitstudent.ac.in","hami.yasir2024@vitstudent.ac.in",
            "jeffrey.thomas2024@vitstudent.ac.in","gokul.n2024@vitstudent.ac.in",
            "hari.raju2024@vitstudent.ac.in","sivas.d2024@vitstudent.ac.in"),
        "s2" to mutableListOf(
            "st1","st2","st3", "st4","st5","st6","st7","st8","st9","st10","st11","st12","st13","st14"
        )
    )
)
