package com.state_view_model_processor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.state_view_model_annotations.Unkillable
import com.state_view_model_processor.UnkillableProcessor.Companion.KAPT_KOTLIN_GENERATED_OPTION_NAME
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME)
@Suppress("DefaultLocale")
class UnkillableProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Unkillable::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun process(
        mutableSet: MutableSet<out TypeElement>?,
        roundEnvironment: RoundEnvironment?
    ): Boolean {
        roundEnvironment?.getElementsAnnotatedWith(Unkillable::class.java)?.forEach {
            if (it.kind == ElementKind.CLASS) {
                generateCode(it)
            } else {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Only class can be annotated"
                )
                return true
            }
        }
        return false
    }

    private fun generateCode(element: Element) {
        val className = element.simpleName.toString()
        val pack = processingEnv.elementUtils.getPackageOf(element).toString()
        val fileName = "Unkillable$className"
        val fileBuilder = FileSpec.builder(pack, fileName)
        val classBuilder = TypeSpec.classBuilder(fileName)

        classBuilder
            .addProperty(getSavedStateHandleVariable())
            .addFunction(getConstructor())
        classBuilder.addInitializerBlock(getInitFunction(element.enclosedElements))

        element.enclosedElements.forEach { enclosedElement ->
            if (enclosedElement.kind == ElementKind.FIELD) {
                classBuilder.addProperty(getVariable(enclosedElement))

                val fullType = enclosedElement.asType().asTypeName().toString()
                if (isLiveDataGeneric(fullType)) {
                    classBuilder.addFunction(getLiveDataUpdateFunction(enclosedElement))
                    classBuilder.addFunction(getLiveDataPostUpdateFunction(enclosedElement))
                }
            }
        }

        val file = fileBuilder.addType(classBuilder.build()).build()
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        file.writeTo(File(kaptKotlinGeneratedDir))
    }

    private fun getConstructor(): FunSpec {
        val savedStateHandle = ClassName("androidx.lifecycle", "SavedStateHandle")
        return FunSpec.constructorBuilder()
            .addParameter("savedStateHandle", savedStateHandle)
            .addStatement("this.%N = %N", "savedStateHandle", "savedStateHandle")
            .build()
    }

    private fun getInitFunction(elements: List<Element>): CodeBlock {
        val builder = CodeBlock.builder()
        elements.forEach {
            if (it.kind == ElementKind.FIELD) {
                val fullType = it.asType().asTypeName().toString()
                val name = it.simpleName.toString().capitalize()
                if (isLiveDataGeneric(fullType)) {
                    builder.add(
                        "this.${"${it.simpleName}"} = savedStateHandle?.getLiveData(\"${name}Key\")\n"
                    )
                } else {
                    builder.add(
                        "this.${"${it.simpleName}"} = savedStateHandle?.get(\"${name}Key\")\n"
                    )
                }
            }
        }
        return builder.build()
    }

    private fun getLiveDataPostUpdateFunction(element: Element): FunSpec {
        val fullType = element.asType().asTypeName().toString()
        val genericTypeStart = fullType.indexOf("<")
        val genericTypeEnd = fullType.indexOf(">")
        val genericType =
            ClassName("", fullType.substring(genericTypeStart + 1, genericTypeEnd)).copy(
                true
            )
        return FunSpec.builder("postUpdate${element.simpleName.toString().capitalize()}Value")
            .addParameter("value", genericType)
            .addStatement("this.${element.simpleName}?.postValue(value)")
            .build()
    }

    private fun getLiveDataUpdateFunction(element: Element): FunSpec {
        val fullType = element.asType().asTypeName().toString()
        val genericTypeStart = fullType.indexOf("<")
        val genericTypeEnd = fullType.indexOf(">")
        val genericType =
            ClassName("", fullType.substring(genericTypeStart + 1, genericTypeEnd)).copy(
                true
            )
        return FunSpec.builder("update${element.simpleName.toString().capitalize()}Value")
            .addParameter("value", genericType)
            .addStatement("this.${element.simpleName}?.value = value")
            .build()
    }

    private fun getVariable(element: Element): PropertySpec {
        val fullType = element.asType().asTypeName().toString()
        val className = when {
            isLiveDataGeneric(fullType) -> {
                val genericTypeStart = fullType.indexOf("<")
                val genericTypeEnd = fullType.indexOf(">")
                val genericType =
                    ClassName("", fullType.substring(genericTypeStart + 1, genericTypeEnd)).copy(
                        true
                    )
                ClassName("androidx.lifecycle", "MutableLiveData").parameterizedBy(genericType)
                    .copy(true)
            }
            isGeneric(fullType) -> {
                val genericTypeStart = fullType.indexOf("<")
                val genericTypeEnd = fullType.indexOf(">")
                val genericParameter =
                    ClassName("", fullType.substring(genericTypeStart + 1, genericTypeEnd)).copy(
                        true
                    )
                val genericType = fullType.substring(0, genericTypeStart)
                ClassName("", genericType).parameterizedBy(genericParameter).copy(true)
            }
            else -> ClassName("", fullType).copy(true)
        }

        val variableName = "${element.simpleName}"
        val propertyBuilder = PropertySpec.builder(variableName, className)
            .mutable()
        if (!isLiveDataGeneric(fullType)) {
            propertyBuilder
                .setter(getUnkillableVariableSetter(element))
                .addModifiers(KModifier.PRIVATE)
        } else {
            propertyBuilder.setter(
                FunSpec.setterBuilder()
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
        }
        return propertyBuilder.initializer("null").build()
    }

    private fun getUnkillableVariableSetter(element: Element): FunSpec {
        return FunSpec.setterBuilder()
            .addParameter("value", Any::class)
            .addStatement(
                "savedStateHandle?.set(\"${element.simpleName.toString().capitalize()}Key\", value)"
            )
            .addStatement("field=value")
            .build()
    }

    private fun getSavedStateHandleVariable(): PropertySpec {
        val savedStateHandle =
            ClassName("androidx.lifecycle", "SavedStateHandle").copy(true)
        val variableName = "savedStateHandle"
        return PropertySpec.builder(variableName, savedStateHandle)
            .mutable()
            .addModifiers(KModifier.PRIVATE)
            .initializer("null")
            .build()
    }

    private fun isLiveDataGeneric(type: String): Boolean =
        isGeneric(type) && type.contains("LiveData")

    private fun isGeneric(type: String): Boolean =
        type.contains("<") && type.contains(">")
}