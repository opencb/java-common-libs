/*
 * Copyright 2015 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.commons.io.avro;


import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.opencb.commons.io.DataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Created on 02/04/15.
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class AvroFileWriter<T extends GenericRecord> implements DataWriter<ByteBuffer> {

    private final String codecName;
    private final Schema schema;
    private OutputStream outputStream;
    private final Path output;
    private final DataFileWriter<T> writer;
    private final DatumWriter<T> datumWriter;
    private int numWrites = 0;
    private boolean closeOutputStream;

    protected Logger logger = LoggerFactory.getLogger(this.getClass().toString());

    public AvroFileWriter(Schema schema, String codecName, OutputStream outputStream) {
        // By default, do not close OutputStreams if provided in constructor. Respect symmetric open/close
        this(schema, codecName, outputStream, false);
    }

    public AvroFileWriter(Schema schema, String codecName, OutputStream outputStream, boolean closeOutputStream) {
        this(schema, codecName, null, Objects.requireNonNull(outputStream));
        this.closeOutputStream = closeOutputStream;
    }

    public AvroFileWriter(Schema schema, String codecName, Path output) {
        this(schema, codecName, Objects.requireNonNull(output), null);
        closeOutputStream = true;
    }

    private AvroFileWriter(Schema schema, String codecName, Path output, OutputStream outputStream) {
        this.schema = schema;
        this.outputStream = outputStream;
        this.output = output;
        this.codecName = codecName;

        datumWriter = new SpecificDatumWriter<>();
        writer = new DataFileWriter<>(datumWriter);
        writer.setCodec(AvroCompressionUtils.getCodec(this.codecName));
    }

    @Override
    public boolean open() {
        try {
            if (outputStream == null) {
                outputStream = new FileOutputStream(output.toFile());
            }
            writer.create(schema, outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return true;
    }

    public void writeDatum(T datum) throws IOException {
        writer.append(datum);
    }

    @Override
    public boolean write(List<ByteBuffer> batch) {
        try {
            for (ByteBuffer byteBuffer : batch) {
                if (numWrites++ % 1000 == 0) {
                    logger.debug("Written {} elements", numWrites);
                }
                writer.appendEncoded(byteBuffer);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        logger.debug("[" + Thread.currentThread().getName() + "] Written " + batch.size());
        return true;
    }

    @Override
    public boolean close() {
        try {
            writer.flush();
            if (closeOutputStream) {
                writer.close();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return true;
    }
}
