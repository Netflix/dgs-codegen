/*
 *
 *  Copyright 2026 Netflix, Inc.
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

import com.netflix.graphql.dgs.codegen.CodeGen;
import com.netflix.graphql.dgs.codegen.CodeGenConfig;
import com.netflix.graphql.dgs.codegen.CodeGenResult;
import com.netflix.graphql.dgs.codegen.Language;
import com.netflix.graphql.dgs.codegen.generators.shared.DocFileSpec;
import com.netflix.graphql.dgs.client.codegen.BaseProjectionNode;
import com.netflix.graphql.dgs.client.codegen.GraphQLMultiQueryRequest;
import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeSpec;
import com.squareup.kotlinpoet.FileSpec;
import graphql.language.Document;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.OperationDefinition;
import graphql.language.TypeName;
import graphql.language.VariableDefinition;
import graphql.parser.Parser;
import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import javax.lang.model.element.Modifier;

public final class BenchmarkWorker {
    private static volatile Object sink;
    private static final com.sun.management.ThreadMXBean ALLOCATION =
        (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();

    private BenchmarkWorker() {}

    public static void main(String[] args) throws Exception {
        switch (args[0]) {
            case "fixture" -> writeFixture(args);
            case "run" -> run(args);
            default -> throw new IllegalArgumentException("Expected fixture or run command");
        }
    }

    private static void writeFixture(String[] args) throws Exception {
        int typeCount = Integer.parseInt(args[1]);
        int fieldCount = Integer.parseInt(args[2]);
        long seed = Long.parseLong(args[3]);
        boolean denseDocs = Boolean.parseBoolean(args[4]);
        Files.writeString(Path.of(args[5]), schema(typeCount, fieldCount, seed, denseDocs));
    }

    private static void run(String[] args) throws Exception {
        String mode = args[1];
        Path schemaPath = Path.of(args[2]);
        Path outputPath = Path.of(args[3]);
        int warmups = Integer.parseInt(args[4]);
        int samples = Integer.parseInt(args[5]);
        int scale = Integer.parseInt(args[6]);
        int writers = Integer.parseInt(args[7]);
        double prewarmSeconds = Double.parseDouble(args[8]);
        int operations = mode.startsWith("serialize-") ? scale : 1;
        if (!Double.isFinite(prewarmSeconds) || prewarmSeconds < 0 || prewarmSeconds > 60) {
            throw new IllegalArgumentException("Prewarm duration must be between zero and 60 seconds");
        }

        if (!ALLOCATION.isThreadAllocatedMemorySupported()) {
            throw new IllegalStateException("Thread allocation measurement is unavailable");
        }
        if (!ALLOCATION.isThreadAllocatedMemoryEnabled()) {
            ALLOCATION.setThreadAllocatedMemoryEnabled(true);
        }

        reportOrigins();
        Supplier<Object> operation = operation(mode, schemaPath, outputPath, writers, scale);
        long prewarmTargetNanos = (long) (prewarmSeconds * 1_000_000_000L);
        long prewarmActiveNanos = 0;
        int prewarmIterations = 0;
        while (prewarmActiveNanos < prewarmTargetNanos) {
            long prewarmStarted = System.nanoTime();
            Object value = operation.get();
            prewarmActiveNanos += System.nanoTime() - prewarmStarted;
            sink = value;
            prewarmIterations++;
        }
        if (mode.endsWith("-write")) {
            deleteTree(outputPath);
        }
        System.out.printf(
            Locale.ROOT,
            "BENCH_PREWARM\t%s\t%d\t%.6f%n",
            mode,
            prewarmIterations,
            prewarmActiveNanos / 1_000_000.0);
        Object finalResult = null;
        int total = warmups + samples;
        if (total == 0) {
            total = 1;
        }

        for (int index = 0; index < total; index++) {
            if (mode.endsWith("-write")) {
                deleteTree(outputPath);
            }
            long[] gcBefore = gcSnapshot();
            long allocatedBefore = allocatedBytes();
            long started = System.nanoTime();
            Object value = null;
            for (int operationIndex = 0; operationIndex < operations; operationIndex++) {
                value = operation.get();
                sink = value;
            }
            long ended = System.nanoTime();
            long allocated = allocatedBytes() - allocatedBefore;
            long elapsed = ended - started;
            long[] gcAfter = gcSnapshot();
            finalResult = value;

            if (index < warmups) {
                System.out.printf(
                    Locale.ROOT,
                    "BENCH_SAMPLE\t%s\twarmup\t%d\t%.6f\t%.0f\t%d\t%d%n",
                    mode,
                    index,
                    elapsed / 1_000_000.0 / operations,
                    (double) allocated / operations,
                    gcAfter[0] - gcBefore[0],
                    gcAfter[1] - gcBefore[1]);
            } else if (warmups + samples > 0) {
                System.out.printf(
                    Locale.ROOT,
                    "BENCH_SAMPLE\t%s\tmeasured\t%d\t%.6f\t%.0f\t%d\t%d%n",
                    mode,
                    index - warmups,
                    elapsed / 1_000_000.0 / operations,
                    (double) allocated / operations,
                    gcAfter[0] - gcBefore[0],
                    gcAfter[1] - gcBefore[1]);
            }
        }

        if (finalResult instanceof CodeGenResult result) {
            if (!mode.endsWith("-write")) {
                deleteTree(outputPath);
                render(result, outputPath);
            }
            if (mode.startsWith("merge-")) {
                verifyMergeResult(mode, result);
            }
        } else if (finalResult instanceof String serialized) {
            Files.createDirectories(outputPath);
            Files.writeString(outputPath.resolve("request.graphql"), serialized);
            if (serialized.isBlank()) {
                throw new IllegalStateException("Serialization produced empty output");
            }
        }
    }

    private static Supplier<Object> operation(
        String mode,
        Path schemaPath,
        Path outputPath,
        int writers,
        int scale) {
        if (mode.startsWith("java-") || mode.startsWith("kotlin")) {
            boolean kotlin = mode.startsWith("kotlin");
            boolean write = mode.endsWith("-write");
            CodeGenConfig config = new CodeGenConfig();
            config.setSchemaFiles(Set.of(schemaPath.toFile()));
            config.setPackageName("com.example.synthetic");
            config.setOutputDir(outputPath.resolve("sources"));
            config.setExamplesOutputDir(outputPath.resolve("examples"));
            config.setGeneratedDocsFolder(outputPath.resolve("docs"));
            config.setWriteToFiles(write);
            config.setLanguage(kotlin ? Language.KOTLIN : Language.JAVA);
            config.setGenerateKotlinNullableClasses(mode.startsWith("kotlin2"));
            config.setGenerateClientApi(true);
            config.setGenerateDocs(true);
            config.setShortProjectionNames(true);
            config.setSkipEntityQueries(true);
            config.setFileWriteParallelism(writers);
            return () -> new CodeGen(config).generate();
        }
        if (mode.startsWith("serialize-")) {
            Supplier<String> serializer = serializer(mode, scale);
            return serializer::get;
        }
        if (mode.startsWith("merge-")) {
            List<CodeGenResult> chunks = mergeChunks(mode, scale);
            Supplier<CodeGenResult> merger = () -> {
                CodeGenResult result = new CodeGenResult();
                for (CodeGenResult chunk : chunks) {
                    result = result.merge(chunk);
                }
                return result;
            };
            verifyMergeResult(mode, merger.get());
            return merger::get;
        }
        throw new IllegalArgumentException("Unknown workload: " + mode);
    }

    private static Supplier<String> serializer(String mode, int count) {
        int queryCount = mode.equals("serialize-inline-single") || mode.equals("serialize-single-variable") ? 1 : 10;
        boolean variables = !mode.startsWith("serialize-inline");
        List<GraphQLQueryRequest> requests = new ArrayList<>();
        for (int index = 0; index < queryCount; index++) {
            GraphQLQuery query = new GraphQLQuery("query", "Synthetic") {
                @Override
                public String getOperationName() {
                    return "lookup";
                }
            };
            query.setQueryAlias("item" + index);
            query.getInput().put("id", "id-" + index);
            if (variables) {
                query.getVariableReferences().put("id", "id");
                query.getVariableDefinitions().add(variableDefinition(mode, index));
            }
            BaseProjectionNode projection = new BaseProjectionNode(Optional.empty()) {};
            projection.getFields().put("id", null);
            projection.getFields().put("title", null);
            requests.add(new GraphQLQueryRequest(query, projection));
        }
        Supplier<String> serialize = queryCount == 1
            ? requests.get(0)::serialize
            : new GraphQLMultiQueryRequest(requests)::serialize;
        String first = serialize.get();
        if (first.isBlank() || !first.equals(serialize.get())) {
            throw new IllegalStateException("Repeated serialization changed output for " + mode);
        }
        return serialize;
    }

    private static VariableDefinition variableDefinition(String mode, int index) {
        if (mode.equals("serialize-default-conflict")) {
            return parseVariable("$id: ID = \"value" + index + "\"");
        }
        if (mode.equals("serialize-directive-conflict")) {
            return parseVariable(index % 2 == 0 ? "$id: ID @include(if: true)" : "$id: ID");
        }
        if (mode.equals("serialize-type-conflict")) {
            if (index % 2 == 0) {
                return newVariable("id", TypeName.newTypeName("ID").build());
            }
            return newVariable("id", ListType.newListType(TypeName.newTypeName("ID").build()).build());
        }
        if (mode.equals("serialize-multi-shared-variable") && index % 2 == 1) {
            return newVariable(
                "id",
                NonNullType.newNonNullType(TypeName.newTypeName("ID").build()).build());
        }
        return newVariable("id", TypeName.newTypeName("ID").build());
    }

    private static VariableDefinition newVariable(String name, graphql.language.Type<?> type) {
        return VariableDefinition.newVariableDefinition().name(name).type(type).build();
    }

    private static VariableDefinition parseVariable(String definition) {
        Document document = new Parser().parseDocument("query Synthetic(" + definition + ") { value }");
        return ((OperationDefinition) document.getDefinitions().get(0)).getVariableDefinitions().get(0);
    }

    private static List<CodeGenResult> mergeChunks(String mode, int count) {
        List<JavaFile> files = new ArrayList<>();
        if (mode.equals("merge-collision-a-b-a")) {
            files.add(projection("Projection", "A"));
            files.add(projection("Projection", "B"));
            files.add(projection("Projection", "A"));
        } else {
            int size = mode.equals("merge-same-name") ? Math.min(count, 2) : count;
            for (int index = 0; index < size; index++) {
                String name = mode.equals("merge-unique") ? "Projection" + index : "Projection";
                String body = mode.equals("merge-same-name") ? (index == 0 ? "A" : "B") : "A";
                files.add(projection(name, body));
            }
        }
        List<CodeGenResult> chunks = new ArrayList<>();
        for (JavaFile file : files) {
            chunks.add(projectionResult(file));
        }
        return chunks;
    }

    private static JavaFile projection(String name, String body) {
        TypeSpec type = TypeSpec.classBuilder(name)
            .addModifiers(Modifier.PUBLIC)
            .addMethod(MethodSpec.methodBuilder("body")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return $S", body)
                .build())
            .build();
        return JavaFile.builder("com.example.projection", type).build();
    }

    private static CodeGenResult projectionResult(JavaFile file) {
        return new CodeGenResult(
            List.of(), List.of(), List.of(), List.of(), List.of(), List.of(file), List.of(),
            List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private static void verifyMergeResult(String mode, CodeGenResult result) {
        List<JavaFile> files = result.getClientProjections();
        if (mode.equals("merge-collision-a-b-a")) {
            if (files.size() != 2 || !files.get(1).toString().contains("return \"B\"")) {
                throw new IllegalStateException("A, B, A projection semantics changed");
            }
        } else if (mode.equals("merge-same-name")) {
            if (files.size() != 2) {
                throw new IllegalStateException("Distinct same-name projections were collapsed");
            }
        } else if (mode.equals("merge-identical") && files.size() != 1) {
            throw new IllegalStateException("Identical projections were not deduplicated");
        }
    }

    private static void render(CodeGenResult result, Path output) throws Exception {
        Path sources = output.resolve("sources");
        Path examples = output.resolve("examples");
        Path docs = output.resolve("docs");
        writeJava(result.getJavaDataTypes(), sources);
        writeJava(result.getJavaInterfaces(), sources);
        writeJava(result.getJavaEnumTypes(), sources);
        writeJava(result.getJavaQueryTypes(), sources);
        writeJava(result.getClientProjections(), sources);
        writeJava(result.getJavaConstants(), sources);
        writeJava(result.getJavaDataFetchers(), examples);
        writeKotlin(result.getKotlinDataTypes(), sources);
        writeKotlin(result.getKotlinInputTypes(), sources);
        writeKotlin(result.getKotlinInterfaces(), sources);
        writeKotlin(result.getKotlinEnumTypes(), sources);
        writeKotlin(result.getKotlinConstants(), sources);
        writeKotlin(result.getKotlinClientTypes(), sources);
        writeKotlin(result.getKotlinDataFetchers(), examples);
        for (DocFileSpec doc : result.getDocFiles()) {
            doc.writeTo(docs);
        }
    }

    private static void writeJava(List<JavaFile> files, Path output) throws Exception {
        for (JavaFile file : files) {
            file.writeTo(output);
        }
    }

    private static void writeKotlin(List<FileSpec> files, Path output) throws Exception {
        for (FileSpec file : files) {
            file.writeTo(output);
        }
    }

    private static String schema(int typeCount, int fieldCount, long seed, boolean denseDocs) {
        if (typeCount < 2 || fieldCount < 1) {
            throw new IllegalArgumentException("Use at least two object types and one field");
        }
        StringBuilder schema = new StringBuilder(typeCount * fieldCount * 48);
        schema.append("schema { query: Query }\n");
        schema.append("scalar Date\nscalar LocalDate\n");
        schema.append("type Query {\n");
        for (int index = 0; index < typeCount; index++) {
            schema.append("  item").append(index).append(": Type").append(index).append("\n");
        }
        schema.append("}\n");
        String[] scalarTypes = {"String", "Int", "Boolean", "Float", "ID"};
        for (int type = 0; type < typeCount; type++) {
            appendDescription(schema, denseDocs || type % 8 == 0, "Type" + type, seed);
            schema.append("type Type").append(type).append(" {\n  id: ID!\n  date: Date\n  localDate: LocalDate\n");
            for (int field = 0; field < fieldCount; field++) {
                int scalar = Math.floorMod(seed + type * 31L + field * 17L, scalarTypes.length);
                if (denseDocs || (type % 8 == 0 && field == 0)) {
                    schema.append("  \"\"\"Field documentation with <i>markup</i>, */ and ")
                        .append("\\" + "u0061").append(".\"\"\"\n");
                }
                schema.append("  field").append(field).append(": ").append(scalarTypes[scalar]).append("\n");
            }
            schema.append("  related: Type").append((type + 1) % typeCount).append("\n}\n");
        }
        schema.append("enum Status { ACTIVE INACTIVE UNKNOWN }\n");
        schema.append("input Filter { text: String status: Status }\n");
        return schema.toString();
    }

    private static void appendDescription(StringBuilder schema, boolean denseDocs, String name, long seed) {
        if (!denseDocs) {
            return;
        }
        String escapedUnicode = "\\" + "u0061";
        schema.append("\"\"\"Synthetic documentation ").append(name)
            .append(" with markup <b>text</b>, comment terminator */ and escaped ")
            .append(escapedUnicode).append(" seed ").append(seed).append(".\"\"\"\n");
    }

    private static long allocatedBytes() {
        return ALLOCATION.getThreadAllocatedBytes(Thread.currentThread().getId());
    }

    private static long[] gcSnapshot() {
        long count = 0;
        long millis = 0;
        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            count += Math.max(0, bean.getCollectionCount());
            millis += Math.max(0, bean.getCollectionTime());
        }
        return new long[] {count, millis};
    }

    private static void reportOrigins() throws Exception {
        reportOrigin("core", CodeGen.class);
        reportOrigin("client", GraphQLQueryRequest.class);
        reportOrigin("graphql-java", Parser.class);
    }

    private static void reportOrigin(String name, Class<?> type) throws Exception {
        String location = new File(type.getProtectionDomain().getCodeSource().getLocation().toURI()).getName();
        System.out.println("BENCH_ORIGIN\t" + name + "\t" + location);
    }

    private static void deleteTree(Path root) throws Exception {
        if (!Files.exists(root)) {
            return;
        }
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }
}
