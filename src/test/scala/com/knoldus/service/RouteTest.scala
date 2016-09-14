package com.knoldus.service

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.knoldus.connection.H2DBImpl
import com.knoldus.repo.{Bank, BankRepository}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.{ExecutionContextExecutor, Future}

//
//class RouteTest  extends WordSpec with Matchers with ScalatestRouteTest with Routes with BankRepositoryTestImpl{
//
//  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
//
//  "The Bank service" should {
//
//    "get bank detail by bank id" in {
//      Get("/bank/1") ~> routes ~> check {
//        responseAs[String] === """{}"""
//      }
//    }
//
//    "get all bank detail " in {
//      Get("/bank/all") ~> routes ~> check {
//        responseAs[String] shouldEqual  """[{"name":"TestB bank","id":1}]"""
//      }
//    }
//
//    "save bank detail" in {
//      Post("/bank/save", HttpEntity(ContentTypes.`application/json`, write(Bank("My test Bank")))) ~> routes ~> check {
//        responseAs[String] shouldEqual "Bank has  been saved successfully"
//      }
//    }
//
//
//    "update bank detail" in {
//      Post("/bank/update", HttpEntity(ContentTypes.`application/json`, write(Bank("My test Bank")))) ~> routes ~> check {
//        responseAs[String] shouldEqual "Bank has  been updated successfully"
//      }
//    }
//
//    "delete bank detail by id" in {
//      Post("/bank/delete/2") ~> routes ~> check {
//        responseAs[String] shouldEqual "Bank has been deleted successfully"
//      }
//    }
//
//  }
//
//
//
//}
//// For testing
//trait BankRepositoryTestImpl extends BankRepository with H2DBImpl{
//  override  def create(bank: Bank): Future[Int] = Future.successful(1)
//
//  override  def update(bank: Bank): Future[Int] = Future.successful(1)
//
//  override  def getById(id: Int): Future[Option[Bank]] = Future.successful(Some(Bank("TestB bank" ,Some(1))))
//
//  override def getAll(): Future[List[Bank]] = Future.successful(List(Bank("TestB bank" ,Some(1))))
//
//  override def delete(id: Int): Future[Int] = Future.successful(1)
//
//}