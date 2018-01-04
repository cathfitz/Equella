package com.tle.web.template

import com.tle.web.freemarker.FreemarkerFactory
import com.tle.web.resources.ResourcesService
import com.tle.web.sections._
import com.tle.web.sections.events._
import com.tle.web.sections.render._
import io.circe.generic.auto._
import io.circe.syntax._
import org.jsoup.Jsoup

object RenderNewTemplate {
  val r = ResourcesService.getResourceHelper(getClass)

  case class SectionData(html: Map[String, String], title: String)

  case class TemplateScript(getRenderJs: String, getTemplate: TemplateResult) {
    def getScriptUrl = r.url("js/index.js")
  }


  def renderHtml(viewFactory: FreemarkerFactory, context: RenderEventContext, tempResult: TemplateResult): SectionResult = {

    val _bodyResult = tempResult.getNamedResult(context, "body")
    val unnamedResult = tempResult.getNamedResult(context, "unnamed")
    val bodyResult = CombinedRenderer.combineResults(_bodyResult, unnamedResult)
    val bodyTag = context.getBody
    val formTag = context.getForm
    formTag.setNestedRenderable(bodyResult)
    bodyTag.setNestedRenderable(formTag)
    val _html = SectionUtils.renderToString(context, bodyTag)
    val html = Jsoup.parseBodyFragment(_html).body().html()
    println(html)
    val title = Option(Decorations.getDecorations(context).getTitle).map(_.getText).getOrElse("")
    val renderData = SectionData(Map("body" -> html), title).asJson.spaces2.replaceAll("</script>", "</scrip\"+\"t>")
    viewFactory.createResultWithModel("layouts/outer/react.ftl", TemplateScript(renderData, tempResult))
  }
}
