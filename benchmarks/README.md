# DGS codegen performance benchmarks

Run a quick comparison from a clean checkout with `ruby benchmarks/run.rb --base-ref origin/master --profile quick`.
The quick profile uses two fresh JVM forks, two measured samples per fork and one second of untimed prewarm per worker JVM.
Run the full comparison with `ruby benchmarks/run.rb --base-ref origin/master --profile full`.
The full profile includes published `8.8.0` artifacts, four fresh JVM forks, six warmups, ten measured samples and five seconds of untimed prewarm per worker JVM.
Use `--suite generation`, `--suite serialization`, `--suite merge` or `--suite gradle` to run one workload family.
Use `--output-dir PATH` to select a new result directory.
Use `--type-count`, `--field-count`, `--docs`, `--seed`, `--writers`, `--heap-min`, `--heap-max`, `--collector` and `--prewarm-seconds` to configure a run.
Variant order uses a seeded shuffle of balanced baseline/candidate orders for each workload, with a randomized release position.

The runner builds the candidate and merge-base commits in separate Git worktrees and resolves the release from Maven Central.
Each variant uses its own core classes and dependency set, and the result metadata records the source commit and every jar digest.
The benchmark worker measures direct Java and both Kotlin generators with and without file writes, client serialization, public result merging and the real Gradle `generateJava` task.
The public result merge workloads cover unique names, identical duplicates, different content at the same name and the `A, B, A` destination case.
The synthetic schema varies type and field counts and includes Date and LocalDate common scalar fields.
Sparse and dense documentation includes markup, comment terminators and escaped Unicode text.
Worker allocation counts include the worker main thread and exclude file writer threads.
Gradle reports task execution time and full invocation time separately.

Candidate and baseline path-and-content manifests must match for every workload by default.
Use `--accept-output-change` to compare intentional behavior changes while retaining all differences in `output-changes.json` and the report.
Output drift retains the timing report and summary even when it fails the run.
Changed output can change the amount of work, so its timing differences cannot be attributed solely to generator efficiency.
Release output is recorded separately because `8.8.0` predates the JavaDoc escaping and multi-query variable fixes.
Timing reports use the median of paired-fork percentage changes and publish every fork's change.
Pooled medians describe absolute timings only and are not used to calculate changes.
No regression or improvement labels are assigned until same-artifact calibration supports classification across independent JVM forks.
Missing samples, worker failures, wrong class origins, empty output and unaccepted candidate output drift fail the run.
Raw samples, worker logs, exact commands, jar identities, manifests and the report are written under the selected result directory.

The pull request workflow runs the quick profile on an unprivileged `pull_request` event with read-only repository permissions.
The weekly and manual workflows run the full profile and include release `8.8.0` by default.
Weekly comparisons use the previous successful scheduled run's commit, falling back to source tag `v8.8.0` for the first run, and accept output changes explicitly.
Manual runs can set `accept_output_change` for intentional drift.
Concurrent PR runs use separate concurrency groups; a new push cancels the previous run for that PR.
