package edu.holycross.shot.codexfax

import edu.holycross.shot.cite._
import edu.holycross.shot.citeobj._
import java.io.PrintWriter

import wvlet.log._
import wvlet.log.LogFormatter.SourceCodeLogFormatter


case class Facsimile (
  pages: Vector[CiteObject],
  ictBase: String = "http://www.homermultitext.org/ict2/?"
)  {

  // These are defined in the Codex Model:
  /*
  urn:cite2:hmt:burney86pages.v1.sequence:#Page sequence#Number#
  urn:cite2:hmt:burney86pages.v1.image:#TBS image#Cite2Urn#
  urn:cite2:hmt:burney86pages.v1.urn:#URN#Cite2Urn#
  urn:cite2:hmt:burney86pages.v1.rv:#Recto or Verso#String#recto,verso,bifolio
  urn:cite2:hmt:burney86pages.v1.label:#Label#String#
  */

  /** Select image URN property required by codex model.
  * If no image for this surface, return None.
  *
  * @param cobj Record to extract image URN from.
  */
  def image(cobj: CiteObject): Option[Cite2Urn] = {
    def imgUrn: Cite2Urn = cobj.urn.addProperty("image")
    cobj.propertyValue(imgUrn) match {
      case img: Cite2Urn => Some(img)
      case _ => None
    }
  }

  /** Select surface identifer from URN.
  *
  * @param cobj Record to extract image URN from.
  */
  def pageId(cobj: CiteObject): String = {
    cobj.urn.objectComponent
  }

  /** Format title page for entire facsimile edition.*/
  def titlePage: String = {
    "Markdown string for an index.md page"
  }

  /** Format table of contents page with thumbnails.
  *
  * @param thumbWidth Width of thumbnail images in pixels.
  * @param columns Number of thumbnails to include in each row.
  */
  def toc(thumbWidth: Int = 100, columns : Int = 6): String = {
    "Markdown page for a visual table of contents with thumbnails"
  }

  /** Format a single page with facsimile view in markdown.
  *
  * @param cobj Record for a single physical surface.
  * @param prev Link to previous page.  Empty String if this is first surface.
  * @param next Link to next page.  Empty String if this is last surface.
  */
  def page(cobj: CiteObject, prev: String, next: String) : String = {
    val yaml = s"---\nlayout: page\ntitle: ${cobj.label}\n---\n\n"
    val p = if (prev.isEmpty) { "-" } else { s"[${prev}](${prev}/)"}
    val n = if (next.isEmpty ) { "-" } else { s"[${next}](${next}/)"}
    val pn = s"previous:  ${p} | next: ${n}"
    // CHECK FOR NONE!
    val zoom = ictBase + "urn=" + image(cobj)
    val linkedImage = s"[ZOom](${zoom})"
    yaml + s"${cobj.label}\n\n${linkedImage} \n\n---\n\n" + pn
  }


  /** Write complete markdown facsimile edition to a named directory.
  *
  * @param dirName Name of output directory, as a String.
  */
  def edition(dirName: String, thumbWidth: Int = 100, columns : Int = 6) : Unit = {
    new java.io.PrintWriter(dirName + "/index.md" ){write(titlePage);close;}
    new java.io.PrintWriter(dirName + "/toc.md" ){write(toc(thumbWidth, columns));close;}
    for ((pg,idx) <- pages.zipWithIndex) {
      val prev = if (idx == 0) { ""  } else { pageId(pages(idx - 1))}
      val nxt = if (idx == (pages.size -1)) {""} else { pageId(pages(idx + 1)) }
      val fName = dirName + "/" + pageId(pg) + ".md"
      new java.io.PrintWriter(fName){ write(page(pg, prev, nxt)); close; }
    }

  }



}
