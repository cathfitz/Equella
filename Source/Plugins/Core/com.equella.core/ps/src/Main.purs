module Main where

import Prelude

import Control.Monad.Eff (Eff)
import Control.Monad.Eff.Console (CONSOLE, logShow)
import DOM (DOM)
import DOM.HTML (window)
import DOM.HTML.Types (htmlDocumentToDocument)
import DOM.HTML.Window (document)
import DOM.Node.NonElementParentNode (getElementById)
import DOM.Node.Types (ElementId(ElementId), documentToNonElementParentNode)
import Data.Maybe (fromJust, fromMaybe)
import Data.StrMap as M
import MaterialUI.AppBar (absolute, appBar', position, static)
import MaterialUI.Color (inherit)
import MaterialUI.Divider (divider_)
import MaterialUI.Drawer (anchor, drawer', left, open, persistent)
import MaterialUI.List (list', list_)
import MaterialUI.ListItem (listItem_)
import MaterialUI.ListItemText (listItemText', primary)
import MaterialUI.PropTypes (ut)
import MaterialUI.Properties (className, color, type_)
import MaterialUI.Properties (classes) as MP
import MaterialUI.Styles (mediaQuery, withStyles)
import MaterialUI.TextStyle (display3, title)
import MaterialUI.Toolbar (toolbar')
import MaterialUI.Typography (typography')
import Partial.Unsafe (unsafePartial)
import React (ReactElement, createClassStateless, createElement, createFactory)
import React.DOM as D
import React.DOM.Props as DP
import ReactDOM (render)

foreign import renderData :: {html::M.StrMap String, title::String}




sample :: ReactElement
sample = createFactory (withStyles ourStyles (createClassStateless render)) {}
  where
  drawerWidth = 240
  ourStyles theme = {
    root: {
      width: "100%",
      marginTop: theme.spacing.unit * 3,
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
    navIconHide: {
      -- [theme.breakpoints.up('md')]: {
      --   display: 'none',
      -- },
    },
    drawerHeader: theme.mixins.toolbar,
    drawerPaper: mediaQuery (theme.breakpoints.up "md") {
        width: drawerWidth,
        position: "relative",
        height: "100%"
      }
      { width: 250 },
    content: {
      backgroundColor: theme.palette.background.default,
      width: "100%",
      padding: theme.spacing.unit * 3,
      height: "calc(100% - 56px)",
      marginTop: 56
      -- [theme.breakpoints.up('sm')]: {
      --   height: 'calc(100% - 64px)',
      --   marginTop: 64,
      -- },
    }
  }
  render {classes} =
    D.div [DP.className classes.root] [
      D.div [DP.className classes.appFrame] [
        appBar' [className classes.appBar] [
          toolbar' [] [
            typography' [type_ title, color inherit] [ D.text renderData.title ]
          ]
        ],
        drawer' [type_ persistent, anchor left, open true, MP.classes {paper: classes.drawerPaper :: String} ] [
          divider_ [],
          list_ [
            listItem_ [
              listItemText' [primary $ ut "Search"] []
            ]
          ]
        ],
        D.main [ DP.className classes.content, DP.dangerouslySetInnerHTML {__html: fromMaybe "" $ M.lookup "body" renderData.html} ] []
      ]
    ]

main :: forall eff. Eff (dom :: DOM, console::CONSOLE | eff) Unit
main = do
  void (elm' >>= render sample)
  logShow renderData.html
  where

  elm' = do
    win <- window
    doc <- document win
    elm <- getElementById (ElementId "example") (documentToNonElementParentNode (htmlDocumentToDocument doc))
    pure $ unsafePartial (fromJust elm)
