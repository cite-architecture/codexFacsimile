package edu.holycross.shot.codexfax
import org.scalatest.FlatSpec

import edu.holycross.shot.cite._
import edu.holycross.shot.citeobj._

//import wvlet.log.LogFormatter.SourceCodeLogFormatter



class BuildUpsilonSpec extends FlatSpec {


  val upsilonUrn = Cite2Urn("urn:cite2:hmt:e3pages.v1:")

  val repo = CiteRepositorySource.fromFile("jvm/src/test/resources/e3-all.cex")
  val pages = repo.objectsForCollection(upsilonUrn)
  val facsimile = Facsimile(repo, upsilonUrn)

  "A Facsimile"  should "handle all of Upsilon 1.1" in {
    val pages = repo.objectsForCollection(upsilonUrn)
    val facsimile = Facsimile(repo, upsilonUrn)
    facsimile.bifEdition("e3pages")
  }

}
