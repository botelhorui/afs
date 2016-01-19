package pt.tecnico.dsi.afs

import java.io.File

import squants.storage.{Kilobytes, Storage}
import work.martins.simon.expect.fluent.Expect

import scala.util.matching.Regex.Match

import work.martins.simon.expect.fluent._

/**
  *
  */
object AFS {

  def listquota(dir: File): Expect[Either[ErrorCase,(String,Storage,Storage)]] = {
    val command = "fs listquota -path "+dir.getAbsolutePath
    val e = new Expect[Either[ErrorCase,(String,Storage,Storage)]](command,Left(UnknownError))
    e.expect
      .when(
        """Volume Name\s+Quota\s+Used\s+%Used\s+Partition
          |([^\s]+)\s+(\d+)\s+(\d+)""".stripMargin.r)
      .returning{ m: Match =>
        Right((m.group(1),Kilobytes(m.group(2).toInt),Kilobytes(m.group(3).toInt)))
      }
    e
  }


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

}
