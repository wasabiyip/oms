//Esta parte maneja operaciones con las graficas que se encuentran conectadas
var server_precios, server_op;
var clients = [];
var Graficas = [];
//Creamos una grafica y la añadimos a Graficas[]. 
exports.createGrafica = function(symbol, socket, settings){

    Graficas.push(new Grafica(symbol, socket, settings));
}
//Notificamos a cada grafica conectada con el precio entrante.
exports.notify = function (type,moneda, precio){
    msj = JSON.stringify({
        "msj" :{
            "type":type ,
            "precio":precio
        }
    });
    for(var i=0; i<Graficas.length; i++) {
        if(Graficas[i].getSymbol() === moneda){  
            //Es muy importante enviar '\n' ya que java espera un fin de 
            //linea: readLine();
            Graficas[i].getSocket().write(msj + '\n');
        }
    }
}

exports.expertState = function(txt){
    id= txt.id;
    msj = JSON.stringify({
        "msj" :{
            "type":"get-state",
            "value":id
        }
    });
    for(var i=0; i<Graficas.length; i++) {
        //**CUIDADO esto es una Charrada!
        if(Graficas[i].getSetts().ID == id){
            //tenemos que extraer los bytes del mensaje que vamos a enviar.	    	
            //Es muy importante enviar '\n' ya que java espera un fin de 
            //linea: readLine();
            Graficas[i].getSocket().write(msj +'\n');
        }
    }
}

//Regresamos el numero de graficas.
exports.graficasLength = function(){	

    return Graficas.length;
}
//Revisamos si hay una grafica de determinado symbolo.
exports.symbolExists = function(symbol){
    
    for(i=0; i<Graficas.length;i++){
        if(Graficas[i].getSymbol() === symbol){
            return true;
        }
    }
}

//Obtenemos una grafica apartir de su socket.
exports.getGrafica= function(socket){
	
    for(i=0; i<Graficas.length; i++){
						
        if (Graficas[i].getSocket() === socket){

            return Graficas[i];
        }		
    }
}

//Borramos a una grafica de el array por que se perdio conexion con ella,
//esto para evitar tener graficas que no existen.
exports.closeGrafica= function(socket){

    for(i=0; i<Graficas.length; i++){				
        if (Graficas[i].getSocket().name === socket.name){
            Graficas.splice(i,1);
        }		
    }
}
//regresamos los datos de experts conectados.
exports.getSetts= function(){
	
    var setts = [];
    for (i=0; i<Graficas.length; i++){
        setts.push(Graficas[i].getSetts());
    }
    return setts;
}

exports.closeOrder = function(close){
    msj = JSON.stringify({
        "msj" :{
            "type":"close-order",
            "value":close.id
        }
    });
    
    for(i=0; i<Graficas.length;i++){
        if(Graficas[i].getSetts().ID===close.grafica){
            Graficas[i].getSocket().write(msj +"\n");
        }
    }
}
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
//-------------------------------------------------------------------//
/*
	Objeto Grafica, cada vez  que se conecta una grafica creamos un objeto
	con sus datos.
*/
function Grafica(symbol, socket, settings){
    this.symbol = symbol;
    this.socket = socket;
    this.settings = settings;
    var bollUp,bollDn;
    var bollUpS, bollDnS;
    var price;
    
    this.getData = function(){
        return data;
    }

    this.getSetts = function(){
        return this.settings;
    }

    this.getSymbol = function(){
        return this.symbol;
    }

    this.getSocket = function(){
        return this.socket;
    }
    /*
		Setters: Puede que los borre...
	*/
    this.setBollUp= function(price){
        this.bollUp = price;
    }

    this.setBollDn= function(price){
        this.bollDn = price;
    }

    this.setBollUpS= function(price){
        this.bollUpS = price;
    }

    this.setBollDnS= function(price){
        this.bollDnS = price;
    }
    this.setPrice = function(price){
        this.price = price;
    }
}
/*
***--Fin de Objeto--***
*/
//-------------------------------------------------------------------//

