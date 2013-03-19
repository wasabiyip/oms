/**
*Este archivo contiene funciones que se usan en diversos lugares,
*este archivo estará lleno de grandes sorpresas/alegrias.
**/
//Variable que ajusta la hora dependiendo del broker(CONSTANTE).
var AJUSTE = 9;
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
//Regresamos la hora en formato 12:00
function getDate(){
  var date = new Date();
  return date.getHours() + ':'+ date.getMinutes();
}
//regresamos la hora de la última vela para determinado periodo.
function getPeriodDate(periodo){
  var date = new Date();
  var hora = date.getHours()+AJUSTE>=24?(date.getHours()+AJUSTE)-24: date.getHours()+AJUSTE;
  var dif = date.getMinutes()%periodo;
  var min = date.getMinutes()- dif;
  return hora+':'+min;
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
//Tabla de /operaciones.
$(document).ready(function() {
    $('#example').dataTable({
          "bProcessing" : true,
          "sAjaxSource" : "ops.txt",
          "oLanguage": {
            "sLengthMenu": "Mostrando _MENU_ por página",
            "sZeroRecords": "No hay ordenes :(",
            "sInfo": "_START_ a _END_ de _TOTAL_ ordenes",
            "sInfoEmpty": "0 a 0 de 0 ordenes",
            "sInfoFiltered": "(filtered from _MAX_ total records)",
            "sSearch": "Buscar:",
            "oPaginate":{
              "sPrevious":"Anterior",
              "sNext":"Siguiente"
            }
        }
    });
} );

