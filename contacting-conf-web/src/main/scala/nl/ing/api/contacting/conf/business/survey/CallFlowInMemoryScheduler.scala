package nl.ing.api.contacting.conf.business.survey

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import nl.ing.api.contacting.conf.business.callflow.CallFlowTriggerAndProcessor
import nl.ing.api.contacting.conf.business.callflow.CallFlowTriggerAndProcessor.CallFlowRequestData
import nl.ing.api.contacting.util.ResultT
import nl.ing.api.contacting.util.syntaxf.FSyntax.FOps

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

/**
 * @author Ayush M
 */
class CallFlowInMemoryScheduler(callFlowClient: CallFlowTriggerAndProcessor) extends LazyLogging {

  def scheduleCallFlow(requestData: CallFlowRequestData, delayInSeconds: Long): ResultT[Unit] = {
    for {
      _ <- IO.sleep(Duration.create(delayInSeconds, TimeUnit.SECONDS)).adaptErrorT()
      res <- callFlowClient.execute(requestData)
    } yield res
  }
}
