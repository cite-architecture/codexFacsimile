package edu.holycross.shot.codexfax

import edu.holycross.shot.cite._
import edu.holycross.shot.citeobj._
import edu.holycross.shot.citebinaryimage._

import java.io.PrintWriter
import java.io.File

import wvlet.log._
//import wvlet.log.LogFormatter.SourceCodeLogFormatter


/** Physical facsimile for a sequence of pages implementing
* the CITE architecture's model of an illustrated codex.
*
* @param repo Cite Object repository with data.
* @param urn URN of collection implementing the codex model.
* @param ictBase Location of Image Citation Tool.
* @param iiifBase Base URL for an IIIF image service.
* @param pathBase Base path for image collections further organized
* by URN structure on the IIIF service host.
*/
case class Facsimile (
  repo: CiteCollectionRepository,
  urn: Cite2Urn,
  ictBase: String = "http://www.homermultitext.org/ict2/?",
  iiifBase:  String = "http://www.homermultitext.org/iipsrv?",
  pathBase: String = "/project/homer/pyramidal/deepzoom/"
) extends LogSupport {

  /** Collect Vector of page objects. */
  def pages = repo.objectsForCollection(urn)

  /** Labelling String for collection.*/
  def label =  repo.catalog.collection(urn).get.collectionLabel

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

  /** Extract rights statement for page record's image.
  *
  * @param record CiteObject recording a page in the codex.
  */
  def rights(record: CiteObject) : String = {
    val imgOpt = record.propertyValue(record.urn.addProperty("image")) match {
      case img: Cite2Urn => Some(img)
      case _ => None
    }
    val imgUrn = imgOpt.get

    imgUrn.objectComponent match {
      case "null" => "No image available"
      case _  => {
        debug("imgOpt is " + imgOpt)
        debug("So get that as " + imgUrn )

        val imgObj = repo.citableObject(imgUrn)
        debug("And retrieve its object " + imgObj)

        val rightsProp : Cite2Urn = imgUrn.addProperty("rights")
        imgObj.propertyValue(rightsProp).toString
      }
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
    val select = selectIntro + options + "</select>"

    yaml + "\n\nSee a [visual table of contents](./toc/)" + "\n\nView page:\n\n" + select
  }

  /** Format thumbnail image with link to page.
  *
  * @param cobj Record for page.
  * @param w Width in pixels to make thumbnail image.
  */
  def thumbLink(cobj: CiteObject, w: Int): String = {
    val pg = "../" + cobj.urn.objectComponent + "/"
    image(cobj) match {

      case Some(u) => {
        val embeddedImg = imageMarkdown(u, w)

        s"[![${pg}](${embeddedImg})${cobj.urn.objectComponent}](${pg})"
      }

      case _ => s"[${cobj.label}](${pg})" + " No image available."
    }
  }

  /** Format table of contents page with thumbnails.
  *
  * @param thumbWidth Width of thumbnail images in pixels.
  * @param columns Number of thumbnails to include in each row.
  */
  def toc(thumbWidth: Int = 100, columns : Int = 6): String = {
    val yaml = s"---\nlayout: page\ntitle: ${label}\n---\n\n"

    val grouped = pages.sliding(6, 6).toVector
    val rows = grouped.map ( row => row.map(pg => thumbLink(pg, thumbWidth)))

    val mdRows = rows.map(row => "| " + row.mkString(" |"))

    val tableHdr = for (i <- 1 to columns) yield {
      "| ------------- "
    }

    yaml + "Markdown for thumbnails\n\n" + tableHdr.mkString + " |\n" + mdRows.mkString(" |\n") + " |\n\n"
  }

  /** Format a single page with facsimile view in markdown.
  *
  * @param cobj Record for a single physical surface.
  * @param prev Link to previous page.  Empty String if this is first surface.
  * @param next Link to next page.  Empty String if this is last surface.
  */
  def pageMarkdown(cobj: CiteObject, imgWidth: Int, prev: String, next: String) : String = {
    val yaml = s"---\nlayout: page\ntitle: ${cobj.label}\n---\n\n"
    val urn = s"Cite this object as `${cobj.urn}`.  The full image is linked to a citation tool you can use to cite regions of the image."
    val p = if (prev.isEmpty) { "-" } else { s"[${prev}](../${prev}/)"}
    val n = if (next.isEmpty ) { "-" } else { s"[${next}](../${next}/)"}
    val pn = s"previous: ${p} | next: ${n}"
    val imgRights = "<p style=\"text-align: center; font-style: italic;\">" + rights(cobj) + "</p>"
    yaml + s"${cobj.label}\n\n${urn}\n\n${imageLink(cobj, imgWidth)} \n\n${imgRights}\n\n---\n\n" + pn
  }

  /** Constructs markdown for an embedded image of a given width.
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
  def imageLink(cobj: CiteObject,  w: Int, label: String = ""): String = {
    val formattedLabel : String = if (label.isEmpty) {
      cobj.urn.objectComponent
    } else {
      label
    }
    image(cobj) match {

      case Some(u) => {
        val embeddedImg = imageMarkdown(u, w)
        val zoom = ictBase + "urn=" + u
        s"[![${formattedLabel}](${embeddedImg})](${zoom})"
      }

      case _ => formattedLabel + " No image available."
    }
  }

  /** Write complete markdown facsimile edition to a named directory.
  *
  * @param dirName Name of output directory, as a String.
  */
  def edition(dirName: String, imgWidth: Int = 800, thumbWidth: Int = 100, columns : Int = 6) : Unit = {
    new java.io.PrintWriter(dirName + "/index.md" ){write(titlePage);close;}
    val tocDir = new java.io.File(dirName + "/toc").mkdirs

    new java.io.PrintWriter(dirName + "/toc/index.md" ){write(toc(thumbWidth, columns));close;}
    for ((pg,idx) <- pages.zipWithIndex) {
      val prev = if (idx == 0) { ""  } else { pageId(pages(idx - 1))}
      val nxt = if (idx == (pages.size -1)) {""} else { pageId(pages(idx + 1)) }

      val pageDir = new java.io.File(dirName + "/" +  pageId(pg)).mkdirs
      val fName = dirName + "/" + pageId(pg) + "/index.md"
      new java.io.PrintWriter(fName){ write(pageMarkdown(pg, imgWidth, prev, nxt)); close; }
    }
  }

  def bifIdRange(bifolio: Vector[CiteObject]) : String = {
      bifolio.size match {
        case 0 => {
          warn("ERROR: empty page list")
          ""
        }
        case 1 => {
          bifolio(0).urn.objectComponent
        }
        case 2 => {
          bifolio(0).urn.objectComponent + "-" + bifolio(1).urn.objectComponent
        }
        case _ => {
          warn("ERROR: wrong number of pages in bifolio list: " + bifolio.size)
          "ERROR-" + bifolio(0).urn.objectComponent
        }
      }
  }

  def bifEdition(dirName: String, imgWidth: Int = 800, thumbWidth: Int = 100, columns : Int = 6) : Unit = {
    new java.io.PrintWriter(dirName + "/index.md" ){write(bifTitlePage);close;}
    val tocDir = new java.io.File(dirName + "/toc").mkdirs
    new java.io.PrintWriter(dirName + "/toc/index.md" ){write(bifToc(thumbWidth, columns));close;}

    val pairs = pages.sliding(2,2).toVector


    for ((pr,idx) <- pairs.zipWithIndex) {
      val prev = if (idx == 0) { ""  } else {
        bifIdRange(pairs(idx - 1))
      }
      val nxt = if (idx == (pairs.size -1)) {""} else {
        bifIdRange(pairs(idx + 1))

      }

      val bifDir = new java.io.File(dirName + "/" +  bifIdRange(pr)).mkdirs
      val fName = dirName + "/" + bifIdRange(pr) + "/index.md"
      new java.io.PrintWriter(fName){
        write(bifPageMarkdown(pr, imgWidth, prev, nxt));
        close;
      }
    }

  }

  def imgNonNull(cobj: CiteObject): Boolean = {
    cobj.propertyValue(cobj.urn.addProperty("image")) match {
      case img: Cite2Urn => {
        if (img.objectComponent == "null") { false} else {true}
      }
      case _ => false
    }
  }

  def bifPageMarkdown(bifolio: Vector[CiteObject], imgWidth: Int, prev: String, next: String) : String = {

    val rangeId = bifIdRange(bifolio)
    val yaml = s"---\nlayout: page\ntitle: ${rangeId}\n---\n\n"
    val cite = bifolio.size match {
      case 1 =>  s"Cite this object as `" + bifolio(0).urn + "`"
      case 2 =>  s"Cite this object as `" + bifolio(0).urn + "-" + bifolio(1).urn.objectComponent + "`."
    }



    val hasImage : Boolean = imgNonNull(bifolio(0))

    val urn = if (hasImage) {
      cite +  " The full image is linked to a citation tool you can use to cite regions of the image."
    } else {
      cite
    }

    val p = if (prev.isEmpty) { "-" } else { s"[${prev}](../${prev}/)"}
    val n = if (next.isEmpty ) { "-" } else { s"[${next}](../${next}/)"}
    val pn = s"previous: ${p} | next: ${n}"
    val imgRights = if (hasImage ) {
      "<p style=\"text-align: center; font-style: italic;\">" + rights(bifolio(0)) + "</p>"
    } else { "" }

    val imgLink = if (hasImage){ imageLink(bifolio(0), imgWidth) } else { "No image available." }


    //yaml + s"${cobj.label}\n\n${urn}\n\n${imageLink(cobj, imgWidth)} \n\n${imgRights}\n\n---\n\n" + pn

    yaml + s"${rangeId}\n\n${urn}\n\n${imgLink} \n\n${imgRights}\n\n---\n\n" + pn
  }


  def bifThumbLink(verso: CiteObject, recto: CiteObject, w: Int): String = {
    val rangeLabel = verso.urn.objectComponent + "-" +  recto.urn.objectComponent
    val pg = "../" + rangeLabel + "/"

    if (imgNonNull(verso)) {
      image(verso) match {
        case Some(u) => {
          val embeddedImg = imageMarkdown(u, w)
          s"[![${pg}](${embeddedImg})${rangeLabel}](${pg})"
        }
        case _ => s"[${rangeLabel}](${pg})" + " No image available."
      }
    } else {
      s"[${rangeLabel}](${pg})" + " No image available."
    }
  }

  def bifThumb(pr: Vector[CiteObject], thumbWidth: Int = 100) : String = {
    pr.size match {
      case 0 => {
        warn("Empty pairing!")
        ""
      }
      case 1 => {
        warn("Singleton left over " + pr(0).urn)
        //thumbLink(pr(0), thumbWidth)
        ""
      }
      case 2 => {
        bifThumbLink(pr(0), pr(1), thumbWidth)
      }
      case _ => {
        warn("Very odd sized pair: " + pr.size + " for " + pr.map(_.urn).mkString(", "))
        warn("Creating thumbnail for first image")
        thumbLink(pr(0), thumbWidth)
      }
    }
  }

  def bifToc(thumbWidth: Int = 100, columns : Int = 6): String = {
    Logger.setDefaultLogLevel(LogLevel.DEBUG)
    val yaml = s"---\nlayout: page\ntitle: ${label}\n---\n\n"

    val paired = pages.sliding(2,2).toVector
    debug(s"${paired.size} pairs from ${pages.size} pages")

    val grouped = paired.sliding(6, 6).toVector

    val rows = grouped.map(row => row.map( pr => bifThumb(pr, thumbWidth)))
    val mdRows = rows.map(row => "| " + row.mkString(" |"))

    val tableHdr = for (i <- 1 to columns) yield {
      "| ------------- "
    }

    yaml + "Markdown for thumbnails\n\n" + tableHdr.mkString + " |\n" + mdRows.mkString(" |\n") + " |\n\n"
  }

  def bifTitlePage: String = {
    val yaml = s"---\nlayout: page\ntitle: ${label}\n---\n\n"
    val pairIds = pages.sliding(2,2).toVector.map( pr => bifIdRange(pr))
    val pageLinks = pairIds.map(bif => {
      "<option value=\"./" + bif + "/\">" + bif + "</option>"
    })


    val options = """<option select="selected">-</option>""" + pageLinks

    val selectIntro = """<select id="selectbox" name="" onchange="javascript:location.href = this.value;">"""
    val select = selectIntro + options + "</select>"

    yaml + "\n\nSee a [visual table of contents](./toc/)" + "\n\nView bifolio spread:\n\n" + select
  }

}
