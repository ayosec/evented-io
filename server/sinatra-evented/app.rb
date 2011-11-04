
require 'bundler'
Bundler.require

require 'sinatra'
require 'sinatra/async'
require 'time'

require_relative 'models/base'
require_relative 'models/user'
require_relative 'models/website'
require_relative 'models/visit.rb'

class App < Sinatra::Base
  register Sinatra::Async

  def json_reply(status, object)
    reply [
      status,
      {"Content-Type" => "application/json"},
      object.to_json
    ]
  end

  def reply(args)
    request.env['async.callback'].call args
  end

  def reply_unauth!
    reply [ 401, {'Content-Type' => 'text/plain'}, ["Unauthorized"] ]
  end

  def auth!
    begin
      credentials = Rack::Auth::Basic::Request.new(env).credentials
    rescue
      reply_unauth!
      return
    end

    User.valid?(*credentials) do |user|
      user ? yield(user) : reply_unauth!
    end
  end

  apost "/users" do
    user = User.new(params)
    user.save do |succeed|
      if succeed
        json_reply 200, user_id: user.id
      else
        json_reply 403, errors: user.errors.messages
      end
    end
  end

  apost "/websites" do
    auth! do |user|
      website = Website.new(name: params[:name])
      website.user = user
      website.save do |succeed|
        if succeed
          json_reply 200, website_id: website.id
        else
          json_reply 403, errors: website.errors
        end
      end
    end
  end

  apost "/visit" do

    Website.first(name: params[:name]) do |website|
      if website.nil?
        json_reply 404, {}
        next
      end

      visited_at = Time.parse(params[:visited_at])
      params.delete :name
      params.delete :visited_at

      visit = Visit.new(params)
      visit.website = website
      visit.visited_at = visited_at
      visit.save do
        json_reply 200, {}
      end
    end

  end

  aget "/stats" do
    auth! do |user|

      Website.first(name: params[:name]) do |website|
        if website.nil?
          json_reply 404, {}
          next
        end

        unless website.user_id == user.id
          json_reply 403, {}
          next
        end

        key_generator = []
        fields = []
        params[:query].to_s.split(",").each do |item|
          case item
          when "os", "path", "ip", "browser"
            key_generator << "this.#{item}.toString()"
            fields << "#{item}: this.#{item}"

          when "hour"
            t = 'pad(this.visited_at.getUTCHours()) + ":" + pad(this.visited_at.getUTCMinutes())'
            key_generator << t
            fields << "hour: #{t}"

          when "day"
            t = 'this.visited_at.getUTCFullYear().toString() + pad(this.visited_at.getUTCMonth() + 1) + pad(this.visited_at.getUTCDate())'
            key_generator << t
            fields << "day: #{t}"

          else
            json_reply 403, {errors: "Invalid item: #{item}"}
            next
          end

        end

        if fields.empty?
          json_reply 403, {errors: "No items to query"}
          next
        end

        deferrable = Visit.collection.map_reduce(
          %[function() {
              var pad = function(n) {
                n = n.toString();
                return n.length == 1 ? ("0" + n) : n;
              };

              emit(#{key_generator.join(" + '|' + ")}, { #{fields.join(",")}, n: 1 })
            }],
          %[function(k, v) {
              var acc = v[0], i = 1, l = v.length;
              while(i < l) {
                acc.n += v[i].n;
                i++;
              };
              return acc;
            }],
          query: { website_id: website.id },
          out: { inline: 1 },
          raw: true)

        deferrable.errback do
          json_reply 500, {}
        end

        deferrable.callback do |mr_result|
          result = mr_result["results"].map do |item|
            {
              count: item["value"].delete("n").to_i,
              key: item["value"]
            }
          end

          json_reply 200, result
        end
      end

    end

  end
end
