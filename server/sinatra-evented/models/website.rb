
class Website < Model::Base
  belongs_to :user

  field :name

  validates_length_of :name, allow_blank: false, minimum: 4
  validates_uniqueness_of :name

  index :name, unique: true
end

