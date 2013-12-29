package tipa.auth.controllers

import play.api.mvc._

trait HTTPAuthConfig  {
 self : Controller with AuthConfig =>
   
    val realm : String
    
def retrieveHTTPCredentials(request : RequestHeader)  = {
    request.headers.get("Authorization").flatMap { authorization =>
    authorization.split(" ").drop(1).headOption.flatMap { encoded =>
      ((new String(org.apache.commons.codec.binary.Base64.decodeBase64(encoded.getBytes))).split(":")).toList match {
        case u :: p :: Nil => Some((u, p))
        case _ => None
      }
    } 
} 
}

    def authenticationRequired(implicit request : RequestHeader) : Result = {
          Unauthorized.withHeaders("WWW-Authenticate" -> s"""Basic realm=${realm}""")
}
    }
