
require 'optparse'
require_relative 'report'

module CmdlineParser
  extend self

  Options = Struct.new(:concurrency, :interval_info, :base_url, :source, :timeout, :report)

  def parse!
    options = Options.new
    options.concurrency = 50
    options.base_url = "http://localhost:3000"
    options.source = ARGF
    options.timeout = 30
    options.report = "stats"

    OptionParser.new do |opts|
      opts.banner = "Usage: #$0 [options]"

      opts.on "-c", "--concurrency N", Integer, "Max concurrent connections" do |c|
        options.concurrency = c
      end

      opts.on "-i", "--interval-info N", Float, "Dump progress every N seconds" do |i|
        options.interval_info = i
      end

      opts.on "-u", "--base-url URL", String, "Base URL for resolve URIs" do |b|
        options.base_url = b
      end

      opts.on "-t", "--timeout N", Float, "Request timeout, in seconds" do |t|
        options.timeout = t
      end

      opts.on "-r", "--report R", Report::Types.keys, "Report type (one of #{Report::Types.keys.join(", ")})" do |r|
        options.report = r
      end

    end.parse!

    options
  end
end
