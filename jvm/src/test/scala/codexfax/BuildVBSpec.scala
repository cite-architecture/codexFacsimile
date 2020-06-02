package edu.holycross.shot.codexfax
import org.scalatest.FlatSpec

import edu.holycross.shot.cite._
import edu.holycross.shot.citeobj._

//import wvlet.log.LogFormatter.SourceCodeLogFormatter



class BuildVBSpec extends FlatSpec {


  val faxUrn = Cite2Urn("urn:cite2:hmt:msB.v1:")

  val repo = CiteRepositorySource.fromFile("jvm/src/test/resources/vb-all.cex")
  val pages = repo.objectsForCollection(faxUrn)
  val facsimile = Facsimile(repo, faxUrn)

  "A Facsimile"  should "handle all of Venetus B" in {
    val pages = repo.objectsForCollection(faxUrn)
    val facsimile = Facsimile(repo, faxUrn)
    facsimile.bifEdition("vbpages")
  }

}
