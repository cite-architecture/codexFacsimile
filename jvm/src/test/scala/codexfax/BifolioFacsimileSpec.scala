package edu.holycross.shot.codexfax
import org.scalatest.FlatSpec

import edu.holycross.shot.cite._
import edu.holycross.shot.citeobj._

//import wvlet.log.LogFormatter.SourceCodeLogFormatter



class BifolioFacsimileSpec extends FlatSpec {


  val upsilonUrn = Cite2Urn("urn:cite2:hmt:e3pages.v1:")



  "A Facsimile"  should "write bifolio layouts" in {
      val repo = CiteRepositorySource.fromFile("jvm/src/test/resources/e3allTiniest.cex")
      val pages = repo.objectsForCollection(upsilonUrn)
      val facsimile = Facsimile(repo, upsilonUrn)
      facsimile.bifEdition("testbifout")
  }

  it should "construct a ToC by bifolio spread" in {
    val repo = CiteRepositorySource.fromFile("jvm/src/test/resources/e3pagesTiny.cex")
    val pages = repo.objectsForCollection(upsilonUrn)
    val facsimile = Facsimile(repo, upsilonUrn)
    val toc = facsimile.bifToc()
    println(toc)
  }

  it should "allow surfaces with no illustration available" in {
    val repo = CiteRepositorySource.fromFile("jvm/src/test/resources/e3WithNulls.cex")
    val pages = repo.objectsForCollection(upsilonUrn)
    val facsimile = Facsimile(repo, upsilonUrn)
    val toc = facsimile.bifToc()
    println(toc)
  }



}
