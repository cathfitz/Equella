
exports.renderData = renderData
exports.setBodyHtml = function(ref)
{
  return function()
  {
    if (ref)
    {
      $(ref).html(renderData.html.body);
    }
  }
}

exports.doOnClick = function (onClickJS)
{
  return function(e) {
    if (onClickJS)
    {
      eval(onClickJS)
    }
    e.stopPropagation();
    return false;
  }
}
