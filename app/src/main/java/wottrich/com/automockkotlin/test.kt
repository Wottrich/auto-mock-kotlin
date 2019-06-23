package wottrich.com.automockkotlin

import com.google.gson.Gson
import wottrich.com.mock_annotations.MockModel

@MockModel(customName = "Person", body = "[{\"name\": \"Lucas\", \"age\":19, \"address\":\"Rua B\", \"canWork\":true}, {\"name\": \"Marcelo\", \"age\":20, \"address\":\"Rua C\", \"canWork\":false}]")
class Mock {

}

fun main() {

    for (person in Person.list) {
        println(person.name)
    }

}