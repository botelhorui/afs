package pt.tecnico.dsi.afs

import java.io.File
import java.util.concurrent.TimeUnit

import _root_.squants.storage.Kilobytes
import work.martins.simon.expect.fluent._
import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, DurationInt}
import squants.storage.StorageConversions._
import squants.storage.{Kilobytes, Storage}

import scala.util.matching.Regex.Match


/*
/etc/init.d/openafs-client restart
kinit ciistadmin/admin
aklog
klist
#insert password
cd /afs/ist.utl.pt/groups/dsi-panel-tests
touch random.data
dd if=/dev/zero of=random.data bs=1K count=1
fs listquota

*/

object teste extends App {
  val path = "/afs/ist.utl.pt/groups/dsi-panel-tests"

  val e1 = AFS.listquota(new File(path))
  val t1 = Await.result(e1.run(),3.seconds)
  val e2 = AFS.setQuota(new File(path),Kilobytes(1024))
  val t2 = Await.result(e2.run(),5.seconds)
  val e3 = AFS.listquota(new File(path))
  val t3 = Await.result(e3.run(),5.seconds)


  println("finished")

}
