package com.example.mockprocessor

import com.google.auto.service.AutoService
import com.google.gson.annotations.SerializedName
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
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
import javax.tools.StandardLocation

@AutoService(Processor::class) // For registering the service
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions("kapt.kotlin.generated")
open class MockProcessor : AbstractProcessor() {

    private val suffix = "Mock"

    private lateinit var elementsUtils: Elements
    private lateinit var filer: Filer
    private lateinit var messager: Messager


    private val list = ClassName("kotlin.collections", "List")
    private val arrayList = ClassName("kotlin.collections", "ArrayList")

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

            //feature name class
            val mockClassName = suffix + element.simpleName

            val mockType = TypeSpec.classBuilder(mockClassName)

            if (mockWith.serializable)
                mockType.addSuperinterface(Serializable::class)

            if (element.kind == ElementKind.CLASS) {
                for (innerElement in element.enclosedElements) {
                    if (innerElement.kind == ElementKind.FIELD
                        && innerElement.getAnnotation(MockField::class.java) != null) {

                        val fieldWith = innerElement.getAnnotation(MockField::class.java) as MockField

                        var fieldParent: TypeMirror? = null

                        try {
                            fieldWith.type
                        } catch (mte: MirroredTypeException) {
                            messager.printMessage(Diagnostic.Kind.WARNING, "exception")
                            fieldParent = mte.typeMirror
                        }

                        val nameField = fieldWith.attribute

                        val typeMirror = fieldParent?.asTypeName() ?: return false

                        var fieldSpec: PropertySpec.Builder

                        val spec = InternalProperty()
                        spec.nameField = nameField
                        spec.modifiers = KModifier.INTERNAL

                        when (typeMirror) {
                            INT -> {
                                val valueInt = fieldWith.value.toInt()
                                spec.typeClass = Int::class
                                spec.format = "%L"
                                spec.args = valueInt
                            }
                            DOUBLE -> {
                                val valueDouble = fieldWith.value.toDouble()
                                spec.typeClass = Double::class
                                spec.format = "%L"
                                spec.args = valueDouble
                            }
                            else -> {
                                spec.typeClass = String::class
                                spec.format = "%S"
                                spec.args = fieldWith.value
                            }
                        }

                        fieldSpec = spec.returnProperty()

                        if (mockWith.serializedName) {
                            val annotationSpec = AnnotationSpec.builder(SerializedName::class)
                            annotationSpec.addMember("value", nameField)
                            fieldSpec.addAnnotation(annotationSpec.build())
                        }

                        mockType.addProperty(fieldSpec.build())
                    }
                }
                val mockTypeBuilder = mockType.build()

                createFile(packageName, mockClassName, mockTypeBuilder)

                if (mockWith.list) {
                    val mockTypeList = TypeSpec.classBuilder(mockClassName + "List")
                    val classType = ClassName(packageName, mockClassName)

                    val listOfClassType = list.parameterizedBy(classType)
                    val arrayListOfClassType = arrayList.parameterizedBy(classType)

                    val funList = FunSpec.builder("get${mockClassName}List")
                        .returns(listOfClassType)
                        .addStatement("val result = %T()", arrayListOfClassType)

                    for (i in 0..mockWith.listSize) {
                        funList.addStatement("result += %T()", classType)
                    }

                    funList.addStatement("return result")

                    mockTypeList.addFunction(funList.build())

                    createFile(packageName, mockClassName+"List", mockTypeList.build())
                }
            }
        }

        return true
    }

    private fun createFile(packageName: String, className: String, builder: TypeSpec) {
        val kotlinFile = FileSpec.builder(packageName, className).addType(builder).build()

        val kotlinFileObject = filer.createResource(StandardLocation.SOURCE_OUTPUT, packageName, "$className.kt")
        kotlinFileObject.openWriter().use { kotlinFile.writeTo(it) }
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return mutableSetOf(MockField::class.java.canonicalName, MockModel::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

}