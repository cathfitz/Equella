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
import Data.Tuple (Tuple(..))
import MaterialUI.AppBar (appBar', appBar_, position, static)
import MaterialUI.Color (inherit)
import MaterialUI.Properties (color, type_)
import MaterialUI.TextStyle (display3, display4)
import MaterialUI.Toolbar (toolbar')
import MaterialUI.Typography (typography')
import Partial.Unsafe (unsafePartial)
import React (ReactElement)
import React.DOM as D
import React.DOM.Props (_id, _type, action, dangerouslySetInnerHTML, method, name, value)
import ReactDOM (render)

foreign import renderData :: {html::M.StrMap String, title::String}

sample :: ReactElement
sample =
  D.div' [
    appBar' [position static] [
      toolbar' [] [
        typography' [type_ display3, color inherit] [ D.text renderData.title ]
      ]
    ],
    D.div [ dangerouslySetInnerHTML {__html: fromMaybe "" $ M.lookup "body" renderData.html} ] []
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
