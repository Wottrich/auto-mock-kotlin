package com.example.mockprocessor

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import kotlin.reflect.KClass

fun getProperty (name: String, type: KClass<*>) : PropertySpec.Builder {
    return PropertySpec.builder(name, type)
}


class InternalProperty {
    var nameField: String = ""
    lateinit var typeClass: KClass<*>
    var modifiers: KModifier = KModifier.PUBLIC
    var format: String = ""
    var args: Any? = null


    fun returnProperty () : PropertySpec.Builder{
        val spec = getProperty(nameField, typeClass).addModifiers(modifiers)
        spec.initializer(format, args)
        return spec
    }
}