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

package com.netflix.graphql.dgs.client.codegen.exampleprojectionnext

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNodeNext
import java.util.*

class EntitiesV2MovieKeyProjection(
    parent: EntitiesProjectionRootNext,
    root: EntitiesProjectionRootNext,
    schemaType: Optional<String>
) : BaseSubProjectionNodeNext<EntitiesV2MovieKeyProjection, EntitiesProjectionRootNext, EntitiesProjectionRootNext>(
    parent,
    root,
    schemaType = schemaType
) {

    fun moveId(): EntitiesV2MovieKeyProjection {
        fields["moveId"] = null
        return this
    }

    fun title(): EntitiesV2MovieKeyProjection {
        fields["title"] = null
        return this
    }

    fun releaseYear(): EntitiesV2MovieKeyProjection {
        fields["releaseYear"] = null
        return this
    }

    fun reviews(username: String, score: Int): Movies_ReviewsProjection {
        val projection = Movies_ReviewsProjection(this, root)
        fields["reviews"] = projection
        inputArguments.computeIfAbsent("reviews") { mutableListOf() }
        (inputArguments["reviews"] as MutableList).add(InputArgument("username", username))
        (inputArguments["reviews"] as MutableList).add(InputArgument("score", score))
        return projection
    }

    init {
        fields["__typename"] = null
    }
}
