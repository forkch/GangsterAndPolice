exports.index = function(req, res){
  res.render('index', { title: 'Catch the thiefes', context: "mobile" });
};

exports.admin = function(req, res){
  res.render('game-admin', { title: 'Ctt - Admin', context: "mobile" });
};