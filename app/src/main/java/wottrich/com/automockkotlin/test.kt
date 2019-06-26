package wottrich.com.automockkotlin

import com.google.gson.Gson
import wottrich.com.mock_annotations.MockModel

object Obj1 {

    var list: MutableList<Any> = mutableListOf()

}

class Obj2 {

    var name: String = ""
    var obj: MutableList<Any> = mutableListOf()

}

fun main() {

    val body = "[{\"name\": \"Lucas\", \"age\":19, \"address\":\"Rua B\", \"canWork\":true, \"child\":[{\"name\":Luiz}, {\"name\":\"Leandro\"}]}, {\"name\": \"Marcelo\", \"age\":20, \"address\":\"Rua C\", \"canWork\":false}]"

    val map = Gson().fromJson(body, List::class.java) as List<Map<String, Any>>

    val obj1 = Obj1

    for (mapper in map) {

        val parameters = Obj2()

        for ((key, value) in mapper) {
            parameters.name = key
            parameters.obj.add(value)
            if (value is List<*>) {
                with(value as List<Map<String, Any>>) {

                    for (subMapper in this) {
                        val parameters2 = Obj2()
                        for ((subKey, subValue) in subMapper) {
                            parameters2.name = subKey
                            parameters2.obj.add(subValue)
                        }
                        parameters.obj.add(parameters2)
                    }
                }
            } else {

            }
        }

        obj1.list.add(parameters)

    }

    fun _for (list: MutableList<Any>) {

        //for ()

    }

    for (obj in obj1.list) {
        println((obj as Obj2).name)
        for (o2 in obj.obj) {
            o2 as Obj2

            println(o2.name)
            println(o2.obj)

        }

    }

}