/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.language.nativeplatform.internal.incremental.sourceparser

import com.google.common.collect.ImmutableList
import org.gradle.internal.serialize.SerializerSpec
import org.gradle.language.nativeplatform.internal.IncludeType

class IncludeDirectivesSerializerTest extends SerializerSpec {
    def "serializes empty directives"() {
        def directives = new DefaultIncludeDirectives(ImmutableList.of(), ImmutableList.of(), ImmutableList.of())

        expect:
        serialize(directives, new IncludeDirectivesSerializer()) == directives
    }

    def "serializes include directives"() {
        def include1 = new IncludeWithSimpleExpression("one.h", true, IncludeType.QUOTED)
        def include2 = new IncludeWithSimpleExpression("two.h", true, IncludeType.SYSTEM)
        def include3 = new IncludeWithSimpleExpression("three.h", false, IncludeType.MACRO)
        def include4 = new IncludeWithMacroFunctionCallExpression("A", true, ImmutableList.of(new SimpleExpression("X", IncludeType.MACRO), new SimpleExpression("Y", IncludeType.MACRO)))
        def directives = new DefaultIncludeDirectives(ImmutableList.copyOf([include1, include2, include3, include4]), ImmutableList.of(), ImmutableList.of())

        expect:
        serialize(directives, new IncludeDirectivesSerializer()) == directives
    }

    def "serializes macro directives"() {
        def macro1 = new MacroWithSimpleExpression("ONE", IncludeType.QUOTED,"one")
        def macro2 = new MacroWithSimpleExpression("TWO", IncludeType.MACRO, "two")
        def macro3 = new MacroWithMacroFunctionCallExpression("THREE", "abc", [])
        def macro4 = new MacroWithMacroFunctionCallExpression("FOUR", "abc", [new SimpleExpression("abc.h", IncludeType.QUOTED)])
        def macro5 = new MacroWithMacroFunctionCallExpression("FIVE", "abc", [new MacroFunctionCallExpression("macro", [new SimpleExpression("abc.h", IncludeType.QUOTED)])])
        def macro6 = new UnresolveableMacro("SIX")
        def macro7 = new UnresolveableMacro("SEVEN")
        def directives = new DefaultIncludeDirectives(ImmutableList.of(), ImmutableList.copyOf([macro1, macro2, macro3, macro4, macro5, macro6, macro7]), ImmutableList.of())

        expect:
        serialize(directives, new IncludeDirectivesSerializer()) == directives
    }

    def "serializes macro function directives"() {
        def macro1 = new ReturnFixedValueMacroFunction("ONE", 0, IncludeType.QUOTED, "one", [])
        def macro2 = new ReturnFixedValueMacroFunction("TWO", 3, IncludeType.MACRO, "two", [new SimpleExpression("abc", IncludeType.MACRO)])
        def macro3 = new ReturnParameterMacroFunction("THREE", 12, 4)
        def macro4 = new ArgsMappingMacroFunction("FOUR", 3, [0, 1, 2] as int[], "macro", [new SimpleExpression("abc.h", IncludeType.QUOTED)])
        def macro5 = new UnresolveableMacroFunction("FIVE", 3)
        def macro6 = new UnresolveableMacroFunction("SIX", 3)
        def directives = new DefaultIncludeDirectives(ImmutableList.of(), ImmutableList.of(), ImmutableList.copyOf([macro1, macro2, macro3, macro4, macro5, macro6]))

        expect:
        serialize(directives, new IncludeDirectivesSerializer()) == directives
    }
}
