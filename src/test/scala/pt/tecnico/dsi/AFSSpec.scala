package pt.tecnico.dsi

import java.io.File

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, FlatSpec}
import pt.tecnico.dsi.afs.AFS
<<<<<<< HEAD:afs/src/test/scala/pt/tecnico/dsi/AFSSpec.scala
import pt.tecnico.dsi.afs.AFS.{DirectoryAlreadyMounting, InvalidVolume, InvalidDirectory, ErrorCase}
=======
import pt.tecnico.dsi.afs.AFS.{InvalidDirectory, ErrorCase}
>>>>>>> 1f72092a7a558b246ae952d08e215425c7c75cb5:src/test/scala/pt/tecnico/dsi/AFSSpec.scala
import squants.information.{Kilobytes, Gigabytes, Information}
import work.martins.simon.expect.fluent.Expect

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.Implicits.global



/**
  *
  */
class AFSSpec extends FlatSpec with ScalaFutures with Matchers{
  val path = "/afs/ist.utl.pt/users/7/7/ist167077"
  val nonExistingPath = "/afs/ist.utl.pt/users/7/7/ist160972"

  val tmpFile = "random.data"
  val defaultQuota = Kilobytes(2048000)
  val volumeName: String = "users.ist167077"
  val nonExitingVolumeName = "aaasaaa"
  val timeout = 5.seconds
  val volumeExpected = "#"+volumeName
  /*
    removes all files and directories in the path afs directory
   */
  def clean() = {
    sys.process.Process(Seq("rm","-f",tmpFile), new java.io.File(path)).!!
    val command = s"fs setquota -path ${new File(path).getPath} -max ${defaultQuota.toKilobytes.toLong}"
    sys.process.Process(command.split(" "), new File(path)).!!

  }

<<<<<<< HEAD:afs/src/test/scala/pt/tecnico/dsi/AFSSpec.scala
  //region <PTS commands>

