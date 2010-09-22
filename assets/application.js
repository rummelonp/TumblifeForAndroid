var $ = function(id) {
  return document.getElementById(id);
};
var like = function() {
  $('like').setAttribute('class', 'like');
};