import akka.actor.{Actor, ActorLogging, ActorSystem, ClassicActorSystemProvider, Props}
import akka.stream.{Materializer, SystemMaterializer}

import java.util.Date

import spray.json._

case class Payment(paymentId: String, tenantId: String, txnDate: Long, paymentDetails: String)

trait PaymentJsonProtocol extends DefaultJsonProtocol {
  implicit val paymentJSON = jsonFormat4(Payment)
}

object PaymentService {
  case class SanctionCheck(payment: Payment)
  case class AmlCheck(payment: Payment)
  case class FraudCheck(payment: Payment)
  case class FundsControlCheck(payment: Payment)
  case class LiquidityControlCheck(payment: Payment)
  case class AccountPostingsCheck(payment: Payment)

  val SUCCESS: String = "SUCCESS"
  val FAILURE: String = "FAILURE"
}

class Mock3PActor extends Actor with ActorLogging {
  import PaymentService._

  override def receive: Receive = {
    case SanctionCheck(payment) =>
      log.info("SanctionCheck Service called for: {}", payment)
      sender() ! payment

    case AmlCheck(payment) =>
      log.info("AmlCheck Service called for: {}", payment)
      sender() ! payment

    case FraudCheck(payment) =>
      log.info("FraudCheck Service called for: {}", payment)
      sender() ! payment

    case FundsControlCheck(payment) =>
      log.info("FundsControlCheck Service called for: {}", payment)
      sender() ! payment

    case LiquidityControlCheck(payment) =>
      log.info("LiquidityControlCheck Service called for: {}", payment)
      sender() ! payment

    case AccountPostingsCheck(payment) =>
      log.info("AccountPostingsCheck Service called for: {}", payment)
      sender() ! payment

    case _ =>
      log.info("Service unavailable")
      sender() ! FAILURE
  }

}
