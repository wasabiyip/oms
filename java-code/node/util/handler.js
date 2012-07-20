function getType(message){
	
	if (message.charAt(0) === '$'){
		return $;
	}else if(message.charAt(0) === 'L'){
		return L;
	}else return false;
}

exports.getType = getType;
