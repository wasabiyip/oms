/**
*Este archivo contiene funciones que se usan en diversos lugares,
*este archivo estará lleno de grandes sorpresas/alegrias.
**/
//Checamos cual es el titulo de la página actualmente cargada para 
//marcar su correspondiente nav como activo pa' que se vea bonito.
switch(document.title){
	case "Monitoreo":
		$('.nav #monitor').addClass('active');
		break;
	case "Hitorial":
		$('.nav #historial').addClass('active');
		break;
	case "Trade":
		$('.nav #trading').addClass('active');
		break;
	default:
		console.log('El horror!');
}
$("#charts-tab:first-child").addClass('active');
//
function getDate(){
  var date = new Date();
  return date.getHours() + ':'+ date.getMinutes();
}
//
function unID(cadena){
  var text = cadena.split("");
  var res ="";
  for(i=0; i<text.length; i++){
      res +=text[i];
      if(i==2)
          res += '/';
  }  
  return res.slice(0, res.lastIndexOf("-"));
}
//Quitamos un / de el symbolo generalmente USD/JPY es igual a USDJPY
function unSlash(cadena){
  return cadena.replace("/","");
}
//Lo inverso a lo anterior...
function Slash(cadena){
  var text = cadena.split("");
  var res ="";
  for(i=0; i<text.length; i++){
      res +=text[i];
      if(i==2)
          res += '/';
  }
  return res;
}
//
function redondear( precio){       
	return Math.round(precio*100000)/100000;
}

function notificator(moneda,type){
  var val = parseInt($('#'+moneda+'-notif').html());
    if(isNaN(val)){
      val=0;
    }
  if(type == '1'){
    $('#'+moneda+'-notif').html(val+1);
  }else if(type == '0'){
    if((val-1)==0){
      $('#'+moneda+'-notif').html('');
    }else{
      $('#'+moneda+'-notif').html(val-1);
    }
    
  }

}

