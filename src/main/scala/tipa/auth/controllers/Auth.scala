package tipa.auth.controllers

import play.api.mvc._

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee._
import scala.concurrent.Future
import play.api.libs.streams._
import akka.stream.Materializer

trait Auth extends Controller {
  self: Controller with AuthConfig =>
    implicit def materializer: Materializer

  def authenticated(f: => UserType => EssentialAction) = EssentialAction {
    implicit request =>
      Accumulator.flatten {
        resolveUser(request) map {
          _.fold(
            (result: Result) => Accumulator.done(result),
            (user: UserType) =>
              f(user)(request))
        }
      }
  }

  def maybeAuthenticated(f: => Option[UserType] => EssentialAction) = EssentialAction {
    implicit request =>
      Accumulator.flatten {
        resolveUser(request) map {
          _.fold(
            (result: Result) => f(None)(request),
            (user: UserType) =>
              f(Some(user))(request))
        }
      }

  }

  type AuthorizationRequest = UserType => Future[Either[Result, Boolean]]

  def authorized(authorizationRequest: AuthorizationRequest)(f: => UserType => EssentialAction) = authenticated { user =>
    EssentialAction { implicit request =>
      Accumulator.flatten {
        authorizationRequest(user)
          .map {
            _.fold((result: Result) => Accumulator.done(result),
              auth => {
                if (auth) {
                  f(user)(request)
                } else {
                  Accumulator.done(onUnauthorized(request))
                }
              })
          }
      }
    }
  }

  def simpleAuthorizationRequest(f: => UserType => Boolean): AuthorizationRequest = (user: UserType) => Future.successful(Right(f(user)))

  type AuthenticationResolver = RequestHeader => Future[Either[Option[Result], UserType]]

  private def composeAuthenticationResolvers(resolver1: AuthenticationResolver, resolver2: AuthenticationResolver) = { request: RequestHeader =>
    resolver1(request) flatMap {
      _.fold(maybeResult => maybeResult match {
        case Some(result) => Future.successful(Left(Some(result)))
        case None         => Future.failed(new Exception)
      },
        user => Future.successful(Right(user)))

    } fallbackTo { resolver2(request) }
  }

  def resolveUser(request: RequestHeader): Future[Either[Result, UserType]] = {
    authenticationResolvers.reduceLeft(composeAuthenticationResolvers)(request)
      .map {
        _.left.map { maybeResult =>
          maybeResult.getOrElse(authenticationFailed(request))
        }
      }
  }
}