package com.example.mockprocessor

import com.google.auto.service.AutoService
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.squareup.kotlinpoet.*
import wottrich.com.mock_annotations.MockField
import wottrich.com.mock_annotations.MockModel
import java.io.Serializable
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.tools.Diagnostic
import javax.annotation.processing.ProcessingEnvironment

@AutoService(Processor::class) // For registering the service
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions("kapt.kotlin.generated")
open class MockProcessor : AbstractProcessor() {

    private val suffix = "Mock"

    private lateinit var elementsUtils: Elements
    private lateinit var filer: Filer
    private lateinit var messager: Messager

    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment?) {
        super.init(processingEnvironment)
        if (processingEnvironment != null) {
            elementsUtils = processingEnvironment.elementUtils
            filer = processingEnvironment.filer
            messager = processingEnvironment.messager
        }
    }


    override fun process(set: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        if (roundEnvironment == null)
            return false

        for (element in roundEnvironment.getElementsAnnotatedWith(MockModel::class.java)) {
            val mockWith = element.getAnnotation(MockModel::class.java) as MockModel
            val packageName = elementsUtils.getPackageOf(element).qualifiedName.toString()

            ProcessorHelper

            //feature name class
            val mockClassName =
                if (mockWith.customName.isEmpty()) {
                    suffix + element.simpleName
                } else {
                    mockWith.customName
                }

            val classType = ClassName(packageName, mockClassName)

            val mockType = TypeSpec.objectBuilder(mockClassName)

            if (mockWith.serializable)
                mockType.addSuperinterface(Serializable::class)

            if (element.kind == ElementKind.CLASS) {
                if (mockWith.body.isNotEmpty()) {

                    if (mockWith.body.substring(0, 1) != "[") {

                        //<editor-folder defaultstate="Collapsed" desc="Body Json">
                        val map = Gson().fromJson(mockWith.body, Map::class.java) as Map<String, Any>

                        for ((key, value) in map) {

                            val typeClass = ProcessorHelper.typeClass(value = value)
                            val initializer = ProcessorHelper.format(value = value)

                            ProcessorHelper.createField(key, typeClass, value, initializer).apply {
                                mockType.addProperty(this)
                            }

                        }

                        if (mockWith.list) {
                            ProcessorHelper.createMutableListField(classType, mockWith.listSize).apply {
                                mockType.addProperty(this)
                            }
                        }
                        //</editor-folder>

                    } else {

                        val map = Gson().fromJson(mockWith.body, List::class.java) as List<Map<String, Any>>

                        val name = "${mockClassName}List"
                        val classListType = ClassName(packageName, name)
                        var listClass = TypeSpec.classBuilder(name)
                        var lastKey = ""
                        val constructor = FunSpec.constructorBuilder()

                        var listCodeBlock: MutableList<CodeBlock>

                        ProcessorHelper.loadListGson(name, map, filer).apply {
                            listClass = this
                        }

                        for ((index, mapper) in map.withIndex()) {

                            val parameters: MutableList<Any> = mutableListOf()

                            for ((key, value) in mapper) {
                                parameters.add(value)

                                if (lastKey != key && index == 0) {
                                    lastKey = key

                                    val typeClass = ProcessorHelper.typeClass(value = value)
                                    ProcessorHelper.createParameter(key, typeClass).apply {
                                        constructor.addParameter(this)
                                    }
                                }

                            }

                            ProcessorHelper.createMultiParameters(parameters).apply {
                                //listCodeBlock.add(this)
                            }

                        }

                        listClass.let {
                            it.primaryConstructor(constructor.build())
                            it.build().let {
                                ProcessorHelper.createFile(packageName, name, it, filer)
                            }
                        }

                        ProcessorHelper.createInitializerMultiParametersWithCodeBlock(
                            classListType,
                            mutableListOf(),
                            true
                        ).apply {
                            ProcessorHelper.createMutableListWithCodeBlock(classListType, this).apply {
                                mockType.addProperty(this)
                            }
                        }

                    }

                    mockType.build().let {
                        ProcessorHelper.createFile(packageName, mockClassName, it, filer)
                    }
                } else {

                    //<editor-folder defaultstate="Collapsed" desc="MockField">
                    for (innerElement in element.enclosedElements) {
                        innerElement.getAnnotation(MockField::class.java)
                            .takeIf { innerElement.kind == ElementKind.FIELD }
                            ?.let { field ->

                                var fieldParent: TypeMirror? = null

                                try {
                                    field.type
                                } catch (mte: MirroredTypeException) {
                                    messager.printMessage(Diagnostic.Kind.WARNING, "exception")
                                    fieldParent = mte.typeMirror
                                }


                                val typeMirror = fieldParent?.asTypeName() ?: return false

                                val type = ProcessorHelper.typeClass(type = typeMirror)
                                val format = ProcessorHelper.format(type = typeMirror)

                                val property = PropertySpec.builder(field.attribute, type).apply {
                                    this.addModifiers(KModifier.PUBLIC)
                                    this.initializer(format, field.value)
                                }

                                if (mockWith.serializedName) {
                                    AnnotationSpec.builder(SerializedName::class.java).apply {
                                        this.addMember("value = %S", field.attribute)
                                        property.addAnnotation(this.build())
                                    }
                                }

                                mockType.addProperty(property.build())

                            }
                    }

                    if (mockWith.list) {
                        ProcessorHelper.createMutableListField(classType, mockWith.listSize).apply {
                            mockType.addProperty(this)
                        }
                    }

                    mockType.build().let {
                        ProcessorHelper.createFile(packageName, mockClassName, it, filer)
                    }
                    //</editor-folder>

                }
            }
        }
        return true
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return mutableSetOf(MockField::class.java.canonicalName, MockModel::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

}