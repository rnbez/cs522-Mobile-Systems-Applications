package edu.stevens.cs522.chat.oneway.server.requests;

import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Created by Rafael on 3/12/2016.
 */
public class RestMethod {
    /* 15 Points
    * RestMethod provides logic for performing HTTP requests (using code samples from
    * notes)
    * */

    //    ... // See lectures.
    public Response perform(Register request) {
        Response response = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(request.getRequestUri().toString());
            connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestProperty("USER_AGENT", "");
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setRequestProperty("CONNECTION", "Keep-Alive");
//            connection.setConnectTimeout(...);
//            connection.setReadTimeout(...);

            Map<String, String> headers = request.getRequestHeaders();
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    connection.addRequestProperty(header.getKey(),
                            header.getValue());
                }
            }

            connection.connect();
            throwErrors(connection);
            JsonReader rd = new JsonReader(
                    new BufferedReader(
                            new InputStreamReader(connection.getInputStream())));
            response = request.getResponse(connection, rd);
            rd.close();
//            if (response.isValid()) {
//                return response;
//            } else {
//                return null;
//            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) connection.disconnect();
        }

        return response;
    }

    public Response perform(Unregister request) {
        Response response = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(request.getRequestUri().toString());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setUseCaches(false);
            connection.setRequestProperty("CONNECTION", "Keep-Alive");

            Map<String, String> headers = request.getRequestHeaders();
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    connection.addRequestProperty(header.getKey(),
                            header.getValue());
                }
            }

            connection.connect();
            throwErrors(connection);
            JsonReader rd = new JsonReader(
                    new BufferedReader(
                            new InputStreamReader(connection.getInputStream())));
            response = request.getResponse(connection, rd);
            rd.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) connection.disconnect();
        }

        return response;
    }

    public HttpURLConnection connection = null;
    public OutputStream uploadStream;
    public InputStream downloadStream;
    public StreamingResponse perform(Synchronize request) {
//        StreamingOutput out;
        try{
            URL url = new URL(request.getRequestUri().toString());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setRequestProperty("CONNECTION", "Keep-Alive");

            Map<String, String> headers = request.getRequestHeaders();
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.addRequestProperty(header.getKey(),
                        header.getValue());
            }

            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(0);
            connection.setDoInput(true);
//            outputStream = connection.getOutputStream();
//            OutputStream out = connection.getOutputStream();
//            uploadStream = out;
//            JsonWriter jw = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
//            request.write(jw);
//            throwErrors(connection);
//            downloadStream = connection.getInputStream();
            return new StreamingResponse(request.getResponse(connection, null));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    void outputRequestEntity(HttpURLConnection connection, Request request) throws IOException {
        String requestEntity = request.getRequestEntity();
        if (requestEntity != null) {
            connection.setDoOutput(true);
//            connection.setRequestProperty("CONTENT_TYPE","application/json");
            byte[] outputEntity = requestEntity.getBytes();
            connection.setFixedLengthStreamingMode(outputEntity.length);
            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            out.write(outputEntity);
            out.flush();
            out.close();
        }
    }

    public void throwErrors(HttpURLConnection connection) throws IOException {
        final int status = connection.getResponseCode();
        if (status < 200 || status >= 300) {
            String exceptionMessage = "Error response "
                    + status + " " + connection.getResponseMessage() +
                    " for " + connection.getURL();
            throw new IOException(exceptionMessage);
        }
    }



    public class StreamingResponse {
        private Response response;

        public StreamingResponse(Response response) {
            this.response = response;
        }
        public HttpURLConnection getGetConnection() {
            return RestMethod.this.connection;
        }

        public java.io.InputStream getInputStream() throws IOException {
            return RestMethod.this.connection.getInputStream();
        }

        public java.io.OutputStream getOutputStream() throws IOException {
            return RestMethod.this.connection.getOutputStream();
        }

        public Response getResponse() {
            return response;
        }

        public void disconnect() {
            // This will close upload and download streams
            RestMethod.this.closeConnection();
        }
    }

    private void closeConnection() {
        if (connection != null) connection.disconnect();
    }


}
