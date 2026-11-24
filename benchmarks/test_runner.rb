require 'minitest/autorun'
require 'open3'
require 'rbconfig'
require 'tmpdir'
require_relative 'run'

class BenchmarkRunnerTest < Minitest::Test
  def test_detects_missing_worker_samples
    rows = [{ 'mode' => 'java-model', 'phase' => 'measured', 'iteration' => 0, 'milliseconds' => 4.0, 'allocated_bytes' => 128.0 }]
    error = assert_raises(RuntimeError) do
      DgsBenchmark::Runner.validate_samples(rows, 'java-model', 1, 2)
    end
    assert_match(/Missing or duplicate warmup samples/, error.message)
  end

  def test_rejects_failed_worker_process
    _output, status = Open3.capture2e(RbConfig.ruby, '-e', 'exit 7')
    error = assert_raises(RuntimeError) do
      DgsBenchmark::Runner.validate_command_status(status, ['ruby', 'fake-worker'])
    end
    assert_match(/status 7/, error.message)
  end

  def test_output_drift_retains_reports_and_cleans_temporary_artifacts
    with_fixture_runner do |runner|
      runner.drift = true
      error = assert_raises(RuntimeError) { runner.run }

      assert_match(/Generated output changed for fixture/, error.message)
      assert File.file?(File.join(runner.output, 'summary.json'))
      report = File.read(File.join(runner.output, 'report.md'))
      assert_includes report, 'Output changes fail this run'
      assert_includes report, '| fixture | 4 |'
      failure = JSON.parse(File.read(File.join(runner.output, 'failure.json')))
      assert_equal 'verify output manifests', failure.fetch('action')
      refute File.exist?(File.join(runner.output, 'worker-classes'))
      refute File.exist?(File.join(runner.output, 'gradle-fixture'))
    end
  end

  def test_accepts_intentional_output_drift_without_hiding_it
    with_fixture_runner('--accept-output-change') do |runner|
      runner.drift = true

      assert_equal 0, runner.run
      changes = JSON.parse(File.read(File.join(runner.output, 'output-changes.json')))
      assert_equal true, changes.fetch('accepted')
      assert_equal 4, changes.fetch('changes').size
      assert_includes File.read(File.join(runner.output, 'report.md')), 'Output changes were explicitly accepted'
      refute File.exist?(File.join(runner.output, 'failure.json'))
    end
  end

  def test_output_acceptance_does_not_mask_missing_manifests
    with_fixture_runner('--accept-output-change') do |runner|
      runner.missing_manifest = true

      error = assert_raises(RuntimeError) { runner.run }
      assert_match(/manifest sample counts differ/, error.message)
    end
  end

  def test_worker_failure_still_fails_and_cleans_compiled_artifacts
    with_fixture_runner('--accept-output-change') do |runner|
      runner.worker_failure = true

      error = assert_raises(RuntimeError) { runner.run }
      assert_match(/fixture worker failed/, error.message)
      refute File.exist?(File.join(runner.output, 'worker-classes'))
      refute File.exist?(File.join(runner.output, 'gradle-fixture'))
    end
  end

  def test_report_uses_paired_changes_when_pooled_medians_reverse_the_direction
    Dir.mktmpdir('dgs-paired-report') do |output|
      runner = DgsBenchmark::Runner.new(['--forks', '2', '--include-release', 'true', '--output-dir', output])
      { 'base' => [[1, 1, 10, 10], [10, 10, 100, 100]],
        'candidate' => [[4, 4, 4, 4], [50, 50, 50, 50]],
        'release' => [[1, 1, 10, 10], [10, 10, 100, 100]] }.each do |variant, forks|
        forks.each_with_index do |values, fork|
          values.each { |value| runner.samples << sample(variant, fork + 1, value) }
        end
      end
      runner.send(:write_results)

      summary = JSON.parse(File.read(File.join(output, 'summary.json'))).fetch('fixture')
      assert_operator summary.fetch('candidate').fetch('pooled_median_ms'), :>, summary.fetch('base').fetch('pooled_median_ms')
      assert_in_delta(-18.1818, summary.fetch('comparison').fetch('median_change_percent'), 0.0001)
      assert_in_delta(-18.1818, summary.fetch('release_comparison').fetch('median_change_percent'), 0.0001)
      report = File.read(File.join(output, 'report.md'))
      assert_includes report, '| -18.18% |'
      assert_includes report, '-27.27%, -9.09%'
      refute_includes report, '| Classification |'
      refute_includes report, '| regression |'
      refute_includes report, '| improvement |'
    end
  end

  def test_full_profile_orders_are_reproducible_and_balance_pair_order
    first = DgsBenchmark::Runner.new(['--profile', 'full'])
    second = DgsBenchmark::Runner.new(['--profile', 'full'])
    orders = first.send(:fork_orders)

    assert_equal 4, orders.size
    assert_equal orders, second.send(:fork_orders)
    assert orders.all? { |order| order.sort == %w[base candidate release] }
    assert_equal 2, orders.count { |order| order.index('base') < order.index('candidate') }
  end

  def test_requires_a_valid_worker_prewarm_marker
    marker = "BENCH_PREWARM\tjava-model\t3\t1000.250\n"

    assert_equal({ 'iterations' => 3, 'milliseconds' => 1000.25 },
      DgsBenchmark::Runner.validate_prewarm(marker, 'java-model', 1.0))
    assert_raises(RuntimeError) { DgsBenchmark::Runner.validate_prewarm('', 'java-model', 0.0) }
    assert_raises(RuntimeError) { DgsBenchmark::Runner.validate_prewarm(marker, 'java-model', 1.1) }
  end

  def test_accepts_complete_finite_sample_set
    rows = [
      { 'mode' => 'java-model', 'phase' => 'warmup', 'iteration' => 0, 'milliseconds' => 4.0, 'allocated_bytes' => 128.0 },
      { 'mode' => 'java-model', 'phase' => 'measured', 'iteration' => 0, 'milliseconds' => 5.0, 'allocated_bytes' => 132.0 },
      { 'mode' => 'java-model', 'phase' => 'measured', 'iteration' => 1, 'milliseconds' => 4.5, 'allocated_bytes' => 130.0 }
    ]
    assert_equal 3, DgsBenchmark::Runner.validate_samples(rows, 'java-model', 1, 2).size
  end

  def test_rejects_duplicate_sample_ids_and_negative_allocation
    duplicate_rows = [
      { 'mode' => 'java-model', 'phase' => 'warmup', 'iteration' => 0, 'milliseconds' => 1.0, 'allocated_bytes' => 4.0 },
      { 'mode' => 'java-model', 'phase' => 'measured', 'iteration' => 0, 'milliseconds' => 1.0, 'allocated_bytes' => 4.0 },
      { 'mode' => 'java-model', 'phase' => 'measured', 'iteration' => 0, 'milliseconds' => 1.0, 'allocated_bytes' => 4.0 }
    ]
    assert_raises(RuntimeError) { DgsBenchmark::Runner.validate_samples(duplicate_rows, 'java-model', 1, 2) }

    negative_rows = [{
      'mode' => 'java-model', 'phase' => 'warmup', 'iteration' => 0,
      'milliseconds' => 1.0, 'allocated_bytes' => -1.0
    }]
    error = assert_raises(RuntimeError) { DgsBenchmark::Runner.validate_samples(negative_rows, 'java-model', 1, 0) }
    assert_match(/negative sample/, error.message)
  end

  private

  def with_fixture_runner(*args)
    Dir.mktmpdir('dgs-runner-test') do |output|
      yield FixtureRunner.new(['--output-dir', output, '--profile', 'full', '--include-release', 'false', *args])
    end
  end

  def sample(variant, fork, milliseconds)
    { 'mode' => 'fixture', 'variant' => variant, 'fork' => fork, 'phase' => 'measured', 'milliseconds' => milliseconds.to_f }
  end

  # Substitute source builds and JVM launches; exercise the real manifest, report and failure lifecycle.
  class FixtureRunner < DgsBenchmark::Runner
    attr_accessor :drift, :worker_failure, :missing_manifest

    private

    def validate_options; end
    def create_worktree(*); end
    def build_source_variant(*); end
    def generate_schema; end
    def metadata; {}; end

    def compile_worker
      FileUtils.mkdir_p(@worker_classes)
      File.write(File.join(@worker_classes, 'worker.class'), 'compiled fixture')
    end

    def run_suites
      raise 'fixture worker failed' if worker_failure

      options[:forks].times do |fork|
        %w[base candidate].each do |variant|
          destination = File.join(output, 'outputs', variant, fork.to_s)
          FileUtils.mkdir_p(destination)
          File.write(File.join(destination, 'source.txt'), drift && variant == 'candidate' ? 'changed' : 'identical')
          manifest_entry('fixture', variant, fork + 1, manifest(destination, "fixture-#{variant}-#{fork}"))
          @samples << { 'mode' => 'fixture', 'variant' => variant, 'fork' => fork + 1,
            'phase' => 'measured', 'milliseconds' => variant == 'base' ? 10.0 : 20.0 }
        end
      end
      @manifests.fetch('fixture').fetch('candidate').shift if missing_manifest
    end
  end
end
