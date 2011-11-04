#
# The Model::Base is a MongoID-like implementation based con EM::Mongo.
# *IT IS NOT PRODUCTION READY*, just functional enought.

require 'digest/md5'
require 'em-mongo'
require 'active_model'

module Model
  class Base
    include ActiveModel::Validations
    include ActiveModel::MassAssignmentSecurity

    class <<self
      def mongodb_connect
        Thread.current["mongodb_connection"] ||= begin
          conn = EM::Mongo::Connection.new('localhost').db('eventedsinatra')

          # Create indexes
          db_indexes.each do |collection_name, arguments|
            conn.collection(collection_name).create_index(*arguments)
          end

          conn
        end
      end

      def collection_name
        name.underscore + "s"
      end

      def collection
        mongodb_connect.collection collection_name
      end

      def first(conditions = {})
        def_doc = EM::Mongo::Cursor.new(collection, selector: conditions, limit: 1).next_document
        def_doc.errback { yield nil }
        def_doc.callback do |doc|
          yield doc && new.tap {|i| i.attributes = doc }
        end

        def_doc
      end

      # Macros

      attr_accessor :db_indexes
      def index(field, options = {})
        self.db_indexes ||= []
        self.db_indexes << [collection_name, [ [[field, 1]], options ] ]
      end

      def field(*names)
        names.each do |name|
          class_eval <<-EOM
            def #{name}
              @attributes["#{name}"]
            end
            def #{name}=(v)
              @attributes["#{name}"] = v
            end
          EOM
        end
      end

      def belongs_to(rel_name)
        rel_name = rel_name.to_s
        field rel_name + "_id"
        class_eval <<-EOM
          def #{rel_name}
            #{rel_name.camelcase}.find(#{rel_name}_id)
          end
          def #{rel_name}=(that_id)
            @attributes["#{rel_name}_id"] = that_id.id
          end
        EOM
      end

      def validates_uniqueness_of(*fields)
        #fields.each do |field|
        #  validate do |record|
        #    that = record.class.where(field => send(field)).first
        #    if that and that.id != id
        #      errors.add field, "not unique"
        #    end
        #  end
        #end
      end

    end

    attr_accessor :attributes

    def id
      @attributes["_id"]
    end

    def id=(new_id)
      @attributes["_id"] = new_id
    end

    def initialize(attrs = nil)
      @attributes = {}
      assign_attributes(attrs) if attrs
    end

    def assign_attributes(values, options = {})
      sanitize_for_mass_assignment(values, options[:as] || :default).each do |k, v|
        send("#{k}=", v)
      end
    end

    def save
      if valid?
        deferable = self.class.collection.safe_save(@attributes)

        deferable.callback do |doc_id|
          self.id = doc_id
          yield true
        end

        deferable.errback do |errors|
          self.errors.add :base, errors
          yield false
        end

        deferable
      else
        yield false
        nil
      end
    end

  end


end
