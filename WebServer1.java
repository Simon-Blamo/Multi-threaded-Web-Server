import java.io.* ;
import java.net.* ;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.* ;
public final class WebServer1 {
    public static void main(String argv[]) throws Exception {
        Selector selector = Selector.open();
        Selector selector1 = Selector.open();
        int port = 8111;
        int port1 = 5111;

        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        ServerSocketChannel serverChannel1 = ServerSocketChannel.open();
        
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress(port));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        
        serverChannel1.configureBlocking(false);
        serverChannel1.socket().bind(new InetSocketAddress(port1));
        serverChannel1.register(selector1, SelectionKey.OP_ACCEPT);

        while (true) {
            SocketChannel listeningSocket = serverChannel.accept(); 
            SocketChannel listeningSocket1 = serverChannel1.accept();
            if (listeningSocket != null) {
                HttpRequest request = new HttpRequest(listeningSocket.socket());
                Thread thread = new Thread(request);
                thread.start();
            }
            if(listeningSocket1 != null){
                MovedRequest request1 = new MovedRequest(listeningSocket1.socket());
                Thread thread1 = new Thread(request1);
                thread1.start();
            }
        }
    }

    final static class HttpRequest implements Runnable {
        final static String  CRLF = "\r\n";
        Socket socket;

        public HttpRequest(Socket socket) {
            this.socket = socket;
        }

        public void run(){
            try {
                processRequest();
            } catch (Exception e){
                System.out.println(e);
            }
        }

        private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {
            byte[] buffer = new byte[1024];
            int bytes = 0;

            while((bytes = fis.read(buffer)) != -1){
                os.write(buffer, 0, bytes);
            }
        }

        private static String contentType(String fileName) {
            if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
                return "text/html";
            }
            if(fileName.endsWith(".png") || fileName.endsWith(".jpeg") || fileName.endsWith(".gif") || fileName.endsWith(".tiff")) {
                int index = fileName.lastIndexOf(".");
                String fileType = fileName.substring(index+1);
                return "image/" + fileType;
            }
            return "application/octet-stream";
        }

        private void processRequest() throws Exception {
            InputStream inputStream = socket.getInputStream();
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());

            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(reader);
            
            String requestLine = br.readLine();
            System.out.println("Request Line:" + requestLine);

            StringTokenizer tokens = new StringTokenizer(requestLine);
            tokens.nextToken();
            String fileName = tokens.nextToken();
            System.out.println("File Name:" + fileName+ "\n");
            FileInputStream fis = null;
            boolean fileExists = true;
            try {
                fis = new FileInputStream(fileName);
            } catch (FileNotFoundException e) {
                
                fileExists = false;
            }
            
            String statusLine = null;
            String contentTypeLine = null;
            String entityBody = null;
            if(fileExists){
                statusLine = "HTTP/1.1 200 OK";
                contentTypeLine = "Content-type: " +
                contentType( fileName ) + CRLF;
            } else {
                statusLine = "HTTP/1.1 404 N Found";
                contentTypeLine = "text/html";
                entityBody = "<HTML><HEAD><TITLE>Not Found</TITLE></HEAD><BODY>Not Found</BODY></HTML>";
            }

            String headerLine = null;
            while((headerLine = br.readLine()).length() != 0){
                System.out.println(headerLine);
            }

            
            String response = statusLine + CRLF + contentTypeLine +  CRLF;
            if(fileExists){
                os.writeBytes(response);
                sendBytes(fis, os);
            } else {
                os.writeBytes(response + CRLF + entityBody);
            }

            os.close();
            br.close();
            socket.close();
        }
    }

    final static class MovedRequest implements Runnable {
        final static String  CRLF = "\r\n";
        Socket socket;

        public MovedRequest(Socket socket) {
            this.socket = socket;
        }

        public void run(){
            try {
                processRequest();
            } catch (Exception e){
                System.out.println(e);
            }
        }

        private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {
            byte[] buffer = new byte[1024];
            int bytes = 0;

            while((bytes = fis.read(buffer)) != -1){
                os.write(buffer, 0, bytes);
            }
        }

        private void processRequest() throws Exception {
            InputStream inputStream = socket.getInputStream();
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());

            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(reader);
            
            String requestLine = br.readLine();
            System.out.println("Request Line:" + requestLine);

            StringTokenizer tokens = new StringTokenizer(requestLine);
            tokens.nextToken();
            String fileName = tokens.nextToken();
            FileInputStream fis = null;
            boolean fileExists = true;
            try {
                fis = new FileInputStream(fileName);
            } catch (FileNotFoundException e) {
                
                fileExists = false;
            }
            
            String statusLine = null;
            String contentTypeLine = null;
            String entityBody = null;
            if(fileExists){
                statusLine = "HTTP/1.1 301 Moved Permanently";
                contentTypeLine = "Location: https://www.google.com/"+ CRLF;
            } else {
                statusLine = "HTTP/1.1 404 N Found";
                contentTypeLine = "text/html";
                entityBody = "<HTML><HEAD><TITLE>Not Found</TITLE></HEAD><BODY>Not Found</BODY></HTML>";
            }

            String headerLine = null;
            while((headerLine = br.readLine()).length() != 0){
                System.out.println(headerLine);
            }

            
            String response = statusLine + CRLF + contentTypeLine +  CRLF;
            if(fileExists){
                os.writeBytes(response);
                sendBytes(fis, os);
            } else {
                os.writeBytes(response + CRLF + entityBody);
            }

            os.close();
            br.close();
            socket.close();
        }
    }
}