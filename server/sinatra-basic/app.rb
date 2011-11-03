
require 'bundler'
Bundler.require

require 'sinatra'
require 'time'
require './models'

def json_reply(status, object)
  [
    status,
    {"Content-Type" => "application/json"},
    object.to_json
  ]
end

class BasicAuth
  def initialize(app)
    @app = app
  end

  def call(env)
    catch :user_required do
      return @app.call(env)
    end

    [ 401, {'Content-Type' => 'text/plain'}, ["Unauthorized"] ]
  end
end

def auth!
  credentials = Rack::Auth::Basic::Request.new(env).credentials
  User.valid?(*credentials) or throw :user_required
rescue
  throw :user_required
end

get "/" do
  # Just to query something to the database
  "#{Website.count} webs"
end

post "/users" do
  user = User.new(params)
  if user.save
    json_reply 200, user_id: user.id
  else
    json_reply 403, errors: user.errors
  end
end

post "/websites" do
  user = auth!
  website = Website.new(name: params[:name])
  website.user = user
  if website.save
    json_reply 200, website_id: website.id
  else
    json_reply 403, errors: website.errors
  end
end

post "/visit" do

  website = Website.first(conditions: { name: params[:name] })
  if website.nil?
    return json_reply(404, {})
  end

  visited_at = Time.parse(params[:visited_at])
  params.delete :name
  params.delete :visited_at

  visit = Visit.new(params)
  visit.website = website
  visit.visited_at = visited_at
  visit.save
  json_reply 200, {}

end

get "/stats" do
  user = auth!

  website = Website.first(conditions: { name: params[:name] })
  if website.nil?
    return json_reply(404, {})
  end

  unless website.user == user
    return json_reply(403, {})
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
      return json_reply(403, {errors: "Invalid item: #{item}"})
    end

  end

  if fields.empty?
    return json_reply(403, {errors: "No items to query"})
  end

  mr_result = Visit.collection.map_reduce(
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

  result = mr_result["results"].map do |item|
    {
      count: item["value"].delete("n").to_i,
      key: item["value"]
    }
  end

  json_reply 200, result
end
