package edu.holycross.shot.codexfax
import org.scalatest.FlatSpec

import edu.holycross.shot.cite._
import edu.holycross.shot.citeobj._


class FacsimileSpec extends FlatSpec {

  val repo = CiteRepositorySource.fromFile("code/src/test/resources/burney86pages.cex")
  val burney86urn = Cite2Urn("urn:cite2:hmt:burney86pages.v1:")
  val pages = repo.objectsForCollection(burney86urn)


  val facsimile = Facsimile(pages, "British Library, Burney 86")

  //val img = Cite2Urn("urn:cite2:hmt:burney86pages.v1.image:")
  //  objs(0).propertyValue(img)
  // then match on result
  "A Facsimile"  should  "extract an Image URN from a record" in {
    val record = pages(0)
    val expected = Cite2Urn("urn:cite2:bl:burney86imgs.v1:burney_ms_86_f001r")
    assert(facsimile.image(record).get == expected)
  }

  it should "construct a markdown reference to a binary image" in {
    val record = pages(0)
    val img = facsimile.image(record).get
    val imgSize = 100
    val actual = facsimile.imageMarkdown(img, imgSize)
    val expected = s"http://www.homermultitext.org/iipsrv?IIIF=/project/homer/pyramidal/deepzoom/bl/burney86imgs/v1/burney_ms_86_f001r.tif/full/${imgSize},/0/default.jpg"
    assert(actual == expected)
  }

  it should "format a linked image for a page" in {
    val record = pages(0)
    println("IMAGE: " + facsimile.imageLink(record, 500))
  }

  it should "return an appropriate message if no image exists for a page" in pending

  it should "find the surface identifier for a record"in {
    val record = pages(0)
    val expected = "1r"
    assert(facsimile.pageId(record) == expected)
  }


  it should "write a complete edition to a specified directory" in {
    facsimile.edition("testout")
  }

}
