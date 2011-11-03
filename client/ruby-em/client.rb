#!/usr/bin/env ruby

require 'rubygems'
require 'bundler'
Bundler.require

require "em-http-request"
require_relative 'cmdline_parser'
require_relative 'query_machine'
require_relative 'report'

options = CmdlineParser.parse!

begin
  qm = QueryMachine.new(options)

  EventMachine.run do
    qm.new_queries!

    if options[:interval_info]
      EventMachine.add_periodic_timer options[:interval_info] do
        qm.dump_progress
      end
    end
  end
rescue Interrupt
  STDERR.puts "Interrupt!"
end

Report.generate! $stdout, options, qm
