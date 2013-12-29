package tipa.auth.controllers
import play.api.mvc._


trait LoginLogout {
  self: Controller with AuthConfig =>

    val usernameTag = "username"
      def userCookieAge = 3600 // 1 houre

      
  def gotoLoginSucceeded(userId: Id)(implicit request: RequestHeader): Result = {
    gotoLoginSucceeded(userId, loginSucceeded(request))
  }

  def gotoLoginSucceeded(userId: Id, result: => Result)(implicit request : RequestHeader) : Result = {
    result.withCookies(Cookie(usernameTag, serializeUserId(userId), Some(userCookieAge), httpOnly = false))
  }

  def gotoLogoutSucceeded(implicit request: RequestHeader): Result = {
    gotoLogoutSucceeded(logoutSucceeded(request))
  }

  def serializeUserId(id : Id) : String
  def dessirealizeUserId(s : String) : Id
  
  def retrieveUserId(request : RequestHeader) = request.cookies.get(usernameTag).map { cookie =>
    dessirealizeUserId(cookie.value)
    }
  
  def gotoLogoutSucceeded(result: => Result)(implicit request: RequestHeader): Result = {
    result.discardingCookies(DiscardingCookie(usernameTag))
  }
  
    def loginSucceeded(implicit request : RequestHeader) : Result
  def logoutSucceeded(implicit request : RequestHeader) : Result

}
