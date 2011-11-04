
class Visit < Model::Base

  belongs_to :website

  field :path
  field :ip
  field :browser
  field :os
  field :visited_at

  attr_accessible :path, :ip, :browser, :os
end
