package com.github.mlangc.latex

import java.io.File
import java.nio.file.Files

import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.io.{FileUtils, IOUtils}

import scala.collection.JavaConverters._

object RenderEquations extends StrictLogging {
  def main(args: Array[String]): Unit = {
    equationFiles.foreach { equationFile =>
      logger.info(s"Processing ${equationFile.getName}")
      val equation = extractEquation(equationFile)
      val templateToRender = equationsTemplate.replace(EquationPlaceholder, equation)

      doWithTmpTexFile(templateToRender) { texFile =>
        import ammonite.ops._
        implicit val wd: Path = Path(texFile.getParentFile)

        val pdfLatexRes = %%('pdflatex, "-interaction", "nonstopmode", texFile.getName)
        assert(pdfLatexRes.exitCode == 0, s"Failed to execute pdflatex: $pdfLatexRes")

        val tmpPdfFileName = texFile.getName.replace(".tex", ".pdf")
        val tmpPngFileName = texFile.getName.replace(".tex", ".png")
        val tmpPdf = new File(texFile.getParent, tmpPdfFileName)
        val tmpPng = new File(texFile.getParent, tmpPngFileName)

        try {
          val magickRes = %%('magick,
            "-density", "1200", tmpPdfFileName,
            "-resize", "1400", "-gravity", "South", "-background", "white", "-splice", "0x1",
            "-background", "black", "-splice", "0x1", "-trim", "+repage", "-chop", "0x1",
            "-gravity", "North", "-background", "white", "-splice", "0x1",
            "-background", "black", "-splice", "0x1", "-trim", "+repage", "-chop", "0x1", tmpPngFileName)

          assert(magickRes.exitCode == 0, s"Failed to execute magick: $magickRes")

          val targetPng = new File(equationFile.getParent, equationFile.getName.replace(".tex", ".png"))
          FileUtils.copyFile(tmpPng, targetPng)
        } finally {
          tmpPdf.delete()
          tmpPng.delete()
        }
      }
    }
  }

  private def doWithTmpFile[T](suffix: String)(op: File => T): T = {
    val file = Files.createTempFile("eqn", suffix).toFile
    try {
      op(file)
    } finally {
      file.delete()
    }
  }

  private def doWithTmpTexFile[T](tex: String)(op: File => T): T = {
    doWithTmpFile(".tex") { file =>
      FileUtils.writeStringToFile(file, tex, "UTF-8")
      op(file)
    }
  }

  private val EquationPlaceholder = "EQUATIONPLACEHOLDER"

  private def extractEquation(file: File): String = {
    val (res, _) = FileUtils.readLines(file, "UTF-8").asScala
      .map(_.trim)
      .foldLeft(("", false)) { case ((eqn, inEqn), line) =>
          if (inEqn) {
            if (line.startsWith("""\]""")) (eqn, false) else {
              val prefix = if (eqn.nonEmpty) "\n" else ""
              (eqn + prefix + line, inEqn)
            }
          } else {
            if (line.startsWith("""\[""")) (eqn, true)
            else (eqn, inEqn)
          }
      }

    assert(res.nonEmpty, s"Could not extract equation from $file")
    res
  }

  private val equationsResourceDir: File = {
    val latexDir: File = {
      val path = getClass.getResource("/latex").getPath
      val classPathDir = new File(path)

      var targetDir = classPathDir
      while (targetDir.getName != "target") {
        targetDir = targetDir.getParentFile
      }

      val resultDir = new File(targetDir.getParent, "src/main/resources/latex")

      assert(resultDir.isDirectory, s"Not a directory: $path")
      assert(resultDir.canRead, s"Not readable: $path")

      resultDir
    }

    val resourceDir = new File(latexDir, "equations")
    assert(resourceDir.isDirectory, s"Not a directory: $resourceDir")
    assert(resourceDir.canWrite, s"Not writable: $resourceDir")
    resourceDir
  }

  private def equationFiles: Seq[File] = {
    equationsResourceDir.listFiles()
      .filter(f => f.getName.endsWith(".tex"))
  }

  private val equationsTemplate: String = {
    IOUtils.toString(getClass.getResource("/latex/equation-template.tex"))
  }
}
