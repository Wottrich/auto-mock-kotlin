package com.example.mock_processor

import com.google.auto.service.AutoService
import com.google.common.collect.ImmutableSet
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


@AutoService(Processor::class) // For registering the service
@SupportedSourceVersion(SourceVersion.RELEASE_8) // to support Java 8
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
        if (roundEnvironment == null)
            return false

        for (element in roundEnvironment.getElementsAnnotatedWith(MockModel::class.java)) {
            val mockWith = element.getAnnotation(MockModel::class.java) as MockModel
            val packageName = elementsUtils.getPackageOf(element).qualifiedName.toString()
            val mockClassName = SUFFIX + element.simpleName

            val mockType = TypeSpec.classBuilder(mockClassName)
                .addModifiers(KModifier.PUBLIC)

            if (mockWith.serializable)
                mockType.addSuperinterface(Serializable::class)

            if (element.kind == ElementKind.CLASS) {
                print("Element.kind")
                for (innerElement in element.enclosedElements) {
                    if (innerElement.kind == ElementKind.FIELD
                        && innerElement.getAnnotation(MockField::class.java) != null
                    ) {

                        val fieldWith = innerElement.getAnnotation(MockField::class.java) as MockField

                        var fieldParent: TypeMirror? = null

                        try {
                            fieldWith.type
                        } catch (mte: MirroredTypeException) {
                            print("exception")
                            fieldParent = mte.typeMirror
                        }

                        val nameField = fieldWith.attribute

                        val typeMirror = fieldParent?.asTypeName() ?: return false

                        val fieldSpec: PropertySpec.Builder = PropertySpec.builder(
                            nameField, typeMirror
                        ).addModifiers(KModifier.INTERNAL)

                        print(typeMirror.toString())

                        when (typeMirror) {
                            INT -> {
                                val valueInt = fieldWith.value.toInt()
                                fieldSpec.initializer("", valueInt)
                            }
                            DOUBLE -> {
                                val valueDouble = fieldWith.value.toDouble()
                                fieldSpec.initializer("", valueDouble)
                            }
                            else -> {
                                fieldSpec.initializer(fieldWith.value)
                            }
                        }

                        if (mockWith.serializedName) {
                            val annotationSpec = AnnotationSpec.builder(SerializedName::class)
                            annotationSpec.addMember("value", nameField)
                            fieldSpec.addAnnotation(annotationSpec.build())
                        }

                        mockType.addProperty(fieldSpec.build())
                    }
                }
            }

            val kotlinFile = FileSpec.get(packageName, mockType.build())


            val filerSourceFile = kotlinFile.toJavaFileObject()
            try {
                filerSourceFile.openWriter().use { kotlinFile.writeTo(it) }
            } catch (e: Exception) {
                try {
                    filerSourceFile.delete()
                } catch (ignored: Exception) {
                }
                throw e
            }
        }


    return true
}

override fun getSupportedAnnotationTypes(): MutableSet<String> {
    return ImmutableSet.of(MockField::class.java.canonicalName, MockModel::class.java.canonicalName)
}

override fun getSupportedSourceVersion(): SourceVersion {
    return SourceVersion.latestSupported()
}

}