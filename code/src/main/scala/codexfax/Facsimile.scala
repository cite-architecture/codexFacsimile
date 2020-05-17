package edu.holycross.shot.codexfax

import edu.holycross.shot.cite._
import edu.holycross.shot.citeobj._
import edu.holycross.shot.citebinaryimage._

import java.io.PrintWriter

import wvlet.log._
import wvlet.log.LogFormatter.SourceCodeLogFormatter


/** Physical facsimile for a sequence of pages implementing
* the CITE architecture's model of an illustrated codex.
*
* @param pages Ordered sequence of pages.
* @param label Label for manuscript.
* @param ictBase Location of Image Citation Tool.
* @param iiifBase Base URL for an IIIF image service.
* @param pathBase Base path for image collections further organized
* by URN structure on the IIIF service host.
*/
case class Facsimile (
  pages: Vector[CiteObject],
  label: String,
  ictBase: String = "http://www.homermultitext.org/ict2/?",
  iiifBase:  String = "http://www.homermultitext.org/iipsrv?",
  pathBase: String = "/project/homer/pyramidal/deepzoom/"
  ) {


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
    val yaml = s"---\nlayout: page\ntitle: ${label}\n---\n\n"


    val pgIds = pages.map(_.urn.objectComponent)
    val pageLinks = pgIds.map(pg => {
      "<option value=\"./" + pg + "/\">" + pg + "</option>"
    })

    val options = """<option select="selected">-</option>""" + pageLinks



    val selectIntro = """<select id="selectbox" name="" onchange="javascript:location.href = this.value;">"""

    val sel = selectIntro + options + "</select>"

    yaml + s"\n\n" + """See a visual [table of contents]("../toc/")""" + "\n\nView page:\n\n" + sel


  }

  /** Format table of contents page with thumbnails.
  *
  * @param thumbWidth Width of thumbnail images in pixels.
  * @param columns Number of thumbnails to include in each row.
  */
  def toc(thumbWidth: Int = 100, columns : Int = 6): String = {
    val yaml = s"---\nlayout: page\ntitle: ${label}\n---\n\n"


    yaml + "Markdown for thumbnails"

  }

  /** Format a single page with facsimile view in markdown.
  *
  * @param cobj Record for a single physical surface.
  * @param prev Link to previous page.  Empty String if this is first surface.
  * @param next Link to next page.  Empty String if this is last surface.
  */
  def page(cobj: CiteObject, imgWidth: Int, prev: String, next: String) : String = {
    val yaml = s"---\nlayout: page\ntitle: ${cobj.label}\n---\n\n"

    val p = if (prev.isEmpty) { "-" } else { s"[${prev}](../${prev}/)"}
    val n = if (next.isEmpty ) { "-" } else { s"[${next}](../${next}/)"}
    val pn = s"previous:  ${p} | next: ${n}"

    yaml + s"${cobj.label}\n\n${imageLink(cobj, imgWidth)} \n\n---\n\n" + pn
  }

  /** Constructs mardown for an embedded image of a given width.
  *
  * @param img URN of image to display.
  * @param w Width in pixels of the display.
  */
  def imageMarkdown(img: Cite2Urn, w: Int): String = {
    val iiifPath = pathBase + PathUtility.expandedPath(img)
    val iiif = IIIFApi(iiifBase, iiifPath)
    iiif.serviceRequest(img, width = Some(w))
  }


  /** Compose markdown for embedded image linked to
  * Image Citation Tool.
  *
  * @param cobj Page to display image for.
  * @param w Width in pixels to display image.
  */
  def imageLink(cobj: CiteObject, w: Int): String = {
    image(cobj) match {

      case Some(u) => {
        val embeddedImg = imageMarkdown(u, w)
        val zoom = ictBase + "urn=" + u
        s"[![${u.objectComponent}](${embeddedImg})](${zoom})"
      }

      case _ => "No image available for " + cobj.urn
    }
  }




  /** Write complete markdown facsimile edition to a named directory.
  *
  * @param dirName Name of output directory, as a String.
  */

  def edition(dirName: String, imgWidth: Int = 800, thumbWidth: Int = 100, columns : Int = 6) : Unit = {
    new java.io.PrintWriter(dirName + "/index.md" ){write(titlePage);close;}
    new java.io.PrintWriter(dirName + "/toc.md" ){write(toc(thumbWidth, columns));close;}
    for ((pg,idx) <- pages.zipWithIndex) {
      val prev = if (idx == 0) { ""  } else { pageId(pages(idx - 1))}
      val nxt = if (idx == (pages.size -1)) {""} else { pageId(pages(idx + 1)) }
      val fName = dirName + "/" + pageId(pg) + ".md"
      new java.io.PrintWriter(fName){ write(page(pg, imgWidth, prev, nxt)); close; }
    }

  }



}