  "AFS listquota" should "return (name, quota, used) when the directory exists" in {
    //clean()
=======
  def listquota(path: String): (String, Information, Information) = {
>>>>>>> 1f72092a7a558b246ae952d08e215425c7c75cb5:src/test/scala/pt/tecnico/dsi/AFSSpec.scala
    val e = AFS.listquota(new File(path))
    e.run().futureValue match {
      case Right((vname, quota, used)) => {
        println(s">>volume name is $vname")
        assert(vname == volumeName)
      }
      case _ => {
        assert(false);
      }
    }
  }

<<<<<<< HEAD:afs/src/test/scala/pt/tecnico/dsi/AFSSpec.scala
  /*
    it should "return error when directory does not exist" in {
      // TODO remove usage of listquota
      val (name1,quota1,used1) = listquota(nonExistingPath)
      assertResult(nonExistingPath)(name1)
    }
=======
  //region <PTS commands>

  "AFS listquota" should "return (name, quota, used) when the directory exists" in {
    clean()
    val (name1,quota1,used1) = listquota(path)
    assertResult(volumeName)(name1)
  }
>>>>>>> 1f72092a7a558b246ae952d08e215425c7c75cb5:src/test/scala/pt/tecnico/dsi/AFSSpec.scala

      it should "return updated used value after adding a file" in {
        clean()
        // TODO remove usage of listquota
        val (name1,quota1,used1) = listquota(path)
        assertResult(volumeName)(name1)
        // create a file of 1 kilobyte in test afs path
        sys.process.Process(s"dd if=/dev/zero of=$tmpFile bs=1K count=1", new java.io.File(path)).!!
        val (name2,quota2,used2) = listquota(path)
        assertResult(volumeName)(name2)
        assertResult(quota1)(quota2)
        assertResult(used1 + Kilobytes(1))(used2)
      }

      "AFS setquota" should "after increasing a volume quota it should return the same " +
        "volume name, the new set quota, the same used space" in {
        // TODO remove usage of listquota
        val (name1,quota1,used1) = listquota(path)
        assertResult(volumeName)(name1)
        val e2 = AFS.setQuota(new File(path),Gigabytes(1))
        val t2: Either[ErrorCase, Boolean] = Await.result(e2.run(),5.seconds)
        assertResult(Right(true))(t2)
        val (name2,quota2,used2) = listquota(path)
        assertResult(volumeName)(name2)
        assertResult(Gigabytes(1))(quota2)
        assertResult(used1)(used2)
      }
      it should "return error when directory does not exist" in {
        // TODO
      }


<<<<<<< HEAD:afs/src/test/scala/pt/tecnico/dsi/AFSSpec.scala
        "AFS listMount" should "return the volume for the given directory" in {
          val e1 = AFS.listMount(new File(path))
          e1.run().futureValue match {
            case Right(volume) => assertResult(volumeExpected)(volume)
            case _ => fail("Did not get expected volume name")
          }
        }
        it should "Return invalid directory when given directory does not exist" in {
          val e1 = AFS.listMount(new File(nonExistingPath))
          e1.run().futureValue match {
            case Left(error) => assert(error == InvalidDirectory)
            case _ => fail("expected invalid directory but got something else")
          }
        }


        "AFS MakeMount" should "create a volume at a given directory and return success" in {
          // TODO before after
          val ret = AFS.makeMount(new File(path), volumeName).run().futureValue match {
            case Right(success) => assert(success == true)
            case _ => fail("operation not successful")
          }
        }
        it should "return error when directory does not exist" in {
          // TODO before after
          val ret = AFS.makeMount(new File(nonExistingPath), volumeName).run().futureValue match {
            case Left(error) => assert(error == InvalidDirectory)
            case _ => fail("")
          }
        }
        it should "return error when the volume does not exist" in {
          // TODO before after
          val ret = AFS.makeMount(new File(path), nonExitingVolumeName).run().futureValue match {
            case Left(error) => assert(error == InvalidVolume)
            case _ => fail("")
          }
        }
        it should "return error when directory is already a mount point" in {
          // TODO before after
          AFS.makeMount(new File(path), volumeName).run().futureValue match {
            case Right(success) => assert(success == true)
            case _ => fail("operation not successful")
          }
          AFS.makeMount(new File(path), volumeName).run().futureValue match {
            case Left(error) => assert(error == DirectoryAlreadyMounting)
            case _ => fail("operation should have failed")
          }
        }

        "AFS RemoveMount" should "remove mount for the given directory" in {
          // TODO before after
          AFS.removeMount(new File(path)).run().futureValue match {
            case Right(success) => assert(success == true)
            case _ => "failed tol remove a mount"
          }
        }
        it should "return error if the directory does not exist" in {
          // TODO before after
        }
        it should "return error if the directory is not a mount point" in {
          // TODO before after
        }

        "AFS listACL" should "return the default ACL for the given directory" in {
          // TODO before after
        }
        it should "return error if the directory does not exists" in {
          // TODO before after
        }
        it should "return error if the directory is not a mount point" in {
          // TODO before after
        }

        "AFS setACL" should "change the ACL for the given directory" in {
          // TODO before after
        }
        it should "return error if the directory does not exists" in {
          // TODO before after
        }
        it should "return error if the directory is not a mount point" in {
          // TODO before after
        }


        "AFS checkVolumes" should "return success" in {
          // TODO before after
        }
        it should "return the specific error when volume is not ok" in {
          // TODO before after
        }

        //endregion


        //region <PTS commands>

        "AFS createUser" should "return success when username is valid and not used" in {
          //TODO
        }
        it should "fail when the username is invalid" in {
          //TODO
        }
        it should "fail when the username is already in use" in {
          //TODO
        }

        "AFS createGroup" should "create a group when user with username exists and group name is available" in {
          //TODO
        }
        it should "fail when user for username doest not exists" in {
          //TODO
        }
        it should "fail when group name already exists" in {
          //TODO
        }

        "AFS delete user or group" should "delete the given user/group if it exists" in {
          //TODO
        }
        it should "fail when given name is not a user nor group" in {
          //TODO
        }

        "AFS addUserToGroup" should "return success when user and group exist" in {
          //TODO
        }
        it should "fail when user does not exists and group does not exist" in {
          //TODO
        }
        it should "" in {
          //TODO
        }


        "AFS removeUserFromGroup" should "" in {
          //TODO
        }
        "AFS membership" should "" in {
          //TODO
        }
        "AFS listGroups " should "" in {
          //TODO
        }

        //endregion

        //region <VOS commands>

        "AFS backupVolume" should "" in {
          //TODO
        }
        "AFS createVolume" should "" in {
          //TODO
        }
        "AFS removeVolume" should "" in {
          //TODO
        }
        "AFS examineVolume" should "" in {
          //TODOl
        }
        "AFS releaseVolume" should "" in {
          //TODO
        }
        //endregion

      */
=======
  "AFS setquota" should "after increasing a volume quota it should return the same " +
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
  it should "return error when directory doess not exist" in {
    // TODO
  }

  "AFS listMount" should "return the volume for the given directory" in {
    val e1 = AFS.listMount(new File(path))
    e1.run().futureValue match {
      case Right(volume) => assertResult(volumeExpected)(volume)
      case _ => fail("Did not get expected volume name")
    }
  }
  it should "Return invalid directory when given directory does not exist" in {
    val e1 = AFS.listMount(new File(nonExistantDir))
    e1.run().futureValue match {
      case Left(error) => assert(error == InvalidDirectory)
      case _ => fail("expected invalid directory but got something else")
    }
  }


  "AFS MakeMount" should "create a volume at a given directory and return success" in {
    // TODO
  }
  it should "return error when directory does not exist" in {
    // TODO
  }
  it should "return error when the volume does not exist" in {
    // TODO
  }
  it should "return error when directory is already a mount point" in {
    // TODO
  }

  "AFS RemoveMount" should "remove mount for the given directory" in {
    // TODO
  }
  it should "return error if the directory does not exist" in {
    // TODO
  }
  it should "return error if the directory is not a mount point" in {
    // TODO
  }

  "AFS listACL" should "return the default ACL for the given directory" in {
    // TODO
  }
  it should "return error if the directory does not exists" in {
    // TODO
  }
  it should "return error if the directory is not a mount point" in {
    // TODO
  }

  "AFS setACL" should "change the ACL for the given directory" in {
    // TODO
  }
  it should "return error if the directory does not exists" in {
    // TODO
  }
  it should "return error if the directory is not a mount point" in {
    // TODO
  }


  "AFS checkVolumes" should "return success" in {
    // TODO
  }
  it should "return the specific error when volume is not ok" in {
    // TODO
  }

  //endregion


  //region <PTS commands>

  "AFS createUser" should "return success when username is valid and not used" in {
    //TODO
  }
  it should "fail when the username is invalid" in {
    //TODO
  }
  it should "fail when the username is already in use" in {
    //TODO
  }

  "AFS createGroup" should "create a group when user with username exists and group name is available" in {
    //TODO
  }
  it should "fail when user for username doest not exists" in {
    //TODO
  }
  it should "fail when group name already exists" in {
    //TODO
  }

  "AFS delete user or group" should "delete the given user/group if it exists" in {
    //TODO
  }
  it should "fail when given name is not a user nor group" in {
    //TODO
  }

  "AFS addUserToGroup" should "return success when user and group exist" in {
    //TODO
  }
  it should "fail when user does not exists and group does not exist" in {
    //TODO
  }
  it should "" in {
    //TODO
  }


  "AFS removeUserFromGroup" should "" in {
    //TODO
  }
  "AFS membership" should "" in {
    //TODO
  }
  "AFS listGroups " should "" in {
    //TODO
  }

  //endregion

  //region <VOS commands>

  "AFS backupVolume" should "" in {
    //TODO
  }
  "AFS createVolume" should "" in {
    //TODO
  }
  "AFS removeVolume" should "" in {
    //TODO
  }
  "AFS examineVolume" should "" in {
    //TODO
  }
  "AFS releaseVolume" should "" in {
    //TODO
  }
  //endregion


>>>>>>> 1f72092a7a558b246ae952d08e215425c7c75cb5:src/test/scala/pt/tecnico/dsi/AFSSpec.scala
}
