
var mongoose = require('mongoose');
var crypto = require("crypto");

mongoose.connect('mongodb://localhost/nodejsexpress');

// Validators
validates_length_of = function(length) {
  return [ function(value) { return value.toString().length >= length }, "Value too short. Min " + length ];
}

// User
var UserSchema = new mongoose.Schema({
  name: { type: String, unique: true, validate: validates_length_of(4) },
  crypted_password: String
});

UserSchema.virtual("password").set(function(password) { this.crypted_password = User.digest_password(password); })

var User = exports.User = mongoose.model("User", UserSchema);

User.digest_password = function(password) { return crypto.createHash("MD5").update(password).digest("hex"); }
User.valid = function(username, password, callback) { User.findOne({ name: username, crypted_password: User.digest_password(password) }, callback); }

// Website

var Website = exports.Website = mongoose.model("Website", new mongoose.Schema({
  name: { type: String, unique: true, validate: validates_length_of(4) },
  user: { type: mongoose.Schema.ObjectId, ref: 'User' }
}));

// Visit
var Visit = exports.Visit = mongoose.model("Visit", new mongoose.Schema({
  website: { type: mongoose.Schema.ObjectId, ref: "Website" },
  path: String,
  ip: String,
  browser: String,
  os: String,
  visited_at: Date,
}));
