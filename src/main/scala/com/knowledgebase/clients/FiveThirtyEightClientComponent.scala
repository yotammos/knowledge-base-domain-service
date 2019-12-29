package com.knowledgebase.clients

import com.knowledgebase.thrift.{PollEntry, PollResource}
import com.knowledgebase.utils.Defaults.{FTE_HOST, FTE_PORT, POLLS_PAGE, POLLS_PRESIDENT_PRIMARY}
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.util.{Future, Return, Throw, Try}

trait FiveThirtyEightClientComponent {

  def fiveThirtyEightClient: FiveThirtyEightClient

  class FiveThirtyEightClient {
    private val host = FTE_HOST
    private val client: Service[Request, Response] = Http.client.withTls(host) newService s"$host:$FTE_PORT"

    def fetchPresidentPrimaryPollingData(): Future[Seq[PollResource]] =
      fetchPollingData(POLLS_PRESIDENT_PRIMARY)

    private def fetchPollingData(fileName: String): Future[Seq[PollResource]] =
      client(
        Request(Method.Get, s"/$POLLS_PAGE/$fileName")
      ) map { response =>
        if (response.status == Status.Ok) {
          buildPollingResources(response.getContentString())
        } else {
          throw new Exception("failed fetching polling data, error = " + response.getContentString())
        }
      } handle {
        case t: Throwable =>
          println("failed getting polling data, error = " + t.getMessage)
          throw t
      }

    private def buildPollingResources(content: String): Seq[PollResource] = {
      val lines = content split "\n" // first line is header
      val header = lines.head
      val colMap = buildColMap(header split ",")
      val rawPolls = lines.tail.map(_ split ",")
        .groupBy(row => row(colMap.questionId))
        .values
        .toSeq

      rawPolls.flatMap(rawPoll => buildPollResource(rawPoll, colMap) match {
        case Return(resource) => Seq(resource)
        case Throw(error) =>
          println("failed parsing resource, error = " + error.getMessage)
          Seq.empty[PollResource]
      })
    }

    private def buildPollResource(rawResource: Array[Array[String]], colMap: ColMap): Try[PollResource] = {
      val firstLine = rawResource.head
      Try {
        PollResource(
          cycle = firstLine(colMap.cycle).toInt,
          pollster = firstLine(colMap.pollster),
          fteGrade = firstLine(colMap.fteGrade),
          sampleSize = firstLine(colMap.sampleSize).toInt,
          officeType = firstLine(colMap.officeType),
          startDate = firstLine(colMap.startDate),
          endDate = firstLine(colMap.endDate),
          stage = firstLine(colMap.stage),
          state = {
            val maybeState = firstLine(colMap.state)
            if (maybeState.nonEmpty)
              Option(maybeState)
            else
              None
          },
          entries = rawResource.map(line => PollEntry(
            party = line(colMap.party),
            candidate = line(colMap.candidate),
            percentage = line(colMap.percentage).toDouble
          ))
        )
      }
    }

    private case class ColMap(cycle: Int, pollster: Int, fteGrade: Int, sampleSize: Int, officeType: Int, startDate: Int, endDate: Int, stage: Int, party: Int, candidate: Int, percentage: Int, state: Int, questionId: Int)

    private def buildColMap(header: Array[String]) = ColMap(
      cycle = header indexOf "cycle",
      pollster = header indexOf "pollster",
      fteGrade = header indexOf "fte_grade",
      sampleSize = header indexOf "sampleSize",
      officeType = header indexOf "office_type",
      startDate = header indexOf "start_date",
      endDate = header indexOf "end_date",
      stage = header indexOf "stage",
      party = header indexOf "party",
      candidate = header indexOf "answer",
      percentage = header indexOf "pct",
      state = header indexOf "state",
      questionId = header indexOf "question_id"
    )
  }
}
