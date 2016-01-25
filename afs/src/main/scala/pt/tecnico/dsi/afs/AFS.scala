package pt.tecnico.dsi.afs

import java.io.File

import squants.storage.StorageConversions._
import squants.storage.{Kilobytes, Storage}
import squants.storage.{Kilobytes, Storage}
import work.martins.simon.expect.EndOfFile
import work.martins.simon.expect.fluent.Expect

import scala.util.matching.Regex.Match

import work.martins.simon.expect.fluent._

/**
  *
  */
object AFS {

  /*
    Error messages
  */
  trait ErrorCase
  case object InvalidDirectory extends ErrorCase
  case object InvalidUserOrGroupName extends ErrorCase
  case object CouldNotObtainAFSToken extends ErrorCase
  case object InvalidUserName extends ErrorCase
  case object AFSIdAlreadyTaken extends ErrorCase
  case object UnknownError extends ErrorCase
  case object NotImplemented extends ErrorCase

  def listquota(directory: File): Expect[Either[ErrorCase,(String,Storage,Storage)]] = {
    val dir = directory.getPath
    val command = s"fs listquota -path $dir"
    val e = new Expect[Either[ErrorCase,(String,Storage,Storage)]](command,Left(UnknownError))
    e.expect
      .when(s"File '$dir' doesn't exist")
        .returning(Left(InvalidDirectory))
      //Note that Quota might be "no limit" but since we do not allow to set a quota to no limit we don't handle this case
      .when(
        """Volume Name\s+Quota\s+Used\s+%Used\s+Partition
          |([^\s]+)\s+(\d+)\s+(\d+)""".stripMargin.r)
        .returning{ m: Match =>
          //Quota and Used are in kilobytes
          Right((m.group(1),Kilobytes(m.group(2).toInt),Kilobytes(m.group(3).toInt)))
        }
    e
  }

  def setQuota(directory: File, quota: Storage): Expect[Either[ErrorCase, Boolean]] = {
    require(quota > 0.kilobytes, "Quota must be positive")
    val dir = directory.getPath
    val command = s"fs setquota -path $dir -max ${quota.toKilobytes.toLong}"
    val e = new Expect[Either[ErrorCase, Boolean]](command, Left(UnknownError))
    e.expect
      .when(s"File '$dir' doesn't exist")
      .returning(Left(InvalidDirectory))
      .when(EndOfFile)
      .returning(Right(true))
    e
  }

  def listACL(directory: File): Expect[Either[ErrorCase, Map[String, Permission]]] = {
    new Expect[Either[ErrorCase, Map[String, Permission]]]("",Left(NotImplemented))
  }





}
