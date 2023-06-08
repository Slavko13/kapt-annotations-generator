package com.slavko.annotationsgenerator

import com.google.auto.service.AutoService
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor
import org.springframework.data.repository.CrudRepository
import org.springframework.javapoet.*
import org.springframework.stereotype.Repository
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement



class TmgCrudProcessor: AbstractProcessor() {



    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
    }

    override fun process(annotations: Set<TypeElement?>?, roundEnv: RoundEnvironment): Boolean {
        for (element in roundEnv.getElementsAnnotatedWith(TmgCrudGeneration::class.java)) {
            if (element is TypeElement) {
                val typeElement = element as TypeElement
                val packageName = processingEnv.elementUtils.getPackageOf(typeElement).toString() + ".generated"
                val className = typeElement.simpleName.toString()
                val tableName = convertToUnderscoreFormat(className)
                try {
                    generateModelClass(packageName, className, tableName, typeElement)
                    generateRepositoryInterface(packageName, className)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return true
    }

    private fun convertToUnderscoreFormat(input: String): String {
        val regex = Regex("([a-z])([A-Z0-9]+)")
        return input.replace(regex, "$1_${'$'}2").toLowerCase()
    }

    @Throws(IOException::class)
    private fun generateModelClass(packageName: String, className: String, tableName: String, typeElement: TypeElement) {

        val buildedClassName = className + "Model"
        val classBuilder: TypeSpec.Builder = TypeSpec.classBuilder(buildedClassName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Entity::class.java)
            .addAnnotation(Data::class.java)
            .addAnnotation(AllArgsConstructor::class.java)
            .addAnnotation(NoArgsConstructor::class.java)
            .addAnnotation(
                AnnotationSpec.builder(Table::class.java)
                    .addMember("name", "\"$tableName\"")
                    .build()
            )
            .addMethods(generateGettersAndSetters())
            .addField(
                FieldSpec.builder( TypeName.INT, "id", Modifier.PRIVATE)
                    .addAnnotation(Id::class.java)
                    .addAnnotation(GeneratedValue::class.java)
                    .initializer("0")
                    .build()
            )

        for (enclosedElement in typeElement.enclosedElements) {
            if (enclosedElement.kind == ElementKind.FIELD) {
                val fieldElement = enclosedElement as VariableElement
                val fieldName = fieldElement.simpleName.toString()
                val fieldType = fieldElement.asType()

                // Добавление поля в classBuilder
                classBuilder.addField(
                    FieldSpec.builder(TypeName.get(fieldType), fieldName, Modifier.PRIVATE)
                        .build()
                )
            }
        }


        if (!packageName.isEmpty()) {
            classBuilder.addModifiers(Modifier.PUBLIC)
        }
        val classSpec: TypeSpec = classBuilder.build()


        val javaFile = JavaFile.builder(packageName, classSpec)
            .addStaticImport(Generated::class.java, "*")
            .build()

        val sourceFile = processingEnv.filer.createSourceFile("$packageName.$className"+"Model")
        sourceFile.openWriter().use { writer -> writer.write(javaFile.toString()) }

    }


    private fun generateRepositoryInterface(packageName: String, modelClassName: String) {
        val repositoryClassName = modelClassName + "Repository"
        val modelClass = ClassName.get(packageName, modelClassName + "Model")

        val crudRepositoryType = ClassName.get("org.springframework.data.repository", "CrudRepository")
        val idType = ClassName.get("java.lang", "Integer")
        val parameterizedCrudRepositoryType = ParameterizedTypeName.get(crudRepositoryType, modelClass, idType)



        val interfaceBuilder: TypeSpec.Builder = TypeSpec.interfaceBuilder(repositoryClassName)
            .addModifiers(Modifier.PUBLIC)

            .addAnnotation(Repository::class.java)
            .addSuperinterface(parameterizedCrudRepositoryType)

        // Generate method signatures

        val findByIdMethod = MethodSpec.methodBuilder("findById")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(modelClass)
            .addParameter(Long::class.javaPrimitiveType, "id")
            .build()

        // Add methods to the interface
        interfaceBuilder.addMethod(findByIdMethod)

        val interfaceSpec: TypeSpec = interfaceBuilder.build()


        val javaFile = JavaFile.builder(packageName, interfaceSpec)
            .addStaticImport(Generated::class.java, "*")
            .build()

        val sourceFile = processingEnv.filer.createSourceFile("$packageName.$repositoryClassName")
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

    private fun deleteFiles(folderPath: String) {
        val folderPath: Path = Path.of(folderPath)

        Files.walk(folderPath)
            .sorted(Comparator.reverseOrder())
            .forEach { file -> Files.delete(file) }
    }
}
