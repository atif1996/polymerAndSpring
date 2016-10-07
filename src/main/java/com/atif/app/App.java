package com.atif.app;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.sql.SQLException;
import static spark.Spark.*;


/**
 * Main application web server with route handler
 * Went with the spark framework.  This is only a REST service.
 */
public class App
{
  public static void main(String[] args) throws SQLException, ClassNotFoundException {
      DataAccessObj dao = new DataAccessObj();

      exception(Exception.class, (e, req, res) -> e.printStackTrace()); // print all exceptions
      staticFileLocation("/public");
      //externalStaticFileLocation("/Users/atifm/src/cfa_crud/server/src/main/resources/public");

      port(9999);

      Gson gson = new Gson();

      get("/users", (request, response) -> {   //get user
          return dao.getUsers();
      }, gson::toJson);

      get("/users/:id", (request, response) -> {   //get user
          int id = Integer.parseInt(request.params("id"));
          if (id <= 0) {
              response.status(404);
              return "404";
          }
          else {
              if (dao.hasUser(id)) {
                  response.status(200);
                  return dao.getUser(id);
              }
              else
              {
                  response.status(410); // gone
                  return "410";
              }
          }
      }, gson::toJson);

      post("/users/new", (request, response) -> {  // Create user
          JsonElement jelement = new JsonParser().parse(request.body());
          JsonObject  jobject = jelement.getAsJsonObject();
          if ( (!jobject.has("firstName") || !jobject.has("lastName") || !jobject.has("age"))) {
              response.status(400); //Bad Request
              return "400"; //value;
          }

          String first = jobject.get("firstName").getAsString();
          String last = jobject.get("lastName").getAsString();
          int age = jobject.get("age").getAsInt();

          int newid = dao.insertUser(first, last, age);
          if (newid != -1) {
              response.status(201);  // New resource created
          }
          else {
              response.status(500);  // Server error
              return "500";
          }

          String result = "{\"newid\":" + newid + '}';
          return result;
      });

      put("/users/:id", (request, response) -> {    // Update User
          int id = Integer.parseInt(request.params("id"));

          if (!dao.hasUser(id)) {
              response.status(404); // not found
              return "404";
          }

          // iron-form doesn't support sending parameter values in the body
          // Looks like they dont have proper put support.  Workaround is to
          // values from the url encoded parameters
          String first = request.queryParams("firstName");
          String last = request.queryParams("lastName");
          String ageString = request.queryParams("age");
          int age = Integer.parseInt(ageString);

          /*
          JsonElement jelement = new JsonParser().parse(request.body());
          JsonObject  jobject = jelement.getAsJsonObject();
          if ( (!jobject.has("firstName") || !jobject.has("lastName") || !jobject.has("age"))) {
              response.status(400); //Bad Request
              return "400"; //value;
          }

          String first = jobject.get("firstName").toString();
          String last = jobject.get("lastName").toString();
          int age = jobject.get("age").getAsInt();
          */

          UserRecord updatedRecord = new UserRecord(id, first, last, age);
          if (dao.updateUser( updatedRecord )) {
              response.status(200);
              return "200";
          }
          else {
              response.status(500);
              return "500";
          }
      });

      delete("/users/:id", (request, response) -> {  // Delete user
          int id = Integer.parseInt(request.params("id"));
          if (!dao.hasUser(id)) {
              response.status(404); // not found
              return "404";
          }

          if (dao.deleteUser(id)) {
              response.status(200);
          }
          else {
              response.status(500);  //server error
          }
          return "";
      });


      after((req, res) -> {
          System.out.println(req.uri());
          res.type("application/json");                      // Making everything a json type
          //res.header("Access-Control-Allow-Origin", "*");  // Allows COR for testing
      });

  }
}
