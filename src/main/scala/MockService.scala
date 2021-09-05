import PaymentService._
import akka.actor.{ActorSystem, ClassicActorSystemProvider, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.pattern.ask
import akka.stream.{Materializer, SystemMaterializer}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.language.postfixOps
import spray.json._

import scala.util.Success

object MockService extends App with PaymentJsonProtocol {
  implicit val paymentMockSystem: ActorSystem = ActorSystem("PaymentMockSystem", ConfigFactory.load().getConfig("mockServiceConfig"))
  implicit def matFromSystem(implicit provider: ClassicActorSystemProvider): Materializer =
    SystemMaterializer(provider.classicSystem).materializer

  import paymentMockSystem.dispatcher

  // directives
  import akka.http.scaladsl.server.Directives._

  val paymentMockActor = paymentMockSystem.actorOf(Props[Mock3PActor], "paymentMockService")

  implicit val timeout: Timeout = Timeout(2 seconds)

  val paymentService = {
    pathPrefix("api") {
      get {
        val paymentResponse = Payment("DEMO-PAY", "DEMO-TENANT", System.nanoTime(), "Nothing")
        val paymentHttpEntity = HttpEntity(
            ContentTypes.`application/json`,
            paymentResponse.toJson.prettyPrint
          )
        complete(paymentHttpEntity)
      } ~
      path("sanctioncheck") {
        (post & pathEndOrSingleSlash & extractRequest & extractLog) { (request, log) =>
          val entity = request.entity
          val strictEntityFuture = entity.toStrict(2 seconds)
          val paymentRequestFuture = strictEntityFuture.map { strictEntity =>
            strictEntity.data.utf8String.parseJson.convertTo[Payment]
          }

          paymentRequestFuture.onComplete {
            case Success(payment) =>
              log.info("Payment object from request: {}", payment)
              val paymentFuture: Future[Payment] = (paymentMockActor ? SanctionCheck(payment)).mapTo[Payment]
              paymentFuture.onComplete(entity =>
                log.info("Response from actor: {}", entity)
              )
          }

          complete(paymentRequestFuture
            .map(_ => StatusCodes.OK)
            .recover {
              case _ => StatusCodes.InternalServerError
            }
          )
        }
      } ~
      path("amlcheck") {
        (post & pathEndOrSingleSlash & extractRequest & extractLog) { (request, log) =>
          val entity = request.entity
          val strictEntityFuture = entity.toStrict(2 seconds)
          val paymentRequestFuture = strictEntityFuture.map { strictEntity =>
            strictEntity.data.utf8String.parseJson.convertTo[Payment]
          }

          paymentRequestFuture.onComplete {
            case Success(payment) =>
              log.info("Payment object from request: {}", payment)
              val paymentFuture: Future[Payment] = (paymentMockActor ? AmlCheck(payment)).mapTo[Payment]
              paymentFuture.onComplete(entity =>
                log.info("Response from actor: {}", entity)
              )
          }

          complete(paymentRequestFuture
            .map(_ => StatusCodes.OK)
            .recover {
              case _ => StatusCodes.InternalServerError
            }
          )
        }
      } ~
        path("fraudcheck") {
          (post & pathEndOrSingleSlash & extractRequest & extractLog) { (request, log) =>
            val entity = request.entity
            val strictEntityFuture = entity.toStrict(2 seconds)
            val paymentRequestFuture = strictEntityFuture.map { strictEntity =>
              strictEntity.data.utf8String.parseJson.convertTo[Payment]
            }

            paymentRequestFuture.onComplete {
              case Success(payment) =>
                log.info("Payment object from request: {}", payment)
                val paymentFuture: Future[Payment] = (paymentMockActor ? FraudCheck(payment)).mapTo[Payment]
                paymentFuture.onComplete(entity =>
                  log.info("Response from actor: {}", entity)
                )
            }

            complete(paymentRequestFuture
              .map(_ => StatusCodes.OK)
              .recover {
                case _ => StatusCodes.InternalServerError
              }
            )
          }
        } ~
        path("fundscheck") {
          (post & pathEndOrSingleSlash & extractRequest & extractLog) { (request, log) =>
            val entity = request.entity
            val strictEntityFuture = entity.toStrict(2 seconds)
            val paymentRequestFuture = strictEntityFuture.map { strictEntity =>
              strictEntity.data.utf8String.parseJson.convertTo[Payment]
            }

            paymentRequestFuture.onComplete {
              case Success(payment) =>
                log.info("Payment object from request: {}", payment)
                val paymentFuture: Future[Payment] = (paymentMockActor ? FundsControlCheck(payment)).mapTo[Payment]
                paymentFuture.onComplete(entity =>
                  log.info("Response from actor: {}", entity)
                )
            }

            complete(paymentRequestFuture
              .map(_ => StatusCodes.OK)
              .recover {
                case _ => StatusCodes.InternalServerError
              }
            )
          }
        } ~
        path("liquiditycheck") {
          (post & pathEndOrSingleSlash & extractRequest & extractLog) { (request, log) =>
            val entity = request.entity
            val strictEntityFuture = entity.toStrict(2 seconds)
            val paymentRequestFuture = strictEntityFuture.map { strictEntity =>
              strictEntity.data.utf8String.parseJson.convertTo[Payment]
            }

            paymentRequestFuture.onComplete {
              case Success(payment) =>
                log.info("Payment object from request: {}", payment)
                val paymentFuture: Future[Payment] = (paymentMockActor ? LiquidityControlCheck(payment)).mapTo[Payment]
                paymentFuture.onComplete(entity =>
                  log.info("Response from actor: {}", entity)
                )
            }

            complete(paymentRequestFuture
              .map(_ => StatusCodes.OK)
              .recover {
                case _ => StatusCodes.InternalServerError
              }
            )
          }
        } ~
        path("accountpostingscheck") {
          (post & pathEndOrSingleSlash & extractRequest & extractLog) { (request, log) =>
            val entity = request.entity
            val strictEntityFuture = entity.toStrict(2 seconds)
            val paymentRequestFuture = strictEntityFuture.map { strictEntity =>
              strictEntity.data.utf8String.parseJson.convertTo[Payment]
            }

            paymentRequestFuture.onComplete {
              case Success(payment) =>
                log.info("Payment object from request: {}", payment)
                val paymentFuture: Future[Payment] = (paymentMockActor ? AccountPostingsCheck(payment)).mapTo[Payment]
                paymentFuture.onComplete(entity =>
                  log.info("Response from actor: {}", entity)
                )
            }

            complete(paymentRequestFuture
              .map(_ => StatusCodes.OK)
              .recover {
                case _ => StatusCodes.InternalServerError
              }
            )
          }
        }
    }
  }

  Http().newServerAt("localhost", 8080).bindFlow(paymentService)

}
