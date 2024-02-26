import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Cliente {

    public static void main(String[] args){
        //TODO Indicamos la ip del server
        String serverHostname = "localhost";
        //TODO Indicamos puerto del server
        int port = 6789;

        try (Socket clientSocket = new Socket(serverHostname, port);
            //TODO Usaremos el printWriter para crear un flujo de salida para enviar mensajes al servidor
            PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
            //TODO Usaremos el bufferedReader para crear un flujo de entrada para recibir mensajes del servidor
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //TODO Con el siguiente Scanner leeremos la entrada del usuario para hacerla llegar al servidor
            Scanner outMessagerToServer = new Scanner(System.in)) {

            /*
            TODO Creamos una expresion lambda que genera un hilo para recibir y mostrar mensajes del servidor, esto lo
             he realizado consultando y con la ayuda de mi tio, lo digo por si acaso ;)
             */
            Thread serverMessages = new Thread(() -> {
                try {
                    String message;

                    /*
                    TODO Comprobamos que la entrada de información del servidor, la cual recibimos mediante el
                     bufferedReader inFromServer, no sea null, mientras esto sea así, imprimiremos dicha informacion
                     */
                    while ((message = inFromServer.readLine()) != null) {
                        System.out.println(message);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            /*
            TODO Iniciamos el hilo con expresion lambda anteriormente generado para empezar el proceso de recepción e
             impresion de mensajes del server
             */
            serverMessages.start();

            /*
            TODO Lee la entrada del usuario mediante el Scanner y envía mensajes al servidor con el PrintWriter
             outToServer, esto lo haremos en un bucle infinito, de manera que podremos continuar el chat tdo lo que
             querramos, o al menos hasta que el usuario en cuestion no envie un mensaje con el contenido "bye"
             */
            while(true) {
                outToServer.println(outMessagerToServer.nextLine());
            }
        //TODO recogeremos las excepciones necesarias con el fin de manejar los errores
        } catch (UnknownHostException e) {
            System.err.println("No se puede encontrar el host: " + serverHostname);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("No se puede obtener I/O para la conexión con: " + serverHostname);
            System.exit(1);
        }
    }
}
