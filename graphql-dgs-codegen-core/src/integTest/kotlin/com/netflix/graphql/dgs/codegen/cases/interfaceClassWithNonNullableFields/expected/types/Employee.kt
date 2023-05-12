package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithNonNullableFields.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Employee.Builder::class)
public class Employee(
    firstname: () -> String = firstnameDefault,
    lastname: () -> String? = lastnameDefault,
    company: () -> String? = companyDefault
) : Person {
    private val _firstname: () -> String = firstname

    private val _lastname: () -> String? = lastname

    private val _company: () -> String? = company

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getFirstname")
    public override val firstname: String
        get() = _firstname.invoke()

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getLastname")
    public override val lastname: String?
        get() = _lastname.invoke()

    @get:JvmName("getCompany")
    public val company: String?
        get() = _company.invoke()

    public companion object {
        private val firstnameDefault: () -> String = { throw IllegalStateException("Field `firstname` was not requested") }

        private val lastnameDefault: () -> String? = { throw IllegalStateException("Field `lastname` was not requested") }

        private val companyDefault: () -> String? = { throw IllegalStateException("Field `company` was not requested") }
    }

    @JsonPOJOBuilder
    @JsonIgnoreProperties("__typename")
    public class Builder {
        private var firstname: () -> String = firstnameDefault

        private var lastname: () -> String? = lastnameDefault

        private var company: () -> String? = companyDefault

        @JsonProperty("firstname")
        public fun withFirstname(firstname: String): Builder = this.apply {
            this.firstname = { firstname }
        }

        @JsonProperty("lastname")
        public fun withLastname(lastname: String?): Builder = this.apply {
            this.lastname = { lastname }
        }

        @JsonProperty("company")
        public fun withCompany(company: String?): Builder = this.apply {
            this.company = { company }
        }

        public fun build() = Employee(
            firstname = firstname,
            lastname = lastname,
            company = company
        )
    }
}
