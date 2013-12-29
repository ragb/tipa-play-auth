package tipa.auth.controllers

import play.api.mvc._

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee._
import scala.concurrent.Future



trait Auth extends Controller {
  self: Controller with AuthConfig =>

  def authenticated(f: => UserType => EssentialAction) = EssentialAction {
    implicit request =>
        Iteratee.flatten[Array[Byte], SimpleResult] {
          resolveUser(request) map {
            _.fold(
              (result: SimpleResult) => Done(result, Input.Empty),
              (user: UserType) =>
                f(user)(request))
          }
        }
  }

  def maybeAuthenticated(f: => Option[UserType] => EssentialAction) = EssentialAction {
    implicit request =>
        Iteratee.flatten[Array[Byte], SimpleResult] {
          resolveUser(request) map {
            _.fold(
              (result: SimpleResult) => f(None)(request),
              (user: UserType) =>
                f(Some(user))(request))
          }
        }

  }

  type AuthorizationRequest = UserType => Future[Either[SimpleResult, Boolean]]

  def authorized(authorizationRequest: AuthorizationRequest)(f: => UserType => EssentialAction) = authenticated { user =>
    EssentialAction { implicit request =>
      Iteratee.flatten {
        authorizationRequest(user)
          .map {
            _.fold((result : SimpleResult) => Done(result, Input.Empty),
              auth => {
                if (auth) {
                  f(user)(request)
                } else {
                  Done(onUnauthorized(request), Input.Empty)
                }
              })
          }
      }
    }
  }

  def simpleAuthorizationRequest(f : => UserType => Boolean) : AuthorizationRequest = (user : UserType) => Future.successful(Right(f(user)))

      type AuthenticationResolver = RequestHeader => Future[Either[Option[SimpleResult], UserType]]

      private def composeAuthenticationResolvers(resolver1 : AuthenticationResolver, resolver2 : AuthenticationResolver) = { request : RequestHeader =>
        resolver1(request) flatMap {_.fold(maybeResult => maybeResult match {
          case Some(result) => Future.successful(Left(Some(result)))
          case None => Future.failed(new Exception)
        },
        user => Future.successful(Right(user)))
          
        } fallbackTo {resolver2(request)}
      }
      
      def resolveUser(request : RequestHeader) : Future[Either[SimpleResult, UserType]] = {
        authenticationResolvers.reduceLeft(composeAuthenticationResolvers)(request)
        .map {_.left.map{ maybeResult =>
          maybeResult.getOrElse(authenticationFailed(request))
        }
        }
     }
}