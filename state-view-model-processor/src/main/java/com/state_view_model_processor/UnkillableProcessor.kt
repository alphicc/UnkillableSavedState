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
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
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
            .superclass(ClassName("com.stateViewModel", "EmptyState"))
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

    private fun getGenericTypeString(element: TypeMirror): String {
        val fullType = element.asTypeName().toString()
        return if (isGeneric(fullType)) {
            val pack = processingEnv.typeUtils.erasure(element)
            //val rawType = fullType.substring(pack.toString().length, fullType.length)
            val generic = StringBuilder()
            generic.append("<")
            (element as DeclaredType).typeArguments.forEachIndexed { index, typeMirror ->
                //val typeElement =
                //    processingEnv.elementUtils.getTypeElement(rawType.substring(1, rawType.length - 1))
                //"$pack<${getGenericTypeString(typeElement)}>"
                //val elPack = processingEnv.typeUtils.erasure(el.asType())
                if (index > 0) generic.append("?, ${getGenericTypeString(typeMirror)}")
                else generic.append(getGenericTypeString(typeMirror))
                // processingEnv.messager.printMessage(
                //     Diagnostic.Kind.ERROR,
                //     "1 chee ${it.asTypeName().toString()}"
                // )
            }
            generic.append("?>")
            //val typeElement =
            //    processingEnv.elementUtils.getTypeElement(rawType.substring(1, rawType.length - 1))
            "$pack$generic"
        } else processingEnv.typeUtils.asElement(element).simpleName.toString()
    }

    private fun getGenericTypeClass(fullType: String): TypeName {
        return if (isGeneric(fullType)) {
            val index = fullType.indexOf("<") + 1

            val newType = fullType.substring(index, fullType.length - 1)

            val parentClass = fullType.substring(0, index - 1)
            //ClassName("", parentClass).parameterizedBy(getGenericTypeClass(newType))
            /*processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "parentClass $parentClass"
            )*/
            ClassName("", parentClass).parameterizedBy(getGenericTypeClass(newType))
        } else ClassName("", fullType)
    }

    private fun getGenericTypeClassName(element: TypeMirror): TypeName {
        val typeName = element.asTypeName()
        val fullType = typeName.toString()
        return if (isGeneric(fullType)) {
            val pack = processingEnv.typeUtils.erasure(element)
            //val rawType = fullType.substring(pack.toString().length, fullType.length)
            val parentClass = ClassName("", pack.toString())
            //val generic = StringBuilder()
            //generic.append("<")
            val parameterElements = ArrayList<TypeName>()
            (element as DeclaredType).typeArguments.forEachIndexed { index, typeMirror ->
                //val type  = ClassName("", typeMirror.toString())
                //(typeMirror as DeclaredType).typeArguments.forEach {
                //    processingEnv.messager.printMessage(
                //        Diagnostic.Kind.ERROR,
                //        "it ${it.asTypeName().toString()}"
                //    )
                //}
                val result = getGenericTypeClassName(typeMirror)
                parameterElements.add(result)

                //val typeElement =
                //    processingEnv.elementUtils.getTypeElement(rawType.substring(1, rawType.length - 1))
                //"$pack<${getGenericTypeString(typeElement)}>"
                //val elPack = processingEnv.typeUtils.erasure(el.asType())
            }
            //parseGeneric()
            parentClass.parameterizedBy(parameterElements).copy(true)
            //processingEnv.messager.printMessage(
            //    Diagnostic.Kind.ERROR,
            //    "el ${parentClass.toString()}"
            //)
            // parentClass
            // generic.append("?>")
            //val typeElement =
            //    processingEnv.elementUtils.getTypeElement(rawType.substring(1, rawType.length - 1))
        } else typeName.copy(true)
    }

    private fun getInitFunction(elements: List<Element>): CodeBlock {
        val builder = CodeBlock.builder()
        elements.forEach {
            if (it.kind == ElementKind.FIELD) {
                val fullType = it.asType().asTypeName().toString()
                val name = it.simpleName.toString().capitalize()
                //val result = getGenericTypeString(it)
                //processingEnv.messager.printMessage(
                //    Diagnostic.Kind.ERROR,
                //    "result ${result} \n"
                //)
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
                                val parameter = getGenericTypeString(typeArguments.first())
                                builder.add(
                                    "this.${"${it.simpleName}"} = savedStateHandle?.getLiveData<$parameter>(\"${name}Key\")\n"
                                )
                            }
                            else -> {
                                processingEnv.messager.printMessage(
                                    Diagnostic.Kind.ERROR,
                                    "Invalid LiveData"
                                )
                            }
                        }

                        //if ((it as DeclaredType).typeArguments.isNotEmpty()) {
                        //} else {
                        //    processingEnv.messager.printMessage(
                        //        Diagnostic.Kind.ERROR,
                        //        "Invalid LiveData generic parameter"
                        //    )
                        //}
            //
                        //(it as DeclaredType).typeArguments
                        //it.asType().annotationMirrors
                        //builder.add(
                        //    "this.${"${it.simpleName}"} = savedStateHandle?.getLiveData<${newType}>(\"${name}Key\")\n"
                        //)
                    }
                    isGeneric(fullType) -> {
                        builder.add(
                            "this.${"${it.simpleName}"} = savedStateHandle?.get<${
                                getGenericTypeString(
                                    it.asType()
                                )
                            }>(\"${name}Key\")\n"
                        )
                    }
                    else -> {
                        builder.add(
                            "this.${"${it.simpleName}"} = savedStateHandle?.get<${fullType}?>(\"${name}Key\")\n"
                        )
                    }
                }
            }
        }
        return builder.build()
    }

    private fun parseGeneric(type: String): String {
        if (isGeneric(type)) {
            val genericTypeStart = type.indexOf("<")
            val genericTypeEnd = type.indexOf(">")
            val genericType = type.substring(0, genericTypeStart)
            val genericParameter = type.substring(genericTypeStart + 1, genericTypeEnd)
            return "${genericType}<${parseGeneric(genericParameter)}?>?"
        }
        return type
    }

    private fun getLiveDataPostUpdateFunction(element: Element): FunSpec {
        //val fullType = element.asType().asTypeName().toString()
        //val genericTypeStart = fullType.indexOf("<")
        //val genericTypeEnd = fullType.indexOf(">")
        //val genericType =
        //    ClassName("", fullType.substring(genericTypeStart + 1, genericTypeEnd)).copy(
        //        true
        //    )
        //return FunSpec.builder("postUpdate${element.simpleName.toString().capitalize()}Value")
        //    .addParameter("value", genericType)
        //    .addStatement("this.${element.simpleName}?.postValue(value)")
        //    .build()

        val funBuilder = FunSpec.builder("postUpdate${element.simpleName.toString().capitalize()}Value")
        //val funBuilder = FunSpec.builder("update${element.simpleName.toString().capitalize()}Value")
        val typeArguments = (element.asType() as DeclaredType).typeArguments
        when {
            typeArguments.size > 1 -> {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Invalid LiveData generic parameter"
                )
            }
            typeArguments.size == 1 -> {
                // val parameter = getGenericTypeString(typeArguments.first())
                // builder.add(
                //     "this.${"${it.simpleName}"} = savedStateHandle?.getLiveData<$parameter>(\"${name}Key\")\n"
                // )
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
        //val fullType = element.asType().asTypeName().toString()
        //val genericTypeStart = fullType.indexOf("<")
        //val genericTypeEnd = fullType.indexOf(">")
        //val genericType =
        //    ClassName("", fullType.substring(genericTypeStart + 1, genericTypeEnd)).copy(
        //        true
        //    )

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
                // val parameter = getGenericTypeString(typeArguments.first())
                // builder.add(
                //     "this.${"${it.simpleName}"} = savedStateHandle?.getLiveData<$parameter>(\"${name}Key\")\n"
                // )
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
                //val pack = processingEnv.typeUtils.erasure(element.asType())
                //val genericTypeStart = fullType.indexOf("<")
                //val genericTypeEnd = fullType.indexOf(">")
                //val genericType =
                //    ClassName("", fullType.substring(genericTypeStart + 1, genericTypeEnd)).copy(
                //        true
                //    )
                //ClassName("androidx.lifecycle", "MutableLiveData").parameterizedBy(genericType)
                //    .copy(true)
                getGenericTypeClassName(element.asType()).copy(true)
            }
            isGeneric(fullType) -> {
                //val genericTypeStart = fullType.indexOf("<")
                //val genericTypeEnd = fullType.indexOf(">")
                //val genericParameter =
                //    ClassName("", fullType.substring(genericTypeStart + 1, genericTypeEnd)).copy(
                //        true
                //    )
                //val genericType = fullType.substring(0, genericTypeStart)
                //ClassName("", genericType).parameterizedBy(genericParameter).copy(true)
                //getGenericTypeClass(element.asType().asTypeName().toString())
                getGenericTypeClassName(element.asType()).copy(true)
                //getGenericTypeClass(element.asType().asTypeName().toString())
            }
            else -> getGenericTypeClassName(element.asType()).copy(true)
        }

        val variableName = "${element.simpleName}"
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "val $variableName / $className")
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