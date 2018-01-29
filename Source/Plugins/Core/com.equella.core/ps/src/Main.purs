module Main where

import Prelude

import Control.Monad.Eff (Eff)
import Control.Monad.Eff.Console (CONSOLE)
import DOM (DOM)
import DOM.HTML (window)
import DOM.HTML.Types (htmlDocumentToDocument)
import DOM.HTML.Window (document)
import DOM.Node.NonElementParentNode (getElementById)
import DOM.Node.Types (ElementId(ElementId), documentToNonElementParentNode)
import Data.Array (fromFoldable)
import Data.Maybe (fromJust, fromMaybe)
import Data.Nullable (Nullable, toMaybe)
import Data.StrMap as M
import Dispatcher (DispatchEff(DispatchEff), effEval)
import Dispatcher.React (ReactProps(ReactProps), createComponent, modifyState)
import MaterialUI.AppBar (appBar')
import MaterialUI.ButtonBase (onClick)
import MaterialUI.Color (inherit)
import MaterialUI.Divider (divider_)
import MaterialUI.Drawer (anchor, drawer', left, open, persistent, temporary)
import MaterialUI.Hidden (css, hidden', implementation, mdUp, smDown)
import MaterialUI.Icon (icon_)
import MaterialUI.IconButton (iconButton')
import MaterialUI.List (list_)
import MaterialUI.ListItem (button, listItem')
import MaterialUI.ListItemIcon (listItemIcon_)
import MaterialUI.ListItemText (listItemText', primary)
import MaterialUI.PropTypes (EventHandler, handle, ut)
import MaterialUI.Properties (className, color, component, mkProp, type_)
import MaterialUI.Properties (classes) as MP
import MaterialUI.Styles (mediaQuery, withStyles)
import MaterialUI.TextStyle (title)
import MaterialUI.Toolbar (toolbar')
import MaterialUI.Typography (typography')
import Partial.Unsafe (unsafePartial)
import React (ReactElement, Ref, createFactory)
import React.DOM as D
import React.DOM.Props as DP
import ReactDOM (render)

newtype MenuItem = MenuItem {href::Nullable String, title::String, onclick::Nullable String, systemIcon::Nullable String}

data Command = ToggleMenu

foreign import renderData :: {html::M.StrMap String, title::String, menuItems :: Array MenuItem}
foreign import setBodyHtml :: forall eff. Nullable Ref -> Eff eff Unit
foreign import doOnClick :: Nullable String -> EventHandler Unit

type State = {mobileOpen::Boolean}

initialState :: State
initialState = {mobileOpen:false}

sample :: ReactElement
sample = createFactory (withStyles ourStyles (createComponent initialState render (effEval eval))) {}
  where
  drawerWidth = 240
  ourStyles theme = {
    root: {
      width: "100%",
      zIndex: 1
    },
    appFrame: {
      position: "relative",
      display: "flex",
      width: "100%",
      height: "100%"
    },
    appBar: mediaQuery (theme.breakpoints.up "md") {
        width: "calc(100% - " <> show drawerWidth <> "px)"
      } {
      position: "absolute",
      marginLeft: drawerWidth
    },
    navIconHide:
      mediaQuery (theme.breakpoints.up "md") {
        display: "none"
      } {}
    ,
    drawerHeader: theme.mixins.toolbar,
    drawerPaper: mediaQuery (theme.breakpoints.up "md") {
        width: drawerWidth,
        position: "relative",
        height: "100%"
      }
      { width: 250 },
    content: mediaQuery (theme.breakpoints.up "sm") {
        height: "calc(100% - 64px)",
        marginTop: 64
      } {
      backgroundColor: theme.palette.background.default,
      width: "100%",
      padding: theme.spacing.unit * 3,
      height: "calc(100% - 56px)",
      marginTop: 56
    }
  }

  menuItem (MenuItem {title,href,onclick,systemIcon}) = listItem' (hrefProp <> [button true, component "a", mkProp "onClick" $ doOnClick onclick]) [
      listItemIcon_ [icon_ [ D.text $ fromMaybe "folder" $ toMaybe systemIcon ] ],
      listItemText' [primary $ ut title] []
    ]
    where
      hrefProp = fromFoldable $ mkProp "href" <$> toMaybe href

  menuContent = [
    divider_ [],
    list_ (menuItem <$> renderData.menuItems)
  ]

  eval ToggleMenu = modifyState \(s :: State) -> s {mobileOpen = not s.mobileOpen}

  render {mobileOpen} (ReactProps {classes}) (DispatchEff d) =
    D.div [DP.className classes.root] [
      D.div [DP.className classes.appFrame] [
        appBar' [className classes.appBar] [
          toolbar' [] [
            iconButton' [color inherit, className classes.navIconHide, onClick $ handle $ d \_ -> ToggleMenu] [ D.text "menu" ],
            typography' [type_ title, color inherit] [ D.text renderData.title ]
          ]
        ],
        hidden' [ mdUp true ] [
          drawer' [ type_ temporary, anchor left, MP.classes {paper: classes.drawerPaper},
                    open mobileOpen, mkProp "onClose" (handle $ d \_ -> ToggleMenu) ] menuContent ],
        hidden' [ smDown true, implementation css ] [
          drawer' [type_ persistent, anchor left, open true, MP.classes {paper: classes.drawerPaper} ] menuContent
        ],
        D.main [ DP.withRef setBodyHtml, DP.className classes.content ] []
      ]
    ]

main :: forall eff. Eff (dom :: DOM, console::CONSOLE | eff) Unit
main = do
  void (elm' >>= render sample)
  where

  elm' = do
    win <- window
    doc <- document win
    elm <- getElementById (ElementId "example") (documentToNonElementParentNode (htmlDocumentToDocument doc))
    pure $ unsafePartial (fromJust elm)
