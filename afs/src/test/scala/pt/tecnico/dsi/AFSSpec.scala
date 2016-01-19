package pt.tecnico.dsi

import java.io.File

import org.scalatest.{Matchers, FlatSpec}
import pt.tecnico.dsi.afs.AFS

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.Implicits.global

/**
  *
  */
class AFSSpec extends FlatSpec with Matchers{
  "listquota" should "???" in {
    val dir = "/afs/ist.utl.pt/groups/dsi-panel-tests"
    val e = AFS.listquota(new File(dir))
    val ret = Await.result(e.run(),5.minutes)
    println(s"listquota result:$ret")
  }
}
