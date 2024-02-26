import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.Scanner;
import java.util.concurrent.*;

public class Server {
    /*
    TODO Guardaremos los clientes conectados dentro de un Map, ya que en este ejercicio a cada usuario he hecho que
     se identifique con su nombre, y un printWriter Especifico para cada uno de estos
     */
    private static Map<String, PrintWriter> clients = new ConcurrentHashMap<>();
    /*
    TODO Como suposicion, he intentado mantener un guardado de mensajes para los clientes no conectados con otro Map,
     pero no he conseguido que funcione este, asi que en pocas palabras, este map serviria para guardar los mensajes
     y enviarselos a usuarios que no estan ya conectados una vez vuelvan a conectarse
     */
    private static Map<String, Queue<String>> notReadenMessages = new ConcurrentHashMap<>();
    /*
    TODO Generamos un executor para poder hacer uso del ThreadPoolExecutor
     */
    private static ExecutorService executor;

    public static void main(String[] args) throws IOException {
        //TODO Indicamos puerto en el cual el server estará escuchando
        int port = 6789;
        /*
        TODO Mediante el executor creado anteriormente, y la ejecucion de Runtime.getRuntime.avaliableProcessors,
         indicamos el numero maximo de usuarios que tendremos, el cual será el numero maximo de cores que tiene nuestro
         procesador
         */
        int numCores = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(numCores);

        /*
        TODO Agregamos una expresion lambda que hace uso de un ShutdownHook para cerrar el servidor de manera ordenada,
         y poco a poco
         */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Cerrando el servidor...");
            //TODO Cerramos el executor
            executor.shutdown();

            try {
                /*
                TODO Esperamos y nos aseguramos de que todos los hilos se hayan cerrado de manera correcta, antes de
                 que el servidor se apague
                 */
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                //TODO En el caso de que el proceso de espera llegase al final, interrumpiriamos el thread
                Thread.currentThread().interrupt();
            }

            //TODO Indicamos un mensaje que diga que el server se ha cerrado correctamente
            System.out.println("Servidor cerrado correctamente.");
            //TODO Notifica a todos los clientes que el servidor se cierra
            notifyAllClients("El servidor se va a cerrar en breves momentos...", null);
        }));

        //TODO Iniciamos un nuevo server socket para poder aceptar a nuevos clientes al intentar estos entrar
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            //TODO Indicamos con un mensaje que el servidor se ha iniciado, y el puerto en el cual escucha
            System.out.println("Servidor iniciado y escuchando en el puerto " + port);

            //TODO Creamos un bucle infinito que nos permite recibir todos los clientes que queramos nosotros (hasta el máximo)
            while (true) {
                //TODO Como he dicho antes, aceptamos todos los clientes que quieran entrar
                Socket socket = serverSocket.accept();
                //TODO Creamos un printwriter que nos servira para enviar la informacion del server al cliente en cuestión
                PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);

                //TODO Preguntaremos al cliente por su nombre de usuario con el fin de que se pueda "logear"
                outToClient.println("Introduce tu nombre de usuario:");
                //TODO Creamos un scanner para recibir la información procediente del cliente
                Scanner inFromClient = new Scanner(socket.getInputStream());
                //TODO Indicamos el nombre del cliente en una variable para que este se pueda identificar con el mismo
                String username = inFromClient.nextLine();
                //TODO Indicamos el nombre de usuario y el printWriter asociado al mismo
                clients.put(username, outToClient);

                    /*
                    TODO Con este procedimiento, se ha intentado realizar que un cliente recibiera los mensajes que no
                     podia leer mientras estaba conectado, pero no he podido sacarlo, y no tenia suficiente tiempo...
                     */
                Queue<String> messages = notReadenMessages.get(username);

                if (messages != null && !messages.isEmpty()) {
                    while (!messages.isEmpty()) {
                        String pendingMessage = messages.poll();

                        outToClient.println(pendingMessage);
                    }
                }

                //TODO Manejaremos la conexion de el cliente actual en un hilo para el mismo
                executor.execute(new ConnectionHandler(socket, username));
                //TODO Indicamos que el usuario se a conectado, excepcionando al usuario actual
                notifyAllClients(username + " se ha conectado", null);
                    /*
                    TODO No era necesario realmente, pero he creado mensajes de debug en el servidor también, con el
                     simple fin de poder saber que transcurre en el mismo en tdo momento (Cuando se conecta un cliente,
                     cuando se desconecta, etc...)
                     */
                System.out.println(username + " se ha conectado. Número de clientes conectados: " +
                        clients.size());
            }
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    /*
    TODO Creamos un metodo para enviar los mensajes a los clientes que esten conectados, excepcionand actualmente al
     cliente en cuestión
     */
    private static void notifyAllClients(String message, String senderUsername) {
        for (Map.Entry<String, PrintWriter> entry : clients.entrySet()) {
            String username = entry.getKey();
            PrintWriter outToClient = entry.getValue();

            //TODO Evita enviar el mensaje de vuelta al cliente actual en cuestión
            if (!username.equals(senderUsername)) {
                outToClient.println(message);
            }
        }
    }

    //TODO Creamos una clase interna para manejar las conexiones que se realizarán al server
    private static class ConnectionHandler implements Runnable {
        private Socket connectionSocket;
        private String username;

        public ConnectionHandler(Socket connectionSocket, String username) {
            this.connectionSocket = connectionSocket;
            this.username = username;
        }

        @Override
        public void run() {
            //TODO Creamos un bufferedReader para recoger la informacion que recibimos del cliente
            try (BufferedReader inFromClient = new BufferedReader(new
                    InputStreamReader(connectionSocket.getInputStream()))) {

                //TODO Realizamos un bucle infinito para poder continuar mandando mensajes hasta que se indique bye como mensaje
                while (true) {
                    //TODO Creamos la variable que recogera el mensaje
                    String message = inFromClient.readLine();

                    //TODO Comprobamos principalmente si el cliente quiere desconectarse, mirando si su mensaje es bye
                    if (message.equalsIgnoreCase("bye")) {
                        break;
                    }

                    //TODO Reenvía el mensaje a todos los clientes conectados, excepto al cliente actual en cuestión
                    String messageToSend = username + ": " + message;

                    notifyAllClients(messageToSend, username);
                }
            } catch (IOException e) {
                System.err.println("Error de E/S con el cliente: " + e.getLocalizedMessage());
            } finally {
                //TODO Al desconectarse un clinte, borraremos el nombre del mismo del Map de clientes
                clients.remove(username);
                //TODO Indicamos a todos los clientes que un cliente se ha desconectado
                notifyAllClients(username + " se ha desconectado", null);
                /*
                TODO Igual que antes, guardamos un mensaje de debug en el propio servidor con el fin de poder saber que
                 pasa en todo momento en el mismo
                 */
                System.out.println(username + " se ha desconectado. Número de clientes conectados: " +
                        clients.size());
            }
        }
    }
}
