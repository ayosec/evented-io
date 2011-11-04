# coding: UTF-8

require 'erb'

module Report
  Types = {}

  class Base

    def self.inherited(cls)
      Report::Types[cls.name.split("::").last.downcase] = cls
    end

    attr_reader :output, :options, :query_machine

    def initialize(output, options, query_machine)
      @output = output
      @options = options
      @query_machine = query_machine
    end

    def write(*data)
      data.each {|datum| @output << datum }
    end

    def puts(*rows)
      rows.each {|row| write(row, "\n") }
    end

    def results
      tasks = query_machine.tasks
      result = {
        time_running: query_machine.machine_stop - query_machine.machine_start,
        started_at: query_machine.machine_start.to_s,
        groups: []
      }

      groups = [ ["*", tasks] ].concat tasks.group_by {|task| task["query"]["request"]["uri"] }.to_a
      groups.each do |label, tasks|
        times = tasks.map {|task| task.end_time - task.start_time }

        Σ = times.inject(:+)
        τ = times.size.to_f
        α = Σ / τ

        σ = Math.sqrt((times.inject(0) {|β, χ| β + (χ - α) ** 2}) / τ)  # standar deviation

        result[:groups] << {
          label: label,
          total_requests: tasks.size,
          failed_requests: tasks.count {|task| !task.succeed },
          max_time: times.max,
          min_time: times.min,
          avg_time: α,
          std_deviation: σ
        }
      end

      result
    end

    def req_time(time)
      "%.4fs (%.2f reqs/s)" % [time, 1 / time]
    end
  end

  class HTML < Base
    include ERB::Util

    TemplatePath = File.join(File.dirname(__FILE__), "report_template.erb")

    def generate!
      write ERB.new(File.read(TemplatePath)).result(binding).gsub(/^    /, '')
    end

  end

  class JSON < Base
    def generate!
      puts results.to_json
    end
  end

  class Stats < JSON

    def show_req_time(label, time)
      puts "#{label}: #{req_time(time)}"
    end

    def generate!
      results = self.results
      puts "Time running: #{results[:time_running]}s"
      results[:groups].each do |group|
        puts "\n#{group[:label]}:"
        puts "Total requests: #{group[:total_requests]}"
        puts "Failed requests: #{group[:failed_requests]}"
        show_req_time "Max time", group[:max_time]
        show_req_time "Min time", group[:min_time]
        show_req_time "Avg time", group[:avg_time]
        puts "Std. deviation (σ): %.4fs" % group[:std_deviation]
      end
    end
  end


  def self.generate!(output, options, query_machine)
    Report::Types[options.report].new(output, options, query_machine).generate!
  end

end
