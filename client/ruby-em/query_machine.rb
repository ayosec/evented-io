
require 'json'
require 'set'

class QueryMachine
  attr_reader :tasks, :machine_start, :machine_stop

  Task = Struct.new(:id, :start_time, :end_time, :succeed, :http_request, :query)

  def initialize(options)
    @tasks = []
    @running = 0
    @finished = 0
    @failed = 0

    @source = options.source
    @base_url = options.base_url
    @concurrency = options.concurrency
    @timeout = options.timeout

    @machine_start = Time.now

    @is_waiting = true
  end

  def new_queries!
    if @is_waiting
      #STDERR.puts "Waiting for #@running request to finish..."
      return if @running > 0
      @is_waiting = false
    end

    while @running < @concurrency and not @source.eof?
      line = @source.readline.strip
      process_query JSON.parse(line) unless line.empty?
    end

    if @source.eof? and not @is_waiting and @running == 0
      EM.next_tick do
        @machine_stop = Time.now
        EM.stop
      end
    end
  end

  def dump_progress
    STDERR.print "#@finished finished. #@failed failed. #@running running: "
    STDERR.puts @tasks.select {|q| q.end_time.nil? }.group_by {|q| q.query["request"]["uri"] }.map {|uri, queries| "#{queries.size} -> #{uri}" }.join(", ")
  end

  def process_query(query)

    if query["wait"]
      if @running
        @is_waiting = true
        return
      end
    end

    request = query["request"]
    options = {
      path: request["uri"],
      connect_timeout: @timeout,
      inactivity_timeout: @timeout
    }

    if request["method"] !~ /\AGET|POST\Z/i
      STDERR.puts "Unknown method in #{request.inspect}"
      return
    end

    if request["auth"]
      options[:head] = { 'authorization' => request["auth"] }
    end

    if request["query"]
      options[:query] = request["query"]
    end

    task = Task.new
    task.id = @tasks.size + 1
    task.start_time = Time.now
    task.query = query

    task.http_request = http_request = EventMachine::HttpRequest.new(@base_url).send(request["method"].downcase, options)

    http_request.errback do |hr|
      # There is no way to get the error. http_request.error returns nil
      # Just remove this request to the pool
      task.end_time = Time.now
      task.succeed = false
      @running -= 1
      @failed += 1
      new_queries!
    end

    http_request.callback do
      if not task.end_time.nil?
        raise "Task closed again!"
      end

      @running -= 1
      @finished += 1
      new_queries!

      # Validate the response after create a new request
      response = query["response"]
      succeed = catch(:result) do
        task.end_time = Time.now

        if http_request.response_header.status != response["status"]
          throw :result, false
        end

        if http_request.response_header.status == 200
          result = JSON.parse(http_request.response)
          if key = response["has_key"]
            unless result.has_key?(key)
              throw :result, false
            end
          end

          if set = response["compare_set"]
            unless Set.new(result) == Set.new(set)
              throw :result, false
            end
          end
        end

        true
      end

      unless task.succeed = succeed
        @failed += 1
      end

    end

    @running += 1
    @tasks << task
  end
end
