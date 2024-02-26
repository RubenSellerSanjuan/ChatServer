__ChatServer__
==============
Para iniciar el chat, lo que haremos será encender primero el servidor, y posteriormente podremos acceder con tantos
clientes como nuestro pc tenga de nucleos en el procesador, gracias al ThreadPoolExecutor, principalmente, deberemos
indicar un nombre de usuario (Cualquier cosa que escribamos en el primer mensaje que se le enviaria al server será
tomado como nobmre de usuario), y posteriormente podremos empezar a enviar mensajes a los demas clientes conectados, los
cuales pasaran por el servidor, y este los enviara a los demas clientes, cada cliente tiene, para los demas clientes un
nombre de usuario, con el fin de que los demas clientes sepan quien es quien, el mismo cliente no puede ver su propio
nombre de usario.
El servidor, con el fin de guiarse y horientarse con el mismo, generará mensajes de debug, con el fin de ver si un cliente
esta conectado, si se ha ido, e incluso saber el numero de clientes actuales, finalmente, para terminar una conversacion,
el servidor se puede apagar, y este informa a los clientes conectados de que el mismo se esta apagando, o tambien podemos
desconectarnos como clientes mediante el mensaje escrito para el servidor "bye".
#

Caracteristicas:
________________
En este servidor usaremos Sockets para las conexiones al servidor, y entre clientes, BufferedReader para leer los mensajes
recibidos, Scanner para recoger los mismos, y guardarlos para enviarlos, y PrintWriter para enviar de uno a otro los
mensajes.

También se han usado algunas expresiones lambda (Recepcion y mostrado de mensajes en los clientes, y para generar un
shutDownHook en el server con el fin de cerrar el mismo, cuando se tenga que cerrar, de forma ordenada, en el cual
apagamos el ThreadPoolExecutor para cerrar los hilos, proceso el cual tendremos una espera maxima de 5 segundos, y
notifiamos a cada uno de los clientes que el servidor se está cerrando, además de que indicamos mediante un mensaje de
debug en el server propio que el mismo se ha apagado, cuando se apague claro está), con el fin de poder atender ciertas
funcionalidades.

Los datos se guardan en Maps(Usuarios, y mensajes guardados para usuarios desconectados, los cuales por tiempo no he
podido realizar su uso).

Usamos una clase creada por mi llamada conectionHandler la cual nos servira para generar las conexiones de cada uno de
los clientes que se quieran conectar gracias al ThreadPoolExecutor.