package com.slavko.annotationsgenerator

import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor


@TmgCrudGeneration
@AllArgsConstructor
@NoArgsConstructor
class Person {
    val name: String
        get() {
            TODO()
        }
    val age: Int


        get() {
            TODO()
        }


}



