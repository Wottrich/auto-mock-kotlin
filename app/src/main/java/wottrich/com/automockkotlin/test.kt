package wottrich.com.automockkotlin

import com.google.gson.Gson


fun main() {

    //val data = "{\"name\":\"Lucas\"}"

    val data = "{\"name\":\"Lucas\", \"age\":23}"

    val gson = Gson()
    val a = gson.fromJson(data, Map::class.java) as Map<String, Any>


    println(a.values)

}