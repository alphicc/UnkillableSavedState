package com.state_view_model_processor

class TypeConverter(private val javaType: String) {

    fun convertToKotlinType(): String =
        when (javaType) {
            "java.lang.Object" -> "kotlin.Any"
            "java.lang.Cloneable" -> "kotlin.Cloneable"
            "java.lang.Comparable" -> "kotlin.Comparable"
            "java.lang.Enum" -> "kotlin.Enum"
            "java.lang.Annotation" -> "kotlin.Annotation"
            "java.lang.CharSequence" -> "kotlin.CharSequence"
            "java.lang.String" -> "kotlin.String"
            "java.lang.Number" -> "kotlin.Number"
            "java.lang.Throwable" -> "kotlin.Throwable"
            "java.lang.Byte" -> "kotlin.Byte"
            "java.lang.Short" -> "kotlin.Short"
            "java.lang.Integer" -> "kotlin.Int"
            "java.lang.Long" -> "kotlin.Long"
            "java.lang.Character" -> "kotlin.Char"
            "java.lang.Float" -> "kotlin.Float"
            "java.lang.Double" -> "kotlin.Double"
            "java.lang.Boolean" -> "kotlin.Boolean"
            else -> javaType
        }
}