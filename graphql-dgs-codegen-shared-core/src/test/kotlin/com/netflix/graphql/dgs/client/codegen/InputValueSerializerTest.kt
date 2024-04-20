/*
 * Copyright 2021 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.graphql.dgs.client.codegen

import graphql.scalars.id.UUIDScalar
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class InputValueSerializerTest {

    @Test
    fun `Serialize a complex object`() {
        val movieInput = MovieInput(
            1,
            "Some movie",
            MovieInput.Genre.ACTION,
            MovieInput.Director("The Director"),
            listOf(
                MovieInput.Actor("Actor 1", "Role 1"),
                MovieInput.Actor("Actor 2", "Role 2")
            ),
            DateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1))
        )

        val serialize = InputValueSerializer(mapOf(DateRange::class.java to DateRangeScalar())).serialize(movieInput)
        assertThat(serialize).isEqualTo("""{movieId : 1, title : "Some movie", genre : ACTION, director : {name : "The Director"}, actor : [{name : "Actor 1", roleName : "Role 1"}, {name : "Actor 2", roleName : "Role 2"}], releaseWindow : "01/01/2020-01/01/2021"}""")
    }

    @Test
    fun `List of a complex object`() {
        val movieInput = MovieInput(
            1,
            "Some movie",
            MovieInput.Genre.ACTION,
            MovieInput.Director("The Director"),
            listOf(
                MovieInput.Actor("Actor 1", "Role 1"),
                MovieInput.Actor("Actor 2", "Role 2")
            ),
            DateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1))
        )

        val serialize = InputValueSerializer(mapOf(DateRange::class.java to DateRangeScalar())).serialize(listOf(movieInput))
        assertThat(serialize).isEqualTo("""[{movieId : 1, title : "Some movie", genre : ACTION, director : {name : "The Director"}, actor : [{name : "Actor 1", roleName : "Role 1"}, {name : "Actor 2", roleName : "Role 2"}], releaseWindow : "01/01/2020-01/01/2021"}]""")
    }

    @Test
    fun `Null values should be serialized except when properties of a POJO`() {
        class ExamplePojo {
            private val movieId: String? = null
            private val movieTitle: String = "Bojack Horseman"
        }

        assertThat(InputValueSerializer(mapOf()).serialize(null)).isEqualTo("null")
        assertThat(InputValueSerializer(mapOf()).serialize(mapOf("hello" to null))).isEqualTo("{hello : null}")
        assertThat(InputValueSerializer(mapOf()).serialize(ExamplePojo())).isEqualTo("{movieTitle : \"Bojack Horseman\"}")
    }

    @Test
    fun `NullableInputValueSerializer allows null values from POJO`() {
        class ExamplePojo {
            private val movieId: String? = null
            private val movieTitle: String = "Bojack Horseman"
        }

        assertThat(NullableInputValueSerializer(mapOf()).serialize(null)).isEqualTo("null")
        assertThat(NullableInputValueSerializer(mapOf()).serialize(mapOf("hello" to null))).isEqualTo("{hello : null}")
        assertThat(NullableInputValueSerializer(mapOf()).serialize(ExamplePojo())).isEqualTo("{movieId : null, movieTitle : \"Bojack Horseman\"}")
    }

    @Test
    fun `String value`() {
        val serialize = InputValueSerializer().serialize("some string")
        assertThat(serialize).isEqualTo("\"some string\"")
    }

    @Test
    fun `String with slashes`() {
        val serialize = InputValueSerializer().serialize("some \\ \"string\"")
        assertThat(serialize).isEqualTo("\"some \\\\ \\\"string\\\"\"")
    }

    @Test
    fun `int value`() {
        val serialize = InputValueSerializer().serialize(1)
        assertThat(serialize).isEqualTo("1")
    }

    @Test
    fun `long value`() {
        val serialize = InputValueSerializer().serialize(1L)
        assertThat(serialize).isEqualTo("1")
    }

    @Test
    fun `boolean value`() {
        val serialize = InputValueSerializer().serialize(true)
        assertThat(serialize).isEqualTo("true")
    }

    @Test
    fun `double value`() {
        val serialize = InputValueSerializer().serialize(1.1)
        assertThat(serialize).isEqualTo("1.1")
    }

    @Test
    fun `Companion objects should be ignored`() {
        val serialize = InputValueSerializer().serialize(MyDataWithCompanion("some title"))
        assertThat(serialize).isEqualTo("""{title : "some title"}""")
    }

    @Test
    fun `List of Integer`() {
        val serialize = InputValueSerializer().serialize(listOf(1, 2, 3))
        assertThat(serialize).isEqualTo("[1, 2, 3]")
    }

    @Test
    fun `Base class properties should be found`() {
        val serialize = InputValueSerializer().serialize(MySubClass("DGS", 1500))
        assertThat(serialize).isEqualTo("""{stars : 1500, name : "DGS"}""")
    }

    @Test
    fun `Date without scalar`() {
        val input = WithLocalDateTime(LocalDateTime.of(2021, 5, 13, 4, 34))
        val serialize = InputValueSerializer().serialize(input)
        assertThat(serialize).isEqualTo("""{date : "2021-05-13T04:34"}""")
    }

    @Test
    fun `issue 337`() {
        val input = EvilGenre.ACTION
        val serialize = InputValueSerializer().serialize(input)
        assertThat(serialize).isEqualTo("ACTION")
    }

    @Test
    fun `UUID value`() {
        val expected = UUID.randomUUID()
        val actual = InputValueSerializer(
            mapOf(UUID::class.java to UUIDScalar.INSTANCE.coercing)
        ).serialize(expected)
        assertThat(actual).isEqualTo(""""$expected"""")
    }

    @Test
    fun `overridden properties are serialized`() {
        abstract class Base {
            val baseField: Boolean = true
            open val field: String = "default"
        }
        class QueryInput(override val field: String) : Base()
        val serialized = InputValueSerializer().serialize(QueryInput("hello"))
        assertThat(serialized).isEqualTo("""{field : "hello", baseField : true}""")
    }

    @Test
    fun `properties annotated with @Transient should not be included`() {
        data class QueryInput(val visible: String) {
            @Transient
            val notVisible = "do not serialize"
        }
        val serialized = InputValueSerializer().serialize(QueryInput("serialize me"))
        assertThat(serialized).isEqualTo("""{visible : "serialize me"}""")
    }

    enum class EvilGenre {
        ACTION;
        override fun toString(): String {
            return "Genre[$name]"
        }
    }
    data class MyDataWithCompanion(val title: String) {
        companion object
    }

    open class MyBaseClass(private val name: String)

    class MySubClass(name: String, val stars: Int) : MyBaseClass(name)

    class WithLocalDateTime(val date: LocalDateTime)
}
