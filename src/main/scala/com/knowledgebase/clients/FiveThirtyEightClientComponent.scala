package com.knowledgebase.clients

import com.knowledgebase.thrift.{PollEntry, PollResource}
import com.knowledgebase.utils.Defaults.{FTE_HOST, FTE_PORT, POLLS_PAGE, POLLS_PRESIDENT_PRIMARY, NUM_RELEVANT_POLLS}
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
      println(colMap)
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
        .sortWith((poll1, poll2) => dateComparator(poll1.startDate, poll2.startDate))
        .take(NUM_RELEVANT_POLLS)
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
          )).sortWith((entry1, entry2) => entry1.percentage > entry2.percentage)
        )
      }
    }

    private case class ColMap(cycle: Int, pollster: Int, fteGrade: Int, sampleSize: Int, officeType: Int, startDate: Int, endDate: Int, stage: Int, party: Int, candidate: Int, percentage: Int, state: Int, questionId: Int)

    private def buildColMap(header: Array[String]) = ColMap(
      cycle = header indexOf "cycle",
      pollster = header indexOf "pollster",
      fteGrade = header indexOf "fte_grade",
      sampleSize = header indexOf "sample_size",
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

    private def dateComparator(date1: String, date2: String): Boolean = {
      val splitDate1 = date1.split("/")
      val splitDate2 = date2.split("/")

      if (splitDate1(2).toInt != splitDate2(2).toInt) {
        splitDate1(2).toInt > splitDate2(2).toInt
      } else if (splitDate1.head.toInt != splitDate2.head.toInt) {
        splitDate1.head.toInt > splitDate2.head.toInt
      } else {
        splitDate1(1).toInt > splitDate2(1).toInt
      }
    }

  }
}
