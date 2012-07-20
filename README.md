OMS
===

Orden Managment System que usa el estándar <a href="http://fixprotocol.org/">Fix Protocol</a> para intercambiar mensajes con un broker, estos mensajes son tales como subscripción a un stream de precios de un determinado par de monedas.
Este sistema trabaja con operaciones en el mercado <a href="http://en.wikipedia.org/wiki/Foreign_exchange_market">FOREX</a>. 
Esta compuesta por:
<ul>
<li><a href="http://en.wikipedia.org/wiki/Nodejs">Node.js</a> como centro de comunicación entre un Navegador y el nucleo de la aplicación, también distribuye los precios a cada grafica que se conecta a él.</li>
<li>Tenemos una implementaciónde <a href="http://www.quickfixj.org/">Quickfixj</a> como Fix engine para manejar conexiones, sessiones, seguridad SSL y mensajes FIX. </li>
<li>Nuestro motor de base de datos es <a href="http://www.mongodb.org/">MongoDB</a>como dice la página es simple y poderoso!</li>

</ul>
