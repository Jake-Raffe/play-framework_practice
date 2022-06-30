
package views.html

import _root_.play.twirl.api.TwirlFeatureImports._
import _root_.play.twirl.api.TwirlHelperImports._
import _root_.play.twirl.api.Html
import _root_.play.twirl.api.JavaScript
import _root_.play.twirl.api.Txt
import _root_.play.twirl.api.Xml
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import play.api.mvc._
import play.api.data._

object book extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply():play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*1.1*/("""<!DOCTYPE html "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <title>Google Books Embedded Viewer API Example</title>
    <script type="text/javascript" src="https://www.google.com/books/jsapi.js"></script>
    <script type="text/javascript">
      google.books.load();

      function initialize() """),format.raw/*11.29*/("""{"""),format.raw/*11.30*/("""
        """),format.raw/*12.9*/("""var viewer = new google.books.DefaultViewer(document.getElementById('viewerCanvas'));
        viewer.load('ISBN:0738531367');
      """),format.raw/*14.7*/("""}"""),format.raw/*14.8*/("""

      """),format.raw/*16.7*/("""google.books.setOnLoadCallback(initialize);
    </script>
</head>
<body>
<div id="viewerCanvas" style="width: 600px; height: 500px"></div>
</body>
</html>"""))
      }
    }
  }

  def render(): play.twirl.api.HtmlFormat.Appendable = apply()

  def f:(() => play.twirl.api.HtmlFormat.Appendable) = () => apply()

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  DATE: 2022-06-27T15:42:44.310117
                  SOURCE: /Users/jacob.raffe/Documents/scala-practice/play-framework/play-template/app/views/book.scala.html
                  HASH: c8344be1093c2201e62a4f13b0d7b5f6d3922b39
                  MATRIX: 810->0|1314->476|1343->477|1379->486|1538->618|1566->619|1601->627
                  LINES: 26->1|36->11|36->11|37->12|39->14|39->14|41->16
                  -- GENERATED --
              */
          