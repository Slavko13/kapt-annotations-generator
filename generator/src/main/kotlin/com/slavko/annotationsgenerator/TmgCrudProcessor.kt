package com.slavko.annotationsgenerator

import com.google.auto.service.AutoService
import org.springframework.javapoet.JavaFile
import org.springframework.javapoet.MethodSpec
import org.springframework.javapoet.TypeSpec
import java.io.File
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement


class TmgCrudProcessor: AbstractProcessor() {



    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
    }

    override fun process(annotations: Set<TypeElement?>?, roundEnv: RoundEnvironment): Boolean {
        for (element in roundEnv.getElementsAnnotatedWith(TmgCrudGeneration::class.java)) {
            if (element is TypeElement) {
                val typeElement = element as TypeElement
                val packageName = processingEnv.elementUtils.getPackageOf(typeElement).toString()
                val className = typeElement.simpleName.toString()
                val tableName: String = "slavko"
                try {
                    generateModelClass(packageName, className, tableName)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        moveFiles();
        return true
    }

    @Throws(IOException::class)
    private fun generateModelClass(packageName: String, className: String, tableName: String) {
        val buildedClassName = className + "Model"
        val classBuilder: TypeSpec.Builder = TypeSpec.classBuilder(buildedClassName)
            .addModifiers(Modifier.PUBLIC)
            .addMethods(generateGettersAndSetters())
        if (!packageName.isEmpty()) {
            classBuilder.addModifiers(Modifier.PUBLIC)
        }
        val classSpec: TypeSpec = classBuilder.build()
        val javaFile: JavaFile = JavaFile.builder(packageName, classSpec)
            .addStaticImport(Generated::class.java, "*")
            .build()
        val sourceFile = processingEnv.filer.createSourceFile("$packageName.$buildedClassName")
        sourceFile.openWriter().use { writer -> writer.write(javaFile.toString()) }
        
    }

    private fun generateGettersAndSetters(): Iterable<MethodSpec?>? {
        // Implement getter and setter methods generation logic here
        // This is just a placeholder
        return setOf<MethodSpec>()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(TmgCrudGeneration::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported();
    }

    private fun moveFiles() {
        val sourceDir = File("/home/slavko/projects/wisla_rebuild/slavko/five-try/annotations-generator/database-module/build/generated/source/kapt/main/com/slavko/annotationsgenerator")
        val targetDir = File("src/main/kotlin/com/slavko")

        sourceDir.walkTopDown().forEach { sourceFile ->
            val relativePath = sourceFile.relativeTo(sourceDir)
            val targetFile = File(targetDir, relativePath.path)

            targetFile.parentFile.mkdirs()
            sourceFile.copyTo(targetFile, overwrite = true)
        }
    }
}
