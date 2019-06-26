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

fun ex1 () {
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



fun main() {

    val body = "{\"simulation\": {\"id\": \"FUSeneoSTDoN_A7jSHpNUg\", \"car\": {\"id\": \"aYdDChhWtH2UGMAhvzzLsQ\", \"description\": \"Clio\", \"url\": null, \"brand\": \"Clio\", \"selected\": true, \"gender\": \"PARTICULAR\", \"vehicleType\": \"USADO\", \"version\": {\"id\": \"gdakHXqtMpXzHY5fi1VM1A\", \"description\": \"Clio Campus Hi-Flex 1.0 16V 3p\", \"fipe\": \"025150-0\", \"yearManufacture\": 2010, \"yearModel\": 2010, \"price\": 15000, \"fuel\": {\"id\": \"4tceabM1b30zdE6xH8mTtQ\", \"description\": \"Gasolina\", \"importCode\": \"G\", \"active\": true}, \"options\": [], \"acessories\": []}}}}"

    val map = Gson().fromJson(body, Any::class.java)

    if (map is List<*>) {
        println("list")
    } else if (map is Map<*, *>) {
        val mapper = map as Map<String, Any>
        for ((key, value) in mapper) {

            println(key)

            if (value is Map<*, *>) {
                val submapper = value as Map<String, Any>
                loadGson(0, submapper)
            }

        }

    }
}

fun loadGson (count: Int, map: Map<String, Any>) {
    var _count = count
    for ((key, value) in map) {

        if (value is Map<*, *>) {
            val mapper = value as Map<String, Any>
            _count++
            println("$count sub - $mapper")
            loadGson(count, mapper)
        } else {
            println("$key - $value")
        }

    }

}