package org.opencb.commons.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public final class CompressionUtils {

    private CompressionUtils() {
    }

    public static byte[] compress(byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);

        deflater.finish();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer); // returns the generated code... index
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();

        Logger.getLogger(CompressionUtils.class.getName()).log(Level.FINE, "Original: {0} Kb", data.length / 1024);
        Logger.getLogger(CompressionUtils.class.getName()).log(Level.FINE, "Compressed: {0} Kb", output.length / 1024);
        return output;
    }

    public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();

        Logger.getLogger(CompressionUtils.class.getName()).log(Level.FINE, "Original: {0}", data.length);
        Logger.getLogger(CompressionUtils.class.getName()).log(Level.FINE, "Compressed: {0}", output.length);
        return output;
    }
}
