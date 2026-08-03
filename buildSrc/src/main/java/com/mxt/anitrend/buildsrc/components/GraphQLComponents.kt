package com.mxt.anitrend.buildsrc.components

import co.anitrend.retrofit.graphql.codegen.RetrofitGraphQLExtension
import co.anitrend.retrofit.graphql.codegen.config.SerializationBackend
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

private const val GENERATED_GRAPHQL_PACKAGE = "com.mxt.anitrend.graphql.generated"

internal fun Project.configureGraphQLCodegen() {
    extensions.configure<RetrofitGraphQLExtension>("retrofitGraphQL") {
        packageName.set(GENERATED_GRAPHQL_PACKAGE)
        generateVariables.set(true)
        common.generateResponses.set(true)
        common.serializationBackend.set(SerializationBackend.KOTLINX)
        schema.set(file("src/main/graphql/schema.graphql"))
        operations.from(
            fileTree("src/main/graphql") {
                include("**/*.graphql")
            },
        )

        scalars {
            map("CountryCode", "kotlin.String")
            map("FuzzyDateInt", "kotlin.Int")
            map("Json", "kotlinx.serialization.json.JsonElement")
        }
    }
}
