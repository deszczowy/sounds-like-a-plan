package pl.deszczowy.slap;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

class Decoder {

    private String string;

    Decoder(String input){
        if (!input.equals("")) {
            try {
                decode(input);
            } catch (IOException e) {
                this.string = "";
            }
        }else{
            this.string = "";
        }
    }
    void decode(String input) throws IOException {
        this.string = null;
        byte[] bytes = Base64.decode(input, Base64.DEFAULT);
        GZIPInputStream zi = null;
        try {
            zi = new GZIPInputStream(new ByteArrayInputStream(bytes));
            this.string = "";

            InputStreamReader reader = new InputStreamReader(zi);
            BufferedReader in = new BufferedReader(reader);

            String readed;
            while ((readed = in.readLine()) != null) {
                this.string += (readed)+"\n";
            }
        } finally {
            zi.close();
        }
    }

    String get(){
        return this.string;
    }

    String code(String input) throws IOException{
        ByteArrayOutputStream rstBao = new ByteArrayOutputStream();
        GZIPOutputStream zos = new GZIPOutputStream(rstBao);
        zos.write(input.getBytes());
        zos.close();

        byte[] bytes = rstBao.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}
