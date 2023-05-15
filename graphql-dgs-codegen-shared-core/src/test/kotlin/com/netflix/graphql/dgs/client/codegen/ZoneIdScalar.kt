/*
 *
 *  Copyright 2020 Netflix, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.netflix.graphql.dgs.client.codegen

import com.netflix.graphql.dgs.DgsScalar
import graphql.language.StringValue
import graphql.language.Value
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import java.time.ZoneId
import java.time.format.DateTimeParseException
import java.time.zone.ZoneRulesException

@DgsScalar(name = "ZoneId")
class ZoneIdScalar : Coercing<ZoneId, String> {
    @Throws(CoercingSerializeException::class)
    override fun serialize(dataFetcherResult: Any): String {
        return dataFetcherResult as? String
            ?: if (dataFetcherResult is ZoneId) {
                dataFetcherResult.id
            } else {
                throw CoercingSerializeException("Expected type 'ZoneId', but was " + dataFetcherResult.javaClass.name)
            }
    }

    @Throws(CoercingParseValueException::class)
    override fun parseValue(input: Any): ZoneId {
        return try {
            if (input is String) {
                ZoneId.of(input)
            } else {
                throw CoercingParseValueException("Expected a String")
            }
        } catch (e: DateTimeParseException) {
            throw CoercingParseValueException(
                String.format(
                    "A valid ZoneId must be provided. I.e. 'Europe/Berlin' or 'UTC'. Was: '%s'.",
                    input
                ),
                e
            )
        } catch (e: ZoneRulesException) {
            throw CoercingParseValueException(
                String.format(
                    "A valid ZoneId must be provided. I.e. 'Europe/Berlin' or 'UTC'. Was: '%s'.",
                    input
                ),
                e
            )
        }
    }

    @Throws(CoercingParseLiteralException::class)
    override fun parseLiteral(input: Any): ZoneId {
        return if (input is StringValue) {
            try {
                ZoneId.of(input.getValue())
            } catch (e: DateTimeParseException) {
                throw CoercingParseValueException(
                    String.format(
                        "A valid ZoneId must be provided. I.e. 'Europe/Berlin' or 'UTC'. Was: '%s'.",
                        input
                    ),
                    e
                )
            } catch (e: ZoneRulesException) {
                throw CoercingParseValueException(
                    String.format(
                        "A valid ZoneId must be provided. I.e. 'Europe/Berlin' or 'UTC'. Was: '%s'.",
                        input
                    ),
                    e
                )
            }
        } else {
            throw CoercingParseLiteralException("Expected a StringValue.")
        }
    }

    @Throws(CoercingParseLiteralException::class)
    override fun valueToLiteral(input: Any): Value<out Value<*>> {
        return StringValue.of(input.toString())
    }
}
