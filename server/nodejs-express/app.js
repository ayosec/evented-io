
var express = require('express');
var models = require('./models');

function auth(request, response, next) {
  var unauth = function() { response.send("Unauthorized", 401); };

  var authorization = request.headers.authorization;
  if (authorization && authorization.search('Basic ') === 0) {
    var credentials = new Buffer(authorization.split(' ')[1], 'base64').toString().split(":");
    models.User.valid(credentials[0], credentials[1], function(error, user) {
      if(error) {
        response.send(error.toString(), 500)
      } else if(user) {
        request.current_user = user;
        next();
      } else {
        unauth();
      }
    });
  } else {
    unauth();
  }
}

function body_requested(request, response, next) {
  if(request.body)
    next();
  else
    response.send("NO PARAMS", 403);
}

var app = express.createServer();
app.configure(function() {
  app.use(express.bodyParser());
  app.use(app.router);
});

app.post('/users', body_requested, function(request, response) {

  var user = new models.User({
    name: request.body.name,
    password: request.body.password
  });

  user.save(function(error) {
    if(error)
      response.json({ errors: error }, 403);
    else
      response.json({ user_id: user.id });
  });
});

app.post('/websites', auth, body_requested, function(request, response) {

  var website = new models.Website({ name: request.body.name, user: request.current_user.id });
  website.save(function(error) {
    if(error)
      response.json({ errors: error }, 403);
    else
      response.json({ website_id: website.id });
  });
});

app.post("/visit", body_requested, function(request, response) {

  models.Website.findOne({name: request.body.name}, function(error, website) {
    if(error)
      return response.json({ errors: error }, 403);

    if(!website)
      return response.send("NOT FOUND", 404);

    var value, attributes = {};
    var valid_attributes = ["path", "ip", "browser", "os", "visited_at"];
    valid_attributes.forEach(function(name) {
      if(value = request.body[name])
        attributes[name] = value;
    });

    attributes.visited_at = new Date(request.body.visited_at);
    attributes.website = website

    visit = new models.Visit(attributes)
    visit.save(function(error) {
      if(error)
        response.json({ errors: error }, 403);
      else
        response.json({ok: 1})
    });

  });
});

app.get("/stats", auth, function(request, response) {
  models.Website.findOne({name: request.query.name}, function(error, website) {
    if(error)
      return response.json({ errors: error }, 403);

    if(!website)
      return response.send("NOT FOUND", 404);

    if(website.user.toString() !== request.current_user.id)
      return response.send("UNAUTHORIZED", 403);

    var key_generator = []
    var fields = []
    var query_items = request.query.query.toString().split(",");
    for(var i = 0; i < query_items.length; i++) {
      var item = query_items[i];

      switch(item) {
        case "os":
        case "path":
        case "ip":
        case "browser":
          key_generator.push("this." + item + ".toString()");
          fields.push(item + ": this." + item);
          break;

        case "hour":
          var t = 'pad(this.visited_at.getUTCHours()) + ":" + pad(this.visited_at.getUTCMinutes())';
          key_generator.push(t);
          fields.push("hour: " + t);
          break;

        case "day":
          var t = 'this.visited_at.getUTCFullYear().toString() + pad(this.visited_at.getUTCMonth() + 1) + pad(this.visited_at.getUTCDate())';
          key_generator.push(t);
          fields.push("day: " + t);
          break;

        default:
          return response.json({errors: "Invalid item: " + item}, 403);
      }
    }

    if(fields.length === 0) {
      return response.json({errors: "Invalid item: " + item}, 403);
    }

    models.Visit.collection.mapReduce(
      "function() {" +
      "  var pad = function(n) {" +
      "    n = n.toString();" +
      "    return n.length == 1 ? ('0' + n) : n;" +
      "  };" +
      "  emit(" + key_generator.join(" + '|' + ") + ", { " + fields.join(",") + ", n: 1 })" +
      "}",
      "function(k, v) {" +
      "  var acc = v[0], i = 1, l = v.length;" +
      "  while(i < l) {" +
      "    acc.n += v[i].n;" +
      "    i++;" +
      "  };" +
      "  return acc;" +
      "}",
      {
        query: { website: website._id },
        out: { inline: 1 },
      },
      function(error, results) {
        if(error)
          return response.send(error, 500)

        results = results.map(function(item) {
          var value = item.value;
          var row = { count: value.n, key: value };
          delete row.key.n;
          return row;
        });
        response.json(results);
    });
  });
});

app.listen(parseInt(process.env["PORT"] || 3000))
