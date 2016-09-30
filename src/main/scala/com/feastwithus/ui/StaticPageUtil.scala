package com.feastwithus.ui

import java.sql.Date
import java.util.Base64

import com.feastwithus.repo.Volunteer

/**
  * Created by adav on 16/09/2016.
  */
object StaticPageUtil {

  def htmlPageWithBody(title: String)(body: => String): String = {
    s"""
       |<!DOCTYPE html>
       |<html lang="en">
       |  <head>
       |    <meta charset="utf-8">
       |    <meta http-equiv="X-UA-Compatible" content="IE=edge">
       |    <meta name="viewport" content="width=device-width, initial-scale=1">
       |
       |    <title>$title</title>
       |
       |    <!-- Bootstrap -->
       |    <link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet">
       |
       |    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
       |    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
       |    <!--[if lt IE 9]>
       |      <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
       |      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
       |    <![endif]-->
       |  </head>
       |  <body>
       |    <div class="container">
       |      $body
       |    </div>
       |    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
       |    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
       |  </body>
       |</html>
    """.stripMargin
  }

  def generatePublicWhosComingHtml(
                                    thisWeekVolunteers: List[Volunteer],
                                    thisWeekDate: String,
                                    nextWeekVolunteers: List[Volunteer],
                                    nextWeekDate: String
                                  ) = htmlPageWithBody("FEAST! Who's Coming") {
    s"""
       |    <h1>FEAST! Participants</h1>
       |    ${generateEventTablePublicHtml(thisWeekVolunteers, thisWeekDate, makeTitle(0))}
       |    ${generateEventTablePublicHtml(nextWeekVolunteers, nextWeekDate, makeTitle(1))}
     """.stripMargin
  }

  def generateAdminHtml(
                         volunteers: List[(String, List[Volunteer])],
                         dates: List[Date],
                         topVolunteers: Option[List[(String, Int)]] = None
                       ) = htmlPageWithBody("FEAST!bot Admin") {

    val tables = volunteers.zipWithIndex.map { case ((humanDate, volunteersInWeek), i) =>

      if (volunteers.length > 1) generateEventTablePrivateHtml(volunteersInWeek, humanDate, makeTitle(i))
      else generateEventTablePrivateHtml(volunteersInWeek, humanDate)

    }

    val dateSelectorHtmlRows = dates.map(d => s"""<li><a target="_blank" href="/admin/${d.toString}">${DateUtils.formatHumanDate(d.toLocalDate)}</a></li>""")

    val topVolunteersHtml = topVolunteers match {
      case Some(topPeople) => {
        val topVolunteerRowsHtml = topPeople.map {case (name, count) => s"<tr><td>$name</td><td>$count</td></tr>"}
        s"""
           |<hr>
           |<div class="panel panel-primary" style="width: 400px;">
           |  <div class="panel-heading">Top Volunteers</div>
           |
           |  <table class="table">
           |    <thead> <tr> <th> Name</th> <th>Num of Registrations</th> </tr>
           |       </thead>
           |       <tbody>
           |       ${topVolunteerRowsHtml.mkString("\n")}
           |       </tbody>
           |  </table>
           |</div>
         """.stripMargin
      }
      case None => ""
    }

    s"""
       |    <h1><a href="/admin">FEAST! Admin</a></h1>
       |    ${tables.mkString("\n")}
       ||
       |    <div class="dropdown">
       |      <button class="btn btn-default dropdown-toggle" type="button" id="dropdownMenu1" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
       |        Show volunteers for week
       |        <span class="caret"></span>
       |      </button>
       |      <ul class="dropdown-menu" aria-labelledby="dropdownMenu1">
       |        <li><a href="/admin">Back to FEAST!bot Admin Home</a></li>
       |        <li role="separator" class="divider"></li>
       |        ${dateSelectorHtmlRows.mkString("\n")}
       |      </ul>
       |    </div>
       |
       |    $topVolunteersHtml
       |
       |
     """.stripMargin
  }


  def generateEventTablePublicHtml(volunteers: List[Volunteer], headerDateHumanFormat: String, headerTitle: String = "Week:") = {

    val tableRowHtml = volunteers
      .zipWithIndex
      .map { case (v, i) =>
        s"""<tr> <th scope="row">${i + 1}</th> <td>${v.firstname}</td> <td>${v.surname}</td> </tr>"""
      } mkString "\n"

    s"""
       |<h3>$headerTitle <small>$headerDateHumanFormat</small></h3>
       |
       |    <table class="table table-striped">
       |       <thead> <tr> <th></th> <th>First Name</th> <th>Last Name</th> </tr>
       |       </thead>
       |       <tbody>
       |       $tableRowHtml
       |       </tbody>
       |    </table>
     """.stripMargin
  }

  def generateEventTablePrivateHtml(volunteers: List[Volunteer], headerDateHumanFormat: String, headerTitle: String = "Week:") = {

    val tableRowHtml = volunteers
      .zipWithIndex
      .map { case (v, i) =>
        val deleteToken = makeUnregisterToken(v)

        s"""<tr>
            |  <th scope="row">${i + 1}</th>
            |  <td>${v.firstname}</td>
            |  <td>${v.surname}</td>
            |  <td><a href="tel:${v.telephone}">${v.telephone}</a></td>
            |  <td><a href="mailto:${v.email}">${v.email}</a></td>
            |  <td><a target="_blank" href="/unregister/$deleteToken">Remove</a></td>
            |</tr>""".stripMargin
      } mkString "\n"

    val header = s"""<h3>$headerTitle <small>$headerDateHumanFormat</small></h3>"""

    s"""
       |    $header
       |
       |    <table class="table table-striped">
       |       <thead> <tr> <th></th> <th>First Name</th> <th>Last Name</th> <th>Telephone</th> <th>Email</th> <th></th> </tr>
       |       </thead>
       |       <tbody>
       |       $tableRowHtml
       |       </tbody>
       |    </table>
     """.stripMargin
  }

  def makeTitle(i: Int): String = i match {
    case 0 => "This Week"
    case 1 => "Next Week"
    case n => s"In $n Weeks"
  }

  def makeUnregisterToken(v: Volunteer): String =
    Base64.getUrlEncoder.encodeToString((v.email+","+v.id.get).getBytes("UTF-8"))

  def makeUnregisterLink(v: Volunteer): String =
    "https://feastwithus.org.uk/unregister/" + makeUnregisterToken(v)

}
