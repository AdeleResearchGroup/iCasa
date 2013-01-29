package controllers

import play.api._
import libs.EventSource
import libs.json.Json._
import play.api.mvc._
import play.api.Play.current
import java.io.File

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index()).withHeaders(
      "Access-Control-Allow-Origin" -> "*",
      "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
      "Access-Control-Expose-Headers" -> "X-Cache-Date, X-Atmosphere-tracking-id",
      "Access-Control-Allow-Headers" ->"Origin, Content-Type, X-Atmosphere-Framework, X-Cache-Date, X-Atmosphere-Tracking-id, X-Atmosphere-Transport",
      "Access-Control-Max-Age"-> "-1"
    )
  }

  val MAP_DIRECTORY: String = "maps";

  def uploadMap = Action(parse.multipartFormData) { request =>
    request.body.file("picture").map { picture =>
      import java.io.File
      val fileName = picture.filename
      val contentType = picture.contentType
      picture.ref.moveTo(Play.getFile(MAP_DIRECTORY + "/" + fileName))
      Ok("File uploaded")
    }.getOrElse {
      Redirect(routes.Application.index).flashing(
        "error" -> "Missing file"
      )
    }
  }

  def getMaps() = Action {
    Ok(views.html.maps());
  }

  def getGrid() = Action {
      Ok(views.html.grid());
    }

  def getMap(file: String) = Action {
    val fileToServe = new File(Play.application.getFile(MAP_DIRECTORY), file);
    val defaultCache = Play.configuration.getString("assets.defaultCache").getOrElse("max-age=3600")
    Ok.sendFile(fileToServe, inline = true).withHeaders(CACHE_CONTROL -> defaultCache);
  }
}