package com.netflix.graphql.dgs.codegen.cases.dataClassWithInterfaceInheritance.expected.types

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
@JsonDeserialize(builder = Talent.Builder::class)
public class Talent(
    firstname: () -> String = firstnameDefault,
    lastname: () -> String? = lastnameDefault,
    company: () -> String? = companyDefault,
    imdbProfile: () -> String? = imdbProfileDefault
) : Employee {
    private val _firstname: () -> String = firstname

    private val _lastname: () -> String? = lastname

    private val _company: () -> String? = company

    private val _imdbProfile: () -> String? = imdbProfile

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getFirstname")
    public override val firstname: String
        get() = _firstname.invoke()

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getLastname")
    public override val lastname: String?
        get() = _lastname.invoke()

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getCompany")
    public override val company: String?
        get() = _company.invoke()

    @get:JvmName("getImdbProfile")
    public val imdbProfile: String?
        get() = _imdbProfile.invoke()

    public companion object {
        private val firstnameDefault: () -> String = { throw IllegalStateException("Field `firstname` was not requested") }

        private val lastnameDefault: () -> String? = { throw IllegalStateException("Field `lastname` was not requested") }

        private val companyDefault: () -> String? = { throw IllegalStateException("Field `company` was not requested") }

        private val imdbProfileDefault: () -> String? = { throw IllegalStateException("Field `imdbProfile` was not requested") }
    }

    @JsonPOJOBuilder
    @JsonIgnoreProperties("__typename")
    public class Builder {
        private var firstname: () -> String = firstnameDefault

        private var lastname: () -> String? = lastnameDefault

        private var company: () -> String? = companyDefault

        private var imdbProfile: () -> String? = imdbProfileDefault

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

        @JsonProperty("imdbProfile")
        public fun withImdbProfile(imdbProfile: String?): Builder = this.apply {
            this.imdbProfile = { imdbProfile }
        }

        public fun build() = Talent(
            firstname = firstname,
            lastname = lastname,
            company = company,
            imdbProfile = imdbProfile
        )
    }
}
