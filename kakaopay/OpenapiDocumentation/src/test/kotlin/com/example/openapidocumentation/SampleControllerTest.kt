package com.example.openapidocumentation

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@ExtendWith(RestDocumentationExtension::class, SpringExtension::class)
@SpringBootTest
class SampleControllerTest {
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    internal fun setUp(context: WebApplicationContext, restDocumentation: RestDocumentationContextProvider) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
            .build()
    }

    @Test
    fun getSampleByIdTest() {
        val sampleId = "aaa"
        mockMvc.perform(
            RestDocumentationRequestBuilders
                .get("/api/v1/samples/{sampleId}", sampleId)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("sampleId").value(sampleId))
            .andExpect(jsonPath("name").value("sample-$sampleId"))
            .andDo(
                MockMvcRestDocumentationWrapper.document(
                    identifier = "sample",
                    resourceDetails = ResourceSnippetParametersBuilder()
                        .tag("Sample")
                        .description("Get a sample by id")
                        .pathParameters(
                                parameterWithName("sampleId").description("the sample id")
                        )
                        .responseFields(
                            fieldWithPath("sampleId").type(JsonFieldType.STRING).description("The sample identifier."),
                            fieldWithPath("name").type(JsonFieldType.STRING).description("The name of sample.")
                        ),
                )
            )
    }
}