package mesosphere.marathon
package poc.repository

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{ Keep, Sink, Source }
import java.util.UUID
import mesosphere.marathon.poc.state.{ RunSpec, RunSpecRef, Instance }
import org.scalatest.Inside

class StateAuthorityTest extends AkkaUnitTestLike with Inside {
  import StateAuthority.{ StateAuthorityInputEvent, CommandRequest, Effect, StateCommand }
  val instanceId = UUID.fromString("deadbeef-c011-0123-4567-89abcdefffff")
  "invalid commands are rejected right away" in {
    val requestId = 1011
    Given("a fresh instance of Marathon")
    val (input, result) = Source.queue[StateAuthorityInputEvent](16, OverflowStrategy.fail)
      .via(StateAuthority.commandProcessorFlow)
      .toMat(Sink.queue())(Keep.both)
      .run

    When("I submit a command to add a task for a RunSpec that does not exist")
    input.offer(CommandRequest(
      requestId,
      StateCommand.AddInstance(Instance(
        instanceId,
        RunSpecRef("/lol", "blue"),
        incarnation = 1L,
        goal = Instance.Goal.Running))))

    And("the failure gets published")
    inside(result.pull().futureValue) {
      case Some(result: Effect.CommandFailure) =>
        result.requestId shouldBe requestId
        result.rejection.reason shouldBe (s"No runSpec /lol#blue")
    }

    When("we close the stream")
    input.complete()

    Then("no further events are generated")
    result.pull().futureValue shouldBe None
  }
}
