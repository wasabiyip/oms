var profile= '-';
var setProfile = function(prof_name){
	console.log("Perfil :"+prof_name);
	profile = prof_name;
}
var getProfile = function(){
	return profile;
}
exports.setProfile = setProfile;
exports.getProfile = getProfile;