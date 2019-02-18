package com.example.mock_processor

import com.google.auto.service.AutoService
import javax.annotation.processing.*
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

@AutoService(Processor::class)
class MockProcessor : AbstractProcessor() {

    private val SUFFIX = "Mock"

    private lateinit var elementsUtils: Elements
    private lateinit var filer: Filer
    private lateinit var messager: Messager

    override fun init(processingEnvironment: ProcessingEnvironment?) {
        super.init(processingEnvironment)
        if (processingEnvironment != null) {
            elementsUtils = processingEnvironment.elementUtils
            filer = processingEnvironment.filer
            messager = processingEnvironment.messager
        }
    }

    override fun process(set: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        for (element in roundEnvironment.getElementsAnnotatedWith())
    }


}