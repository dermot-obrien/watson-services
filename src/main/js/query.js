// Install API via:
// npm install --save watson-developer-cloud

var DiscoveryV1 = require('watson-developer-cloud/discovery/v1');

var version = '2018-03-05';

// Analysis instance (not trained)
var analysis_username = 'e6a886ca-4664-4528-b131-d310a0e1979d';
var analysis_password = 'dsHy2MMTpD3c';
var analysis_environment_id = '393cb64f-9481-4b6b-b617-79bb52b411b8';
var analysis_collection_id = '68703f71-f69e-4bd5-af92-813d9eda4710';

// Use the analysis instance.
var username = analysis_username;
var password = analysis_password;
var environment_id = analysis_environment_id;
var collection_id = analysis_collection_id;

var discovery = new DiscoveryV1({
  version: version,
  username: username,
  password: password
});

var query = 'What are the recommended battery charger specifications?'

var fs = require('fs');

// Do a query. See https://www.ibm.com/watson/developercloud/discovery/api/v1/node.html?node#query
discovery.query({ environment_id: environment_id, collection_id: collection_id, query: query, passages: true, return_fields: [ "name" ] }, function(error, data) {
  fs.writeFile("results.json", JSON.stringify(data, null, 2),function(err) {
    if(err) {
      return console.log(err);
    }
  });
});