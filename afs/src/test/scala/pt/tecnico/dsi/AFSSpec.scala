package pt.tecnico.dsi

import java.io.File

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, FlatSpec}
import pt.tecnico.dsi.afs.AFS
import pt.tecnico.dsi.afs.AFS.ErrorCase
import squants.storage.{Gigabytes, Kilobytes, Storage}
import work.martins.simon.expect.fluent.Expect

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.Implicits.global

/**
  *
  */
class AFSSpec extends FlatSpec with ScalaFutures with Matchers{
  val path = "/afs/ist.utl.pt/groups/dsi-panel-tests"
  val tmpFile = "random.data"
  val defaultQuota = Kilobytes(2048000)
  val volumeName: String = "group.dsi-panel-tests"
  val nonExistantDir = "dirx"
  /*
    removes all files and directories in the path afs directory
   */
  def clean() = {
    sys.process.Process(Seq("rm","-f",tmpFile), new java.io.File(path)).!!
    val command = s"fs setquota -path ${new File(path).getPath} -max ${defaultQuota.toKilobytes.toLong}"
    sys.process.Process(command.split(" "), new File(path)).!!

  }

  def listquota(path: String): (String, Storage, Storage) = {
    val e = AFS.listquota(new File(path))
    e.run().futureValue match {
      case Right((name, quota, used)) => {
        (name, quota, used)
      }
      case Left(_) => {
        (nonExistantDir,Kilobytes(0),Kilobytes(0))
      }
    }
  }

  "AFS listquota" should "return (name, quota, used) when the directory exists" in {
    clean()
    val (name1,quota1,used1) = listquota(path)
    assertResult(volumeName)(name1)
  }

  it should "return error when directory does not exist" in {
    val (name1,quota1,used1) = listquota(nonExistantDir)
    assertResult(nonExistantDir)(name1)
  }

  it should "return updated used value after adding a file" in {
    clean()
    val (name1,quota1,used1) = listquota(path)
    assertResult(volumeName)(name1)
    // create a file of 1 kilobyte in test afs path
    sys.process.Process(s"dd if=/dev/zero of=$tmpFile bs=1K count=1", new java.io.File(path)).!!
    val (name2,quota2,used2) = listquota(path)
    assertResult(volumeName)(name2)
    assertResult(quota1)(quota2)
    assertResult(used1 + Kilobytes(1))(used2)
  }


  "AFS setquota" should "after increasing the quota it should return the same " +
    "volume name, the new set quota, the same used space" in {
    val (name1,quota1,used1) = listquota(path)

    val e2 = AFS.setQuota(new File(path),Gigabytes(1))
    val t2: Either[ErrorCase, Boolean] = Await.result(e2.run(),5.seconds)
    assertResult(Right(true))(t2)
    val (name2,quota2,used2) = listquota(path)
    assertResult(volumeName)(name2)
    assertResult(Gigabytes(1))(quota2)
    assertResult(used1)(used2)
  }
}
