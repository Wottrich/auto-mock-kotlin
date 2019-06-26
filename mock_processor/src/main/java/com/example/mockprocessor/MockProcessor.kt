package com.example.mockprocessor

import com.google.auto.service.AutoService
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.squareup.kotlinpoet.*
import wottrich.com.mock_annotations.MockField
import wottrich.com.mock_annotations.MockModel
import java.io.BufferedReader
import java.io.File
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

        ProcessorHelper.messager = messager

        for (element in roundEnvironment.getElementsAnnotatedWith(MockModel::class.java)) {
            val mockWith = element.getAnnotation(MockModel::class.java) as MockModel
            val packageName = elementsUtils.getPackageOf(element).qualifiedName.toString()

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

                    val map = Gson().fromJson(mockWith.body, Any::class.java)

                    loadBody(
                        map = map,
                        classHeader = mockType,
                        className = classType,
                        mockClassName = mockClassName,
                        packageName = packageName
                    )

                } else if (mockWith.archive.isNotEmpty()) {

                    val buffer: BufferedReader = File( "/Users/wottrich/Desktop/Gits/auto-mock-kotlin/mock_processor/src/main/java/com/example/mockprocessor/simulation.json").bufferedReader()
                    val inputString = buffer.use { it.readText() }

                    val map = Gson().fromJson(inputString, Any::class.java)

                    loadBody(
                        map = map,
                        classHeader = mockType,
                        className = classType,
                        mockClassName = mockClassName,
                        packageName = packageName
                    )

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

    private fun loadBody (map: Any, classHeader: TypeSpec.Builder, className: ClassName, mockClassName: String, packageName: String) {

        if (map is Map<*, *>) {
            val mapper = map as Map<String, Any>

            messager.printMessage(Diagnostic.Kind.WARNING, "Init")
            ProcessorHelper.loadGson(
                typeSpec = classHeader,
                className = className,
                nameClass = mockClassName,
                packageName = packageName,
                map = mapper,
                filer = filer,
                messager = messager
            )

        } else if (map is List<*>) {
            val list = map as List<Map<String, Any>>

            val name = "${mockClassName}List"
            val listClass = TypeSpec.classBuilder(name)

            ProcessorHelper.loadListGson(listClass, name, packageName, list, filer).apply {
                classHeader.addProperty(this)
            }

        }

    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return mutableSetOf(MockField::class.java.canonicalName, MockModel::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

}