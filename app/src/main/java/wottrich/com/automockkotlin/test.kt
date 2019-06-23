package wottrich.com.automockkotlin

import wottrich.com.mock_annotations.MockModel

@MockModel(customName = "Person2", body = "[{\"name\": \"Lucas\", \"age\":19, \"address\":\"Rua B\", \"canWork\":true}, {\"name\": \"Marcelo\", \"age\":20, \"address\":\"Rua C\", \"canWork\":false}]")
class Mock {

}

fun main() {

    val body = "[{\"name\": \"Lucas\", \"age\":19, \"address\":\"Rua B\", \"canWork\":true}, {\"name\": \"Marcelo\", \"age\":20, \"address\":\"Rua C\", \"canWork\":false}]"

    //val file = File()

    //println(file)

}