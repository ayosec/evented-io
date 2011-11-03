
require 'mongoid'
require 'digest/md5'

Mongoid.configure do |config|
  config.master = Mongo::Connection.new.db("prototypeapp")
end

class User
  include Mongoid::Document

  field :name
  field :crypted_password

  def password=(new_password)
    self.crypted_password = self.class.digest_password(new_password)
  end

  attr_accessible :name, :password
  validates_length_of :name, allow_blank: false, minimum: 4
  validates_uniqueness_of :name

  index :name, unique: true

  class <<self
    def digest_password(password)
      Digest::MD5.hexdigest(password)
    end

    def valid?(username, password)
      first(conditions: {
        name: username,
        crypted_password: digest_password(password)
      })
    end
  end

end

class Website
  include Mongoid::Document
  belongs_to :user

  field :name

  validates_length_of :name, allow_blank: false, minimum: 4
  validates_uniqueness_of :name

  index :name, unique: true
end

class Visit
  include Mongoid::Document

  belongs_to :website

  field :path
  field :ip
  field :browser
  field :os
  field :visited_at

  attr_accessible :path, :ip, :browser, :os
end
