package com.kl3jvi.annotationprocessor

import com.kl3jvi.annotations.Encapsulate

@Encapsulate
data class Model(val counter: Int,
                 val post : String)