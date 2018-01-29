package com.tle.web.template

import com.tle.common.institution.CurrentInstitution
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.plugins.PluginTracker
import com.tle.web.freemarker.FreemarkerFactory
import com.tle.web.navigation.MenuService
import com.tle.web.resources.ResourcesService
import com.tle.web.sections._
import com.tle.web.sections.equella.js.StandardExpressions
import com.tle.web.sections.events._
import com.tle.web.sections.events.js.JSHandler
import com.tle.web.sections.jquery.libraries.JQueryCore
import com.tle.web.sections.render._
import com.tle.web.template.Decorations.MenuMode
import com.tle.web.template.section.MenuContributor
import io.circe.generic.auto._
import io.circe.syntax._

import scala.collection.JavaConverters._

object RenderNewTemplate {
  val r = ResourcesService.getResourceHelper(getClass)

  def isNewLayout(info: SectionInfo): Boolean = {
    Option(info.getAttribute("NEW_LAYOUT")).getOrElse {
      val oldOverride = info.getRequest.getParameter("old") != null
      info.setAttribute("NEW_LAYOUT", !oldOverride)
      !oldOverride
    }
  }

  case class MenuItem(href: Option[String], title: String, onclick: Option[String], systemIcon: Option[String])

  case class SectionData(html: Map[String, String], title: String, menuItems: Iterable[MenuItem])

  case class TemplateScript(getRenderJs: String, getTemplate: TemplateResult) {
    def getScriptUrl = r.url("js/index.js")
  }


  def renderHtml(viewFactory: FreemarkerFactory, context: RenderEventContext,
                 tempResult: TemplateResult, menuService: MenuService): SectionResult = {

    context.preRender(JQueryCore.PRERENDER)
    val _bodyResult = tempResult.getNamedResult(context, "body")
    val unnamedResult = tempResult.getNamedResult(context, "unnamed")
    val bodyResult = CombinedRenderer.combineResults(_bodyResult, unnamedResult)
    val bodyTag = context.getBody
    val formTag = context.getForm
    formTag.setNestedRenderable(bodyResult)
    bodyTag.setNestedRenderable(formTag)
    val menuValues = menuOptions(context, menuService)
    val html = SectionUtils.renderToString(context, bodyTag)
    val title = Option(Decorations.getDecorations(context).getTitle).map(_.getText).getOrElse("")
    val renderData = SectionData(Map("body" -> html), title, menuValues).asJson.spaces2.replaceAll("</script>", "</scrip\"+\"t>")
    viewFactory.createResultWithModel("layouts/outer/react.ftl", TemplateScript(renderData, tempResult))
  }

  private val GUEST_FILTER = new PluginTracker.ParamFilter("enabledFor", "guest")
  private val SERVER_ADMIN_FILTER = new PluginTracker.ParamFilter("enabledFor", "serverAdmin")
  private val LOGGED_IN_FILTER = new PluginTracker.ParamFilter("enabledFor", true, "loggedIn")

  def menuOptions(context: RenderEventContext, menuService: MenuService) : Iterable[MenuItem] = {
    val decorations = Decorations.getDecorations(context)
    val menuMode = decorations.getMenuMode
    if (menuMode == MenuMode.HIDDEN) Iterable.empty
    else {
      val contributors = menuService.getContributors
      val filter = if (CurrentInstitution.get == null) SERVER_ADMIN_FILTER
      else if (CurrentUser.isGuest) GUEST_FILTER
      else LOGGED_IN_FILTER

      def handlerText(handler: JSHandler) : String = {
        context.preRender(handler)
        handler.getStatements(context)
      }
      contributors.getExtensions(filter).asScala.flatMap { ext =>
        contributors.getBeanByExtension(ext).getMenuContributions(context).asScala
      }.sortBy(m => m.getGroupPriority * 1000 + m.getLinkPriority).map {
        mc =>
          val menuLink = mc.getLink
          MenuItem(Option(menuLink.getBookmark).flatMap(b => Option(b.getHref)),
            menuLink.getLabelText,
            Option(menuLink.getHandlerMap.getHandler("click")).map(handlerText),
            Option(mc.getSystemIcon))
      }
    }
  }
}
