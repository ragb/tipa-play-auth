package tipa.auth.controllers

import play.api.mvc._
import scala.concurrent.Future
import play.api.Play


trait AuthConfig {
self : Controller with Auth =>
  

    type Id


type UserType

def authenticationFailed(request : RequestHeader) : SimpleResult
  
  def authenticationResolvers : Seq[AuthenticationResolver]
  
  def onUnauthorized(request : RequestHeader) : SimpleResult
  
}