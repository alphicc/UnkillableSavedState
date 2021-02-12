package com.state_view_model_processor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.state_view_model_annotations.Unkillable
import com.state_view_model_processor.Processor.Companion.KAPT_KOTLIN_GENERATED_OPTION_NAME
import java.io.File
import javax.annotation.processing.*
import javax.annotation.processing.Processor
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME)
@Suppress("DefaultLocale")
class Processor : AbstractProcessor() {

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
            .superclass(ClassName("com.stateViewModel", "EmptyState"))
            .addFunction(getConstructor(element.enclosedElements))
        //classBuilder.addInitializerBlock(getInitFunction(element.enclosedElements))

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

    private fun getConstructor(elements: List<Element>): FunSpec {
        val savedStateHandle = ClassName("androidx.lifecycle", "SavedStateHandle")
        return FunSpec.constructorBuilder()
            .addParameter("savedStateHandle", savedStateHandle)
            .addStatement("this.%N = %N", "stateHandle", "savedStateHandle")
            .addCode(getInitFunction(elements))
            .build()
    }

    private fun getTypeString(element: TypeMirror): String {
        val fullType = element.asTypeName().toString()
        return if (isGeneric(fullType)) {
            val pack = processingEnv.typeUtils.erasure(element)
            val generic = StringBuilder()
            generic.append("<")
            (element as DeclaredType).typeArguments.forEachIndexed { index, typeMirror ->
                if (index > 0) generic.append("?, ${getTypeString(typeMirror)}")
                else generic.append(getTypeString(typeMirror))
            }
            generic.append("?>")
            "$pack$generic"
        } else ClassName("", TypeConverter(fullType).convertToKotlinType()).toString()
    }

    private fun getGenericTypeClassName(element: TypeMirror): TypeName {
        val typeName = element.asTypeName()
        val fullType = typeName.toString()
        return if (isGeneric(fullType)) {
            val pack = processingEnv.typeUtils.erasure(element)
            val parentClass = ClassName("", pack.toString())
            val parameterElements = ArrayList<TypeName>()
            (element as DeclaredType).typeArguments.forEachIndexed { index, typeMirror ->
                val result = getGenericTypeClassName(typeMirror)
                parameterElements.add(result)
            }
            parentClass.parameterizedBy(parameterElements).copy(true)
        } else ClassName("", TypeConverter(fullType).convertToKotlinType())
    }

    private fun getInitFunction(elements: List<Element>): CodeBlock {
        val builder = CodeBlock.builder()
        elements.forEach {
            if (it.kind == ElementKind.FIELD) {
                val fullType = it.asType().asTypeName().toString()
                val name = it.simpleName.toString().capitalize()
                when {
                    isLiveDataGeneric(fullType) -> {
                        val typeArguments = (it.asType() as DeclaredType).typeArguments
                        when {
                            typeArguments.size > 1 -> {
                                processingEnv.messager.printMessage(
                                    Diagnostic.Kind.ERROR,
                                    "Invalid LiveData generic parameter"
                                )
                            }
                            typeArguments.size == 1 -> {
                                val parameter = getTypeString(typeArguments.first())
                                val code =
                                    "this.${"${it.simpleName}"} = stateHandle?.getLiveData<$parameter>(\"${name}Key\")\n"
                                builder.add(code)
                            }
                            else -> {
                                processingEnv.messager.printMessage(
                                    Diagnostic.Kind.ERROR,
                                    "Invalid LiveData"
                                )
                            }
                        }
                    }
                    isGeneric(fullType) -> {
                        val code =
                            "this.${"${it.simpleName}"} = stateHandle?.get<${getTypeString(it.asType())}>(\"${name}Key\")\n"
                        builder.add(code)
                    }
                    else -> {
                        val code =
                            "this.${"${it.simpleName}"} = stateHandle?.get<${fullType}?>(\"${name}Key\")\n"
                        builder.add(code)
                    }
                }
            }
        }
        return builder.build()
    }

    private fun getLiveDataPostUpdateFunction(element: Element): FunSpec {
        val funBuilder =
            FunSpec.builder("postUpdate${element.simpleName.toString().capitalize()}Value")
        val typeArguments = (element.asType() as DeclaredType).typeArguments
        when {
            typeArguments.size > 1 -> {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Invalid LiveData generic parameter"
                )
            }
            typeArguments.size == 1 -> {
                funBuilder.addParameter("value", getGenericTypeClassName(typeArguments.first()))
            }
            else -> {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Invalid LiveData"
                )
            }
        }

        return funBuilder
            .addStatement("this.${element.simpleName}?.postValue(value)")
            .build()
    }

    private fun getLiveDataUpdateFunction(element: Element): FunSpec {
        val funBuilder = FunSpec.builder("update${element.simpleName.toString().capitalize()}Value")
        val typeArguments = (element.asType() as DeclaredType).typeArguments
        when {
            typeArguments.size > 1 -> {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Invalid LiveData generic parameter"
                )
            }
            typeArguments.size == 1 -> {
                funBuilder.addParameter("value", getGenericTypeClassName(typeArguments.first()))
            }
            else -> {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Invalid LiveData"
                )
            }
        }

        return funBuilder
            .addStatement("this.${element.simpleName}?.value = value")
            .build()
    }

    private fun getVariable(element: Element): PropertySpec {
        val fullType = element.asType().asTypeName().toString()
        val className = when {
            isLiveDataGeneric(fullType) -> {
                getGenericTypeClassName(element.asType()).copy(true)
            }
            isGeneric(fullType) -> {
                getGenericTypeClassName(element.asType()).copy(true)
            }
            else -> getGenericTypeClassName(element.asType()).copy(true)
        }

        val variableName = "${element.simpleName}"
        val propertyBuilder = PropertySpec.builder(variableName, className)
            .mutable()
        if (!isLiveDataGeneric(fullType)) {
            propertyBuilder
                .setter(getUnkillableVariableSetter(element))
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
        val statement =
            "stateHandle?.set(\"${element.simpleName.toString().capitalize()}Key\", value)"
        return FunSpec.setterBuilder()
            .addParameter("value", Any::class)
            .addStatement(statement)
            .addStatement("field=value")
            .build()
    }

    private fun getSavedStateHandleVariable(): PropertySpec {
        val savedStateHandle =
            ClassName("androidx.lifecycle", "SavedStateHandle").copy(true)
        val variableName = "stateHandle"
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