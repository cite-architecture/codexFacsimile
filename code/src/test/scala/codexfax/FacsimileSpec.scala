package edu.holycross.shot.codexfax
import org.scalatest.FlatSpec

import edu.holycross.shot.cite._
import edu.holycross.shot.citeobj._


class FacsimileSpec extends FlatSpec {

  val repo = CiteRepositorySource.fromFile("code/src/test/resources/burney86pages.cex")
  val burney86urn = Cite2Urn("urn:cite2:hmt:burney86pages.v1:")
  val pages = repo.objectsForCollection(burney86urn)
  val facsimile = Facsimile(pages)

  //val img = Cite2Urn("urn:cite2:hmt:burney86pages.v1.image:")
  //  objs(0).propertyValue(img)
  // then match on result
  "A Facsimile"  should  "extract an Image URN from a record" in {
    val record = pages(0)
    val expected = Cite2Urn("urn:cite2:hmt:burney86img.v1:burney_ms_86_f001r")
    assert(facsimile.image(record).get == expected)
  }

  it should "write a complete edition to a specified directory" in {
    val facsimile = Facsimile(pages)
    facsimile.edition("testout")
  }

}
