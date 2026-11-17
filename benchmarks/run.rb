#!/usr/bin/env ruby

require 'digest'
require 'etc'
require 'fileutils'
require 'json'
require 'open3'
require 'optparse'
require 'rbconfig'
require 'time'

$stdout.sync = true

module DgsBenchmark
  LANGUAGES = %w[java kotlin kotlin2].freeze
  SERIALIZATION_MODES = %w[
    serialize-inline-single
    serialize-inline-multi
    serialize-single-variable
    serialize-multi-shared-variable
    serialize-type-conflict
    serialize-default-conflict
    serialize-directive-conflict
  ].freeze
  MERGE_MODES = %w[merge-unique merge-identical merge-same-name merge-collision-a-b-a].freeze
  SUITES = %w[generation serialization merge gradle].freeze
  class Runner
    attr_reader :root, :output, :options, :worktrees, :artifacts, :manifests, :samples, :runs

    def initialize(argv)
      @options = parse_options(argv)
      @root = File.expand_path(options[:repo_root])
      @output = File.expand_path(options[:output_dir])
      @worktrees = {}
      @artifacts = {}
      @manifests = Hash.new { |hash, key| hash[key] = {} }
      @samples = []
      @runs = []
      @output_changes = []
      @order_random = Random.new(options[:seed])
      @schema_paths = {}
      @schema_digests = {}
      @output_initialized = false
      @worker_classes = File.join(output, 'worker-classes')
      @gradle_fixture = File.join(output, 'gradle-fixture')
    end

    def run
      validate_options
      prepare_output
      @candidate_sha = git('rev-parse', options[:candidate_sha])
      @base_sha = options[:base_sha] ? git('rev-parse', options[:base_sha]) : git('merge-base', @candidate_sha, options[:base_ref])
      @active_action = 'build benchmark variants'
      write_json('metadata.json', metadata)
      create_worktree('candidate', @candidate_sha)
      create_worktree('base', @base_sha)
      build_source_variant('base')
      build_source_variant('candidate')
      if include_release?
        resolve_release
      end
      compile_worker
      generate_schema
      run_suites
      @active_action = 'verify output manifests'
      verify_outputs
      write_results
      if @output_changes.any? && !options[:accept_output_change]
        raise "Generated output changed for #{@output_changes.map { |change| change.fetch('workload') }.uniq.join(', ')}; see report.md"
      end
      puts "Benchmark results: #{relative(output)}"
      puts "Timing report: #{relative(File.join(output, 'report.md'))}"
      0
    rescue StandardError => error
      if @output_initialized
        write_json('failure.json', {
          'candidate_commit' => @candidate_sha,
          'baseline_commit' => @base_sha,
          'action' => @active_action,
          'error' => sanitize(error.message)
        })
        persist_checkpoint
      end
      raise
    ensure
      cleanup_worktrees
      cleanup_temporary_artifacts if @output_initialized
    end

    def self.validate_prewarm(text, mode, minimum_seconds)
      marker = text.lines.find { |line| line.start_with?("BENCH_PREWARM\t") }
      raise "Missing prewarm marker for #{mode}" unless marker

      fields = marker.strip.split("\t")
      raise "Malformed prewarm marker for #{mode}" unless fields.size == 4 && fields[1] == mode
      iterations = Integer(fields[2])
      milliseconds = Float(fields[3])
      raise "Invalid prewarm result for #{mode}" unless iterations >= 0 && milliseconds.finite? && milliseconds >= 0
      raise "Prewarm duration too short for #{mode}: #{milliseconds} ms" if milliseconds < minimum_seconds * 1_000

      { 'iterations' => iterations, 'milliseconds' => milliseconds }
    end

    def self.validate_samples(rows, mode, warmups, samples)
      matching = rows.select { |row| row['mode'] == mode }
      phases = { 'warmup' => warmups, 'measured' => samples }
      phases.each do |phase, expected_count|
        phase_rows = matching.select { |row| row['phase'] == phase }
        ids = phase_rows.map { |row| row['iteration'] }
        raise "Missing or duplicate #{phase} samples for #{mode}" unless ids.sort == (0...expected_count).to_a
      end
      raise "Unexpected sample phase for #{mode}" unless matching.all? { |row| phases.key?(row['phase']) }
      raise "Non-finite or negative sample for #{mode}" unless matching.all? do |row|
        row['milliseconds'].finite? && row['milliseconds'] >= 0 && row['allocated_bytes'].finite? && row['allocated_bytes'] >= 0
      end

      matching
    end

    def self.validate_command_status(status, command)
      return if status.success?

      raise "Command failed with status #{status.exitstatus}: #{command.join(' ')}"
    end

    private

    def parse_options(argv)
      options = {
        repo_root: File.expand_path('..', __dir__),
        candidate_sha: 'HEAD',
        base_ref: 'origin/master',
        base_sha: nil,
        release_version: '8.8.0',
        include_release: nil,
        accept_output_change: false,
        profile: 'quick',
        suite: SUITES.dup,
        output_dir: nil,
        forks: nil,
        warmups: nil,
        samples: nil,
        type_count: nil,
        field_count: nil,
        seed: 8675309,
        writers: 4,
        heap_min: '512m',
        heap_max: '2g',
        collector: 'G1',
        serial_operations: nil,
        docs: 'both',
        prewarm_seconds: nil,
        java_home: ENV['JAVA_HOME'],
        help: false
      }
      parser = OptionParser.new do |flags|
        flags.banner = 'Usage: ruby benchmarks/run.rb [options]'
        flags.on('--repo-root PATH', 'Repository checkout to benchmark') { |value| options[:repo_root] = value }
        flags.on('--candidate-sha SHA', 'Candidate commit, defaults to HEAD') { |value| options[:candidate_sha] = value }
        flags.on('--base-ref REF', 'Merge-base reference, defaults to origin/master') { |value| options[:base_ref] = value }
        flags.on('--base-sha SHA', 'Explicit comparison baseline commit') { |value| options[:base_sha] = value }
        flags.on('--release-version VERSION', 'Published codegen version, defaults to 8.8.0') { |value| options[:release_version] = value }
        flags.on('--include-release BOOL', 'Include the published release variant') { |value| options[:include_release] = value == 'true' }
        flags.on('--accept-output-change', 'Report intentional baseline/candidate output changes without failing') { options[:accept_output_change] = true }
        flags.on('--profile NAME', 'quick or full') { |value| options[:profile] = value }
        flags.on('--suite LIST', 'Comma-separated subset of generation,serialization,merge,gradle') { |value| options[:suite] = value.split(',') }
        flags.on('--output-dir PATH', 'Writable result directory, defaults under build/performance') { |value| options[:output_dir] = value }
        flags.on('--forks COUNT', Integer, 'Fresh JVM forks per variant') { |value| options[:forks] = value }
        flags.on('--warmups COUNT', Integer, 'Warmup iterations in each worker JVM') { |value| options[:warmups] = value }
        flags.on('--samples COUNT', Integer, 'Measured iterations in each worker JVM') { |value| options[:samples] = value }
        flags.on('--type-count COUNT', Integer, 'Synthetic object type count') { |value| options[:type_count] = value }
        flags.on('--field-count COUNT', Integer, 'Fields per synthetic object type') { |value| options[:field_count] = value }
        flags.on('--seed VALUE', Integer, 'Deterministic schema seed') { |value| options[:seed] = value }
        flags.on('--writers COUNT', Integer, 'Parallel file writers for write workloads') { |value| options[:writers] = value }
        flags.on('--heap-min SIZE', 'Worker minimum heap, defaults to 512m') { |value| options[:heap_min] = value }
        flags.on('--heap-max SIZE', 'Worker and Gradle maximum heap, defaults to 2g') { |value| options[:heap_max] = value }
        flags.on('--collector NAME', 'G1, Parallel or Serial garbage collector') { |value| options[:collector] = value }
        flags.on('--serial-operations COUNT', Integer, 'Serializations per sample batch') { |value| options[:serial_operations] = value }
        flags.on('--docs PROFILE', 'dense, sparse or both synthetic documentation profiles') { |value| options[:docs] = value }
        flags.on('--prewarm-seconds SECONDS', Float, 'Untimed worker prewarm duration per fresh JVM') { |value| options[:prewarm_seconds] = value }
        flags.on('--java-home PATH', 'JDK used for builds and worker processes') { |value| options[:java_home] = value }
        flags.on('-h', '--help', 'Show this help') { options[:help] = true }
      end
      parser.parse!(argv)
      if options[:help]
        puts parser
        exit 0
      end
      profile = options[:profile]
      raise "Unknown profile: #{profile}" unless %w[quick full].include?(profile)

      defaults = profile == 'full' ? { forks: 4, warmups: 6, samples: 10, type_count: 80, field_count: 8, serial_operations: 10_000 } :
        { forks: 2, warmups: 1, samples: 2, type_count: 16, field_count: 4, serial_operations: 500 }
      defaults.each { |key, value| options[key] ||= value }
      options[:prewarm_seconds] ||= profile == 'full' ? 5.0 : 1.0
      options[:include_release] = profile == 'full' if options[:include_release].nil?
      options[:output_dir] ||= File.join(options[:repo_root], 'build', 'performance', "#{profile}-#{Time.now.utc.strftime('%Y%m%dT%H%M%SZ')}")
      options
    end

    def validate_options
      raise 'Unknown suite selection' unless options[:suite].all? { |suite| SUITES.include?(suite) }
      raise 'Select at least one benchmark suite' if options[:suite].empty?
      raise 'Unknown documentation profile' unless %w[dense sparse both].include?(options[:docs])
      raise 'forks, warmups, samples, writers and fixture counts must be positive' unless
        [options[:forks], options[:samples], options[:type_count], options[:field_count], options[:writers], options[:serial_operations]].all?(&:positive?) && options[:warmups] >= 0
      raise 'The benchmark runner requires a clean checkout so commit identities match the measured source' unless git('status', '--porcelain').empty?
      raise 'The release version must be an explicit numeric version' unless options[:release_version].match?(/\A\d+\.\d+\.\d+\z/)
      raise 'Heap sizes must be positive values such as 512m or 2g' unless [options[:heap_min], options[:heap_max]].all? { |size| size.match?(/\A\d+[kKmMgG]\z/) }
      raise 'Unknown garbage collector' unless %w[G1 Parallel Serial].include?(options[:collector])
      raise 'Minimum heap must not exceed maximum heap' if heap_bytes(options[:heap_min]) > heap_bytes(options[:heap_max])
      raise 'Prewarm duration must be between zero and 60 seconds' unless
        options[:prewarm_seconds].finite? && options[:prewarm_seconds].between?(0, 60)
      @java_home = File.expand_path(options[:java_home] || detect_java_home)
      raise "JDK home is missing javac: #{relative(File.join(@java_home, 'bin', 'javac'))}" unless File.executable?(File.join(@java_home, 'bin', 'javac'))
    end

    def prepare_output
      if File.exist?(output) && !Dir.empty?(output)
        raise "Output directory is not empty: #{relative(output)}"
      end
      FileUtils.mkdir_p(output)
      @output_initialized = true
      FileUtils.cp_r(File.join(root, 'benchmarks', 'gradle'), @gradle_fixture)
    end

    def metadata
      profile = options[:profile]
      {
        'format_version' => 1,
        'candidate_commit' => @candidate_sha,
        'baseline_commit' => @base_sha,
        'release_version' => options[:release_version],
        'release_included' => include_release?,
        'accept_output_change' => options[:accept_output_change],
        'profile' => profile,
        'suites' => options[:suite],
        'configuration' => {
          'forks' => options[:forks], 'warmups' => options[:warmups], 'samples' => options[:samples],
          'type_count' => options[:type_count], 'field_count' => options[:field_count], 'documentation_profiles' => documentation_profiles,
          'seed' => options[:seed], 'writers' => options[:writers],
          'heap_min' => options[:heap_min], 'heap_max' => options[:heap_max], 'collector' => options[:collector],
          'serial_operations_per_batch' => options[:serial_operations],
          'worker_prewarm_seconds' => options[:prewarm_seconds],
          'variant_order' => 'Seeded shuffle of balanced base/candidate orders within each workload; release position randomized.',
          'timing_policy' => 'Descriptive paired-fork changes only until same-artifact calibration supports classification.'
        },
        'environment' => {
          'java_version' => command_output([java_bin, '-version']).lines.map(&:strip).reject(&:empty?),
          'os' => RbConfig::CONFIG['host_os'],
          'ruby_version' => RUBY_VERSION,
          'processors' => Etc.nprocessors
        },
        'allocation_scope' => 'Current worker main thread only; writer threads and Gradle workers are excluded.'
      }
    end

    def run_suites
      modes = []
      modes.concat(generation_modes) if options[:suite].include?('generation')
      modes.concat(SERIALIZATION_MODES) if options[:suite].include?('serialization')
      modes.concat(MERGE_MODES) if options[:suite].include?('merge')
      modes.each { |mode| run_worker_mode(mode) }
      run_gradle_suite if options[:suite].include?('gradle')
    end

    def include_release?
      options[:include_release]
    end

    def documentation_profiles
      options[:docs] == 'both' ? %w[dense sparse] : [options[:docs]]
    end

    def generation_modes
      LANGUAGES.flat_map do |language|
        documentation_profiles.flat_map do |density|
          ["#{language}-#{density}-model", "#{language}-#{density}-write"]
        end
      end
    end

    def density_for(mode)
      return 'dense' if mode.include?('-dense-')
      return 'sparse' if mode.include?('-sparse-')

      documentation_profiles.first
    end

    def schema_path_for(mode)
      @schema_paths.fetch(density_for(mode))
    end

    def schema_digest_description
      @schema_digests.map { |density, digest| "#{density} `#{digest}`" }.join(' and ')
    end

    def create_worktree(name, sha)
      @active_action = "create #{name} source worktree"
      path = File.join(output, 'worktrees', name)
      FileUtils.mkdir_p(File.dirname(path))
      raise "Refusing to reuse a worktree path: #{relative(path)}" if File.exist?(path)
      command!(['git', '-C', root, 'worktree', 'add', '--detach', path, sha], root, "worktree-#{name}.log")
      @worktrees[name] = path
    end

    def cleanup_worktrees
      @worktrees.each_value do |path|
        system('git', '-C', root, 'worktree', 'remove', '--force', path, out: File::NULL, err: File::NULL)
      end
    end

    def cleanup_temporary_artifacts
      FileUtils.rm_rf(@worker_classes)
      FileUtils.rm_rf(File.join(output, 'release-lib'))
      FileUtils.rm_rf(@gradle_fixture)
    end

    def build_source_variant(variant)
      @active_action = "build #{variant} source artifact"
      source = worktrees.fetch(variant)
      version = "8.9.0-bench-#{variant}-#{variant == 'candidate' ? @candidate_sha[0, 8] : @base_sha[0, 8]}"
      command = [File.join(source, 'gradlew'), ':graphql-dgs-codegen-core:installDist', ':graphql-dgs-codegen-gradle:jar',
        "-Prelease.version=#{version}", '--no-scan', '--no-build-cache', '--no-configuration-cache', '--max-workers=2', '--console=plain']
      command!(command, source, "build-#{variant}.log")
      library_dir = File.join(source, 'graphql-dgs-codegen-core', 'build', 'install', 'graphql-dgs-codegen-core', 'lib')
      plugin_matches = Dir.glob(File.join(source, 'graphql-dgs-codegen-gradle', 'build', 'libs', "graphql-dgs-codegen-gradle-#{version}.jar"))
      core_matches = Dir.glob(File.join(library_dir, 'graphql-dgs-codegen-core-*.jar'))
      shared_core_matches = Dir.glob(File.join(library_dir, 'graphql-dgs-codegen-shared-core-*.jar'))
      raise "Expected one core jar for #{variant}, found #{core_matches.size}" unless core_matches.size == 1
      raise "Expected one shared core jar for #{variant}, found #{shared_core_matches.size}" unless shared_core_matches.size == 1
      raise "Expected one Gradle plugin jar for #{variant}, found #{plugin_matches.size}" unless plugin_matches.size == 1

      @artifacts[variant] = {
        'commit' => git('rev-parse', 'HEAD', cwd: source),
        'build_version' => version,
        'core_jar' => File.basename(core_matches.first),
        'shared_core_jar' => File.basename(shared_core_matches.first),
        'plugin_jar' => File.basename(plugin_matches.first),
        'jars' => jar_identities(Dir.glob(File.join(library_dir, '*.jar')) + plugin_matches)
      }
      @artifacts[variant]['library_dir'] = library_dir
      @artifacts[variant]['plugin_path'] = plugin_matches.first
      persist_checkpoint
    end

    def resolve_release
      @active_action = "resolve release #{options[:release_version]}"
      lib_dir = File.join(output, 'release-lib')
      metadata_path = File.join(output, 'release-dependencies.tsv')
      command = gradle_command('saveResolvedClasspath', 'release', nil, nil) +
        ["-Dbench.resolvedDir=#{lib_dir}", "-Dbench.resolvedMetadata=#{metadata_path}"]
      command!(command, root, 'resolve-release.log')
      jars = Dir.glob(File.join(lib_dir, '*.jar'))
      core = jars.select { |jar| File.basename(jar).match?(/graphql-dgs-codegen-core-#{Regexp.escape(options[:release_version])}\.jar\z/) }
      shared_core = jars.select { |jar| File.basename(jar).match?(/graphql-dgs-codegen-shared-core-#{Regexp.escape(options[:release_version])}\.jar\z/) }
      plugin = jars.select { |jar| File.basename(jar).match?(/graphql-dgs-codegen-gradle-#{Regexp.escape(options[:release_version])}\.jar\z/) }
      raise "Published core #{options[:release_version]} was not resolved" unless core.size == 1
      raise "Published shared core #{options[:release_version]} was not resolved" unless shared_core.size == 1
      raise "Published Gradle plugin #{options[:release_version]} was not resolved" unless plugin.size == 1

      @artifacts['release'] = {
        'version' => options[:release_version],
        'core_jar' => File.basename(core.first),
        'shared_core_jar' => File.basename(shared_core.first),
        'plugin_jar' => File.basename(plugin.first),
        'resolved_dependencies' => File.file?(metadata_path) ? File.readlines(metadata_path, chomp: true) : [],
        'jars' => jar_identities(jars),
        'library_dir' => lib_dir,
        'plugin_path' => plugin.first
      }
      persist_checkpoint
    end

    def compile_worker
      @active_action = 'compile benchmark worker'
      FileUtils.mkdir_p(@worker_classes)
      release_dir = include_release? ? artifacts.fetch('release').fetch('library_dir') : artifacts.fetch('base').fetch('library_dir')
      classpath = jar_classpath(release_dir)
      source_file = File.join(root, 'benchmarks', 'worker', 'BenchmarkWorker.java')
      command = [File.join(@java_home, 'bin', 'javac'), '--release', '17', '-classpath', classpath, '-d', @worker_classes, source_file]
      command!(command, root, 'compile-worker.log')
    end

    def generate_schema
      @active_action = 'generate synthetic fixtures'
      cp = @worker_classes + File::PATH_SEPARATOR + jar_classpath(artifacts.fetch('candidate').fetch('library_dir'))
      documentation_profiles.each do |density|
        path = File.join(output, 'input', "synthetic-#{density}.graphqls")
        FileUtils.mkdir_p(File.dirname(path))
        command = java_prefix + ['-cp', cp, 'BenchmarkWorker', 'fixture', options[:type_count].to_s,
          options[:field_count].to_s, options[:seed].to_s, (density == 'dense').to_s, path]
        command!(command, root, "generate-fixture-#{density}.log")
        @schema_paths[density] = path
        @schema_digests[density] = Digest::SHA256.file(path).hexdigest
      end
      @artifacts['fixtures'] = @schema_paths.each_with_object({}) do |(density, path), fixtures|
        fixtures[density] = { 'sha256' => @schema_digests.fetch(density), 'bytes' => File.size(path), 'types' => options[:type_count], 'fields_per_type' => options[:field_count], 'seed' => options[:seed] }
      end
      persist_checkpoint
    end

    def run_worker_mode(mode)
      fork_orders.each_with_index do |order, fork_index|
        order.each { |variant| run_worker(variant, mode, fork_index + 1) }
      end
    end

    def fork_orders
      orders = Array.new(options[:forks]) { |index| index.even? ? %w[base candidate] : %w[candidate base] }
      orders.shuffle(random: @order_random).map do |order|
        order.insert(@order_random.rand(3), 'release') if include_release?
        order
      end
    end

    def run_worker(variant, mode, fork)
      @active_action = "run #{mode} for #{variant} fork #{fork}"
      identity = artifacts.fetch(variant)
      destination = File.join(output, 'outputs', mode, variant, "fork-#{fork}")
      FileUtils.rm_rf(destination)
      cp = @worker_classes + File::PATH_SEPARATOR + jar_classpath(identity.fetch('library_dir'))
      scale = mode.start_with?('serialize-') ? options[:serial_operations] : options[:type_count]
      schema = schema_path_for(mode)
      command = java_prefix + ['-cp', cp, 'BenchmarkWorker', 'run', mode, schema, destination,
        options[:warmups].to_s, options[:samples].to_s, scale.to_s, options[:writers].to_s,
        options[:prewarm_seconds].to_s]
      log = File.join(output, 'logs', "#{mode}-#{variant}-fork-#{fork}.log")
      text, status, elapsed = invoke(command, root, log)
      self.class.validate_command_status(status, command.map { |value| sanitize(value) })
      origins = parse_origins(text)
      verify_origins!(variant, origins, mode)
      prewarm = self.class.validate_prewarm(text, mode, options[:prewarm_seconds])
      raw_rows = parse_samples(text, mode)
      rows = self.class.validate_samples(raw_rows, mode, options[:warmups], options[:samples])
      rows.each do |row|
        row.merge!('variant' => variant, 'fork' => fork, 'schema_sha256' => @schema_digests[density_for(mode)], 'workload' => mode)
        @samples << row
      end
      output_manifest = manifest(destination, "#{mode}-#{variant}-fork-#{fork}")
      manifest_entry(mode, variant, fork, output_manifest)
      @runs << {
        'variant' => variant, 'workload' => mode, 'fork' => fork, 'command' => command.map { |value| sanitize(value) },
        'wall_millis' => elapsed, 'prewarm_iterations' => prewarm.fetch('iterations'),
        'prewarm_millis' => prewarm.fetch('milliseconds'), 'log' => relative(log),
        'class_origins' => origins, 'output_manifest' => output_manifest
      }
      puts "#{mode} #{variant} fork #{fork}: #{rows.count { |row| row['phase'] == 'measured' }} measured samples, #{output_manifest['sha256']}"
      persist_checkpoint
    end

    def run_gradle_suite
      @active_action = 'run Gradle generateJava comparisons'
      fixture_schema = File.join(@gradle_fixture, 'inputs', 'schema.graphqls')
      FileUtils.mkdir_p(File.dirname(fixture_schema))
      FileUtils.cp(@schema_paths.fetch(documentation_profiles.first), fixture_schema)
      fork_orders.each_with_index do |order, fork_index|
        order.each do |variant|
          options[:warmups].times { |iteration| run_gradle_variant(variant, fork_index + 1, 'warmup', iteration) }
        end
        options[:samples].times do |iteration|
          order.rotate(iteration % order.size).each do |variant|
            run_gradle_variant(variant, fork_index + 1, 'measured', iteration)
          end
        end
      end
    ensure
      FileUtils.rm_f(File.join(@gradle_fixture, 'inputs', 'schema.graphqls'))
    end

    def run_gradle_variant(variant, fork, phase, iteration)
      @active_action = "run Gradle generateJava for #{variant} fork #{fork}"
      FileUtils.rm_rf(File.join(@gradle_fixture, 'build'))
      command = gradle_command('generateJava', variant, fork, iteration)
      log = File.join(output, 'logs', "gradle-#{variant}-fork-#{fork}-#{phase}-#{iteration}.log")
      text, status, elapsed = invoke(command, root, log)
      self.class.validate_command_status(status, command.map { |value| sanitize(value) })
      marker = text.lines.find { |line| line.start_with?("BENCH_TASK\t") }
      raise "Missing Gradle task outcome for #{variant}" unless marker
      _, task, outcome, millis = marker.strip.split("\t")
      raise "Gradle task did not execute successfully: #{outcome}" unless task == 'generateJava' && outcome == 'SUCCESS'
      origins = parse_origins(text)
      verify_origins!(variant, origins, 'gradle-generateJava')
      output_dir = File.join(@gradle_fixture, 'build', 'generated')
      output_manifest = manifest(output_dir, "gradle-#{variant}-fork-#{fork}-#{phase}-#{iteration}")
      manifest_entry('gradle-generateJava', variant, fork, output_manifest)
      if phase == 'measured'
        [
          ['gradle-generateJava-task', Float(millis)],
          ['gradle-generateJava-invocation', elapsed]
        ].each do |mode, measured_millis|
          @samples << {
            'mode' => mode, 'variant' => variant, 'fork' => fork,
            'phase' => phase, 'iteration' => iteration, 'milliseconds' => measured_millis,
            'allocated_bytes' => nil, 'gc_count' => nil, 'gc_millis' => nil
          }
        end
      end
      @runs << {
        'variant' => variant, 'workload' => 'gradle-generateJava', 'fork' => fork,
        'phase' => phase, 'iteration' => iteration, 'command' => command.map { |value| sanitize(value) },
        'task_millis' => Float(millis), 'invocation_millis' => elapsed, 'task_outcome' => outcome,
        'log' => relative(log), 'class_origins' => origins, 'output_manifest' => output_manifest
      }
      puts "gradle-generateJava #{variant} fork #{fork} #{phase} #{iteration}: task #{millis} ms, invocation #{elapsed.round} ms"
      persist_checkpoint
    end

    def gradle_command(task, variant, fork, iteration)
      props = ["-Dbench.variant=#{variant}", "-Dbench.releaseVersion=#{options[:release_version]}", "-Dbench.writers=#{options[:writers]}"]
      if variant != 'release'
        identity = artifacts.fetch(variant)
        props += ["-Dbench.libDir=#{identity.fetch('library_dir')}", "-Dbench.pluginJar=#{identity.fetch('plugin_path')}"]
      end
      props += ["-Dorg.gradle.jvmargs=-Xms#{options[:heap_min]} -Xmx#{options[:heap_max]} -XX:+Use#{options[:collector]}GC", '--no-daemon', '--no-scan', '--no-build-cache',
        '--no-configuration-cache', '--rerun-tasks', '--max-workers=2', '--console=plain', "-Dorg.gradle.java.home=#{@java_home}"]
      [File.join(root, 'gradlew'), '-p', @gradle_fixture, task] + props
    end

    def verify_outputs
      @output_changes.clear
      @manifests.each do |mode, variants|
        next unless variants['base'] && variants['candidate']

        base_entries = variants.fetch('base').values
        candidate_entries = variants.fetch('candidate').values
        raise "Baseline/candidate manifest sample counts differ for #{mode}" unless base_entries.size == candidate_entries.size
        base_entries.zip(candidate_entries).each_with_index do |(baseline, candidate), index|
          next if baseline.fetch('sha256') == candidate.fetch('sha256')

          @output_changes << { 'workload' => mode, 'run' => index + 1,
            'baseline_sha256' => baseline.fetch('sha256'), 'candidate_sha256' => candidate.fetch('sha256') }
        end
      end
      @manifests.each do |mode, variants|
        next unless variants['release'] && variants['base']

        write_json("release-compatibility/#{safe_name(mode)}.json", {
          'workload' => mode,
          'release_version' => options[:release_version],
          'baseline_matches_release' => variants.fetch('base').values.map { |entry| entry['sha256'] } == variants.fetch('release').values.map { |entry| entry['sha256'] },
          'interpretation' => 'Informational because 8.8.0 predates JavaDoc escaping and correct multi-query variable serialization.'
        })
      end
    end

    def write_results
      persist_checkpoint
      write_json('summary.json', summaries)
      write_report
    end

    def persist_checkpoint
      return unless @output_initialized

      saved_artifacts = JSON.parse(JSON.generate(@artifacts))
      saved_artifacts.each_value do |identity|
        next unless identity.is_a?(Hash)

        identity.delete('library_dir')
        identity.delete('plugin_path')
      end
      write_json('artifacts.json', saved_artifacts)
      write_json('samples.json', @samples)
      write_json('runs.json', @runs)
      write_json('manifests.json', @manifests)
      write_json('output-changes.json', { 'accepted' => options[:accept_output_change], 'changes' => @output_changes })
    end

    def write_report
      report = []
      report << '# DGS codegen performance comparison'
      report << ''
      report << "Candidate `#{@candidate_sha}` compared with baseline `#{@base_sha}`."
      report << "Profile `#{options[:profile]}` used #{options[:forks]} fresh JVM fork(s), #{options[:prewarm_seconds]} seconds of untimed prewarm, #{options[:warmups]} warmup iterations and #{options[:samples]} measured iterations per worker."
      report << "Synthetic schema SHA-256 values are #{schema_digest_description}, with #{options[:type_count]} object types and #{options[:field_count]} generated fields per type plus Date and LocalDate fields."
      report << 'Main-thread allocation excludes writer threads and Gradle worker threads.'
      report << ''
      report << "| Workload | Base pooled median (range) | Candidate pooled median (range) | Median paired change | #{options[:release_version]} pooled median | Median paired change vs #{options[:release_version]} | Per-fork changes |"
      report << '|---|---:|---:|---:|---:|---:|---|'
      measured_modes.each do |mode|
        base_values = measured_values(mode, 'base')
        candidate_values = measured_values(mode, 'candidate')
        next if base_values.empty? || candidate_values.empty?

        comparison = paired_comparison(mode)
        fork_changes = comparison.fetch('forks').map { |fork| fork.fetch('change_percent') }
        change = comparison.fetch('median_change_percent')
        release_values = measured_values(mode, 'release')
        release_comparison = release_values.empty? ? 'not run' : format('%+.2f%%', paired_comparison(mode, 'release').fetch('median_change_percent'))
        report << "| #{mode} | #{format('%.3f', median(base_values))} ms (#{format('%.3f', base_values.min)} to #{format('%.3f', base_values.max)}) | #{format('%.3f', median(candidate_values))} ms (#{format('%.3f', candidate_values.min)} to #{format('%.3f', candidate_values.max)}) | #{format('%+.2f%%', change)} | #{release_values.empty? ? 'not run' : "#{format('%.3f', median(release_values))} ms"} | #{release_comparison} | #{fork_changes.map { |value| format('%+.2f%%', value) }.join(', ')} |"
      end
      report << ''
      report << 'Timing changes are descriptive and never fail the job.'
      report << 'The headline change is the median of paired-fork percentage changes; pooled medians describe absolute timings only.'
      report << 'No regression or improvement labels are assigned until same-artifact calibration supports a classifier across independent JVM forks.'
      report << 'Each Gradle measurement starts a new process, and invocation time includes JVM and Gradle startup.'
      report << ''
      report << '## Output compatibility'
      report << ''
      if @output_changes.empty?
        report << 'Candidate and baseline output manifests match for every measured workload.'
      else
        report << (options[:accept_output_change] ? 'Output changes were explicitly accepted for this run.' : 'Output changes fail this run; timing results are retained for investigation.')
        report << 'Changed output can change the amount of work, so timing differences cannot be attributed solely to generator efficiency.'
        report << ''
        report << '| Workload | Differing manifest pairs |'
        report << '|---|---:|'
        @output_changes.group_by { |change| change.fetch('workload') }.each do |mode, changes|
          report << "| #{mode} | #{changes.size} |"
        end
        report << ''
        report << 'See `output-changes.json` and the per-run manifests for exact output digests.'
      end
      if include_release?
        report << "Published release `#{options[:release_version]}` timing is context only; output differences are recorded separately and do not gate known release-era behavior."
      end
      report << ''
      File.write(File.join(output, 'report.md'), report.join("\n") + "\n")
    end

    def measured_modes
      @samples.map { |row| row['mode'] }.uniq
    end

    def measured_values(mode, variant, fork = nil)
      @samples.select do |row|
        row['mode'] == mode && row['variant'] == variant && row['phase'] == 'measured' && (!fork || row['fork'] == fork)
      end.map { |row| row['milliseconds'] }.compact
    end

    def summaries
      measured_modes.each_with_object({}) do |mode, result|
        result[mode] = %w[base candidate release].each_with_object({}) do |variant, variants|
          values = measured_values(mode, variant)
          next if values.empty?
          rows = @samples.select { |row| row['mode'] == mode && row['variant'] == variant && row['phase'] == 'measured' }
          variants[variant] = {
            'pooled_median_ms' => median(values), 'min_ms' => values.min, 'max_ms' => values.max,
            'main_thread_allocation_median_bytes_per_operation' => median(rows.map { |row| row['allocated_bytes'] }.compact),
            'gc_count_median_per_sample' => median(rows.map { |row| row['gc_count'] }.compact),
            'gc_millis_median_per_sample' => median(rows.map { |row| row['gc_millis'] }.compact),
            'forks' => options[:forks].times.each_with_object({}) do |index, per_fork|
              fork_rows = rows.select { |row| row['fork'] == index + 1 }
              next if fork_rows.empty?
              fork_values = fork_rows.map { |row| row['milliseconds'] }
              per_fork[(index + 1).to_s] = {
                'median_ms' => median(fork_values), 'min_ms' => fork_values.min, 'max_ms' => fork_values.max,
                'main_thread_allocation_median_bytes_per_operation' => median(fork_rows.map { |row| row['allocated_bytes'] }.compact),
                'gc_count_median_per_sample' => median(fork_rows.map { |row| row['gc_count'] }.compact),
                'gc_millis_median_per_sample' => median(fork_rows.map { |row| row['gc_millis'] }.compact)
              }
            end
          }
        end
        result[mode]['comparison'] = paired_comparison(mode)
        result[mode]['release_comparison'] = paired_comparison(mode, 'release') unless measured_values(mode, 'release').empty?
      end
    end

    def paired_comparison(mode, baseline = 'base')
      forks = options[:forks].times.map do |index|
        base_values = measured_values(mode, baseline, index + 1)
        candidate_values = measured_values(mode, 'candidate', index + 1)
        base = median(base_values)
        candidate = median(candidate_values)
        { 'fork' => index + 1, 'baseline_median_ms' => base, 'candidate_median_ms' => candidate,
          'change_percent' => (candidate - base) / base * 100.0 }
      end
      { 'median_change_percent' => median(forks.map { |fork| fork.fetch('change_percent') }), 'forks' => forks }
    end

    def median(values)
      return nil if values.empty?

      sorted = values.sort
      middle = sorted.length / 2
      sorted.length.odd? ? sorted[middle] : (sorted[middle - 1] + sorted[middle]) / 2.0
    end

    def parse_samples(text, mode)
      text.lines.each_with_object([]) do |line, rows|
        next unless line.start_with?("BENCH_SAMPLE\t")

        fields = line.strip.split("\t")
        raise "Malformed sample row for #{mode}: #{line}" unless fields.size == 8 && fields[1] == mode
        rows << {
          'mode' => fields[1], 'phase' => fields[2], 'iteration' => Integer(fields[3]),
          'milliseconds' => Float(fields[4]), 'allocated_bytes' => Float(fields[5]),
          'gc_count' => Integer(fields[6]), 'gc_millis' => Integer(fields[7])
        }
      end
    end

    def parse_origins(text)
      text.lines.each_with_object([]) do |line, origins|
        next unless line.start_with?("BENCH_ORIGIN\t")

        _, kind, artifact = line.strip.split("\t")
        origins << { 'kind' => kind, 'artifact' => artifact }
      end
    end

    def verify_origins!(variant, origins, workload)
      expected = artifacts.fetch(variant)
      core = origins.find { |origin| origin['kind'] == 'core' || origin['kind'] == 'codegen-core' }
      raise "Missing core class origin for #{variant} #{workload}" unless core
      raise "Wrong codegen core loaded for #{variant} #{workload}: #{core['artifact']}" unless core['artifact'] == expected.fetch('core_jar')
      client = origins.find { |origin| origin['kind'] == 'client' }
      raise "Missing shared core class origin for #{variant} #{workload}" unless client
      raise "Wrong shared core loaded for #{variant} #{workload}: #{client['artifact']}" unless client['artifact'] == expected.fetch('shared_core_jar')
      if workload == 'gradle-generateJava'
        plugin = origins.find { |origin| origin['kind'] == 'gradle-plugin' }
        raise "Missing plugin class origin for #{variant}" unless plugin
        raise "Wrong Gradle plugin loaded for #{variant}: #{plugin['artifact']}" unless plugin['artifact'] == expected.fetch('plugin_jar')
      end
      allowed = expected.fetch('jars').map { |jar| jar['name'] } + [expected.fetch('core_jar'), expected.fetch('shared_core_jar'), expected.fetch('plugin_jar')]
      origins.each do |origin|
        raise "Unexpected classpath artifact for #{variant}: #{origin['artifact']}" unless allowed.include?(origin['artifact'])
      end
    end

    def manifest(path, label)
      raise "Output directory missing: #{relative(path)}" unless File.directory?(path)
      files = Dir.glob(File.join(path, '**', '*')).select { |file| File.file?(file) }.sort
      raise "Empty generated output for #{label}" if files.empty?
      entries = files.map do |file|
        { 'path' => file.delete_prefix(path + File::SEPARATOR), 'bytes' => File.size(file), 'sha256' => Digest::SHA256.file(file).hexdigest }
      end
      canonical = entries.map { |entry| [entry['path'], entry['bytes'], entry['sha256']].join("\t") }.join("\n")
      digest = Digest::SHA256.hexdigest(canonical)
      manifest = { 'label' => label, 'file_count' => entries.size, 'bytes' => entries.sum { |entry| entry['bytes'] }, 'sha256' => digest, 'files' => entries }
      write_json("manifests/#{safe_name(label)}.json", manifest)
      manifest
    end

    def manifest_entry(mode, variant, fork, manifest)
      bucket = (@manifests[mode][variant] ||= {})
      key = "#{fork}-#{bucket.size + 1}"
      bucket[key] = { 'sha256' => manifest['sha256'], 'file_count' => manifest['file_count'], 'bytes' => manifest['bytes'] }
    end

    def jar_classpath(directory)
      jars = Dir.glob(File.join(directory, '*.jar')).sort
      raise "No runtime jars under #{relative(directory)}" if jars.empty?
      jars.join(File::PATH_SEPARATOR)
    end

    def jar_identities(paths)
      paths.map do |path|
        { 'name' => File.basename(path), 'sha256' => Digest::SHA256.file(path).hexdigest, 'bytes' => File.size(path) }
      end.sort_by { |entry| entry['name'] }
    end

    def java_prefix
      collector = options[:collector] == 'G1' ? 'UseG1GC' : "Use#{options[:collector]}GC"
      [java_bin, "-Xms#{options[:heap_min]}", "-Xmx#{options[:heap_max]}", "-XX:+#{collector}"]
    end

    def java_bin
      File.join(@java_home, 'bin', 'java')
    end

    def heap_bytes(size)
      amount = Integer(size[0...-1])
      multiplier = { 'k' => 1024, 'm' => 1024**2, 'g' => 1024**3 }.fetch(size[-1].downcase)
      amount * multiplier
    end

    def detect_java_home
      text, status = Open3.capture2e('java', '-XshowSettings:properties', '-version')
      self.class.validate_command_status(status, ['java', '-XshowSettings:properties', '-version'])
      match = text.match(/^\s*java\.home\s*=\s*(.+)$/)
      raise 'Could not determine the active JDK home' unless match
      match[1].strip
    end

    def command_output(command)
      output, status = Open3.capture2e(*command)
      self.class.validate_command_status(status, command)
      output
    end

    def command!(command, cwd, log_name)
      text, status, = invoke(command, cwd, File.join(output, 'logs', log_name))
      self.class.validate_command_status(status, command.map { |value| sanitize(value) })
      text
    end

    def invoke(command, cwd, log)
      FileUtils.mkdir_p(File.dirname(log))
      env = { 'JAVA_HOME' => @java_home, 'PATH' => File.join(@java_home, 'bin') + File::PATH_SEPARATOR + ENV.fetch('PATH'),
        'JAVA_TOOL_OPTIONS' => nil, 'JDK_JAVA_OPTIONS' => nil, 'JAVA_OPTS' => nil, 'GRADLE_OPTS' => nil }
      started = Process.clock_gettime(Process::CLOCK_MONOTONIC)
      text, status = Open3.capture2e(env, *command, chdir: cwd)
      elapsed = (Process.clock_gettime(Process::CLOCK_MONOTONIC) - started) * 1000.0
      File.write(log, sanitize(text))
      [text, status, elapsed]
    end

    def git(*args, cwd: root)
      text, status = Open3.capture2e('git', '-C', cwd, *args)
      self.class.validate_command_status(status, ['git', *args])
      text.strip
    end

    def write_json(path, value)
      target = File.join(output, path)
      FileUtils.mkdir_p(File.dirname(target))
      File.write(target, JSON.pretty_generate(value) + "\n")
    end

    def safe_name(value)
      value.gsub(/[^A-Za-z0-9_.-]/, '_')
    end

    def sanitize(value)
      value.to_s.gsub(root, '$WORKSPACE').gsub(output, '$RESULTS')
    end

    def relative(path)
      path.to_s.gsub(root, '$WORKSPACE').gsub(output, '$RESULTS')
    end
  end
end

if $PROGRAM_NAME == __FILE__
  begin
    exit DgsBenchmark::Runner.new(ARGV).run
  rescue StandardError => error
    warn "Benchmark failed: #{error.message}"
    exit 1
  end
end
