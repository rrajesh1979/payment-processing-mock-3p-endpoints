import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.util.{Failure, Success}

import spray.json._

object HttpClientDemo extends App with PaymentJsonProtocol {
  implicit val system = ActorSystem("ConnectionLevel")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher


  val connectionFlow = Http().outgoingConnection("www.google.com")

  def oneOffRequest(request: HttpRequest) =
    Source.single(request).via(connectionFlow).runWith(Sink.head)

  oneOffRequest(HttpRequest()).onComplete {
    case Success(response) => println(s"Got successful response: $response")
    case Failure(ex) => println(s"Sending the request failed: $ex")
  }

  val paymentRequest = Payment("PAY-100", "DEMO-TENANT", System.nanoTime(), "Request from HTTP Client")

  val paymentHttpRequest =
    HttpRequest(
      HttpMethods.POST,
      uri = Uri("/api/sanctioncheck"),
      entity = HttpEntity(
        ContentTypes.`application/json`,
        paymentRequest.toJson.prettyPrint
      )
    )

  val connectionFlowPayment = Http().outgoingConnection("localhost", 8080)

  def oneOffPaymentRequest(request: HttpRequest) =
    Source.single(request).via(connectionFlowPayment).runWith(Sink.head)

  oneOffPaymentRequest(paymentHttpRequest).onComplete {
    case Success(response) => println(s"Got successful response: $response")
    case Failure(ex) => println(s"Sending the request failed: $ex")
  }

}
