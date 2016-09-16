package com.knoldus.ui

import com.knoldus.repo.Volunteer

/**
  * Created by adav on 16/09/2016.
  */
object StaticPageUtil {

  def generatePublicWhosComingHtml(
                                    thisWeekVolunteers: List[Volunteer],
                                    thisWeekDate: String,
                                    nextWeekVolunteers: List[Volunteer],
                                    nextWeekDate: String
                                  ) = {
    val htmlThisWeekTable =
      thisWeekVolunteers.zipWithIndex.map { case (v, i) => s"""<tr> <th scope="row">${i + 1}</th> <td>${v.firstname}</td> <td>${v.surname}</td> </tr>""" } mkString "\n"

    val htmlNextWeekTable =
      nextWeekVolunteers.zipWithIndex.map { case (v, i) => s"""<tr> <th scope="row">${i + 1}</th> <td>${v.firstname}</td> <td>${v.surname}</td> </tr>""" } mkString "\n"


    s"""
       |<!DOCTYPE html>
       |<html lang="en">
       |  <head>
       |    <meta charset="utf-8">
       |    <meta http-equiv="X-UA-Compatible" content="IE=edge">
       |    <meta name="viewport" content="width=device-width, initial-scale=1">
       |
       |    <title>FEAST! Who's Coming</title>
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
       |    <h1>FEAST! Participants</h1>
       |
       |    <h3>This Week <small>$thisWeekDate</small></h3>
       |
       |    <table class="table table-striped">
       |       <thead> <tr> <th></th> <th>First Name</th> <th>Last Name</th> </tr>
       |       </thead>
       |
       |       <tbody>
       |       ${htmlThisWeekTable}
       |       </tbody>
       |    </table>
       |
       |    <h3>Next Week <small>$nextWeekDate</small></h3>
       |
       |    <table class="table table-striped">
       |       <thead> <tr> <th></th> <th>First Name</th> <th>Last Name</th> </tr>
       |       </thead>
       |
       |       <tbody>
       |        ${htmlNextWeekTable}
       |       </tbody>
       |    </table>
       |
       |    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
       |    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
       |  </body>
       |</html>
    """.stripMargin

  }
}
